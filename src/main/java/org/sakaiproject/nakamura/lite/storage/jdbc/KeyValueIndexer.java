package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageConstants;
import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
import org.sakaiproject.nakamura.lite.storage.DisposableIterator;
import org.sakaiproject.nakamura.lite.storage.Disposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;

public abstract class KeyValueIndexer extends AbstractIndexer {

    private static final int STMT_BASE = 0;
    private static final int STMT_TABLE_JOIN = 1;
    private static final int STMT_WHERE = 2;
    private static final int STMT_WHERE_SORT = 3;
    private static final int STMT_ORDER = 4;
    private static final int STMT_EXTRA_COLUMNS = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyValueIndexer.class);
    protected JDBCStorageClient client;

    public KeyValueIndexer(JDBCStorageClient jdbcStorageClient, Set<String> indexColumns, Map<String, Object> sqlConfig) {
        super(indexColumns);
        this.client = jdbcStorageClient;
    }

    public DisposableIterator<Map<String, Object>> find(final String keySpace, final String columnFamily,
            Map<String, Object> properties) throws StorageClientException {
        String[] keys = null;
        if ( properties != null  && properties.containsKey(StorageConstants.CUSTOM_STATEMENT_SET)) {
            String customStatement = (String) properties.get(StorageConstants.CUSTOM_STATEMENT_SET);
            keys = new String[] { 
                    customStatement+ "." + keySpace + "." + columnFamily,
                    customStatement +  "." + columnFamily, 
                    customStatement, 
                    "block-find." + keySpace + "." + columnFamily,
                    "block-find." + columnFamily, 
                    "block-find" 
           };            
        } else {
            keys = new String[] { "block-find." + keySpace + "." + columnFamily,
                    "block-find." + columnFamily, "block-find" };            
        }
        
        final boolean rawResults = properties != null && properties.containsKey(StorageConstants.RAWRESULTS);

        String sql = client.getSql(keys);
        if (sql == null) {
            throw new StorageClientException("Failed to locate SQL statement for any of  "
                    + Arrays.toString(keys));
        }

        String[] statementParts = StringUtils.split(sql, ';');

        StringBuilder tables = new StringBuilder();
        StringBuilder where = new StringBuilder();
        StringBuilder order = new StringBuilder();
        StringBuilder extraColumns = new StringBuilder();

        // collect information on paging
        long page = 0;
        long items = 25;
        if (properties != null) {
          if (properties.containsKey(StorageConstants.PAGE)) {
            page = Long.valueOf(String.valueOf(properties.get(StorageConstants.PAGE)));
          }
          if (properties.containsKey(StorageConstants.ITEMS)) {
            items = Long.valueOf(String.valueOf(properties.get(StorageConstants.ITEMS)));
          }
        } else {
            properties = ImmutableMap.of();
        }
        long offset = page * items;

        // collect information on sorting
        String[] sorts = new String[] { null, "asc" };
        String _sortProp = (String) properties.get(StorageConstants.SORT);
        if (_sortProp != null) {
          String[] _sorts = StringUtils.split(_sortProp);
          if (_sorts.length == 1) {
            sorts[0] = _sorts[0];
          } else if (_sorts.length == 2) {
            sorts[0] = _sorts[0];
            sorts[1] = _sorts[1];
          }
        }

        List<Object> parameters = Lists.newArrayList();
        int set = 0;
        for (Entry<String, Object> e : properties.entrySet()) {
            Object v = e.getValue();
            String k = e.getKey();
            if ( shouldFind(keySpace, columnFamily, k) || (v instanceof Map)) {
                if (v != null) {
                  // check for a value map and treat sub terms as for OR terms.
                  // Only go 1 level deep; don't recurse. That's just silly.
                  if (v instanceof Map) {
                    // start the OR grouping
                    where.append(" (");
                    @SuppressWarnings("unchecked")
                    Set<Entry<String, Object>> subterms = ((Map<String, Object>) v).entrySet();
                    for(Iterator<Entry<String, Object>> subtermsIter = subterms.iterator(); subtermsIter.hasNext();) {
                      Entry<String, Object> subterm = subtermsIter.next();
                      String subk = subterm.getKey();
                      Object subv = subterm.getValue();
                      // check that each subterm should be indexed
                      if (shouldFind(keySpace, columnFamily, subk)) {
                        set = processEntry(statementParts, tables, where, order, extraColumns, parameters, subk, subv, sorts, set);
                        // as long as there are more add OR
                        if (subtermsIter.hasNext()) {
                          where.append(" OR");
                        }
                      }
                    }
                    // end the OR grouping
                    where.append(") AND");
                  } else {
                    // process a first level non-map value as an AND term

                      if (v instanceof Iterable<?>) {
                          for (Object vo : (Iterable<?>)v) {
                              set = processEntry(statementParts, tables, where, order, extraColumns, parameters, k, vo, sorts, set);
                              where.append(" AND");
                          }
                      } else {
                          set = processEntry(statementParts, tables, where, order, extraColumns, parameters, k, v, sorts, set);
                          where.append(" AND");
                      }
                  }
                } else if (!k.startsWith("_")) {
                  LOGGER.debug("Search on {}:{} filter dropped due to null value.", columnFamily, k);
                }
            } else {
              if (!k.startsWith("_")) {
                  LOGGER.warn("Search on {}:{} is not supported, filter dropped ",columnFamily,k);
              }
            }
        }
        if (where.length() == 0) {
            return new DisposableIterator<Map<String,Object>>() {

                private Disposer disposer;
                public boolean hasNext() {
                    return false;
                }

                public Map<String, Object> next() {
                    return null;
                }

                public void remove() {
                }

                public void close() {
                    if ( disposer != null ) {
                        disposer.unregisterDisposable(this);
                    }
                }
                public void setDisposer(Disposer disposer) {
                    this.disposer = disposer;
                }

            };
        }

        if (sorts[0] != null && order.length() == 0) {
          if (shouldFind(keySpace, columnFamily, sorts[0])) {
            String t = "a"+set;
            if ( statementParts.length > STMT_EXTRA_COLUMNS ) {
                extraColumns.append(MessageFormat.format(statementParts[STMT_EXTRA_COLUMNS], t));
            }
            tables.append(MessageFormat.format(statementParts[STMT_TABLE_JOIN], t));
            parameters.add(sorts[0]);
            where.append(MessageFormat.format(statementParts[STMT_WHERE_SORT], t)).append(" AND");
            order.append(MessageFormat.format(statementParts[STMT_ORDER], t, sorts[1]));
          } else {
            LOGGER.warn("Sort on {}:{} is not supported, sort dropped", columnFamily,
                sorts[0]);
          }
        }


        final String sqlStatement = MessageFormat.format(statementParts[STMT_BASE],
            tables.toString(), where.toString(), order.toString(), items, offset, extraColumns.toString());

        PreparedStatement tpst = null;
        ResultSet trs = null;
        try {
            LOGGER.debug("Preparing {} ", sqlStatement);
            tpst = client.getConnection().prepareStatement(sqlStatement);
            client.inc("iterator");
            tpst.clearParameters();
            int i = 1;
            for (Object params : parameters) {
                tpst.setObject(i, params);
                LOGGER.debug("Setting {} ", params);

                i++;
            }

            long qtime = System.currentTimeMillis();
            trs = tpst.executeQuery();
            qtime = System.currentTimeMillis() - qtime;
            if ( qtime > client.getSlowQueryThreshold() && qtime < client.getVerySlowQueryThreshold()) {
                JDBCStorageClient.SQL_LOGGER.warn("Slow Query {}ms {} params:[{}]",new Object[]{qtime,sqlStatement,Arrays.toString(parameters.toArray(new String[parameters.size()]))});
            } else if ( qtime > client.getVerySlowQueryThreshold() ) {
                JDBCStorageClient.SQL_LOGGER.error("Very Slow Query {}ms {} params:[{}]",new Object[]{qtime,sqlStatement,Arrays.toString(parameters.toArray(new String[parameters.size()]))});
            }
            client.inc("iterator r");
            LOGGER.debug("Executed ");

            // pass control to the iterator.
            final PreparedStatement pst = tpst;
            final ResultSet rs = trs;
            final ResultSetMetaData rsmd = rs.getMetaData();
            tpst = null;
            trs = null;
            return client.registerDisposable(new PreemptiveIterator<Map<String, Object>>() {

                private Map<String, Object> nextValue = Maps.newHashMap();
                private boolean open = true;

                @Override
                protected Map<String, Object> internalNext() {
                    return nextValue;
                }

                @Override
                protected boolean internalHasNext() {
                    try {
                        if (open && rs.next()) {
                            if ( rawResults ) {
                                Builder<String, Object> b = ImmutableMap.builder();
                                for  (int i = 1; i <= rsmd.getColumnCount(); i++ ) {
                                    b.put(String.valueOf(i), rs.getObject(i));
                                }
                                nextValue = b.build();
                            } else {
                               String id = rs.getString(1);
                               nextValue = client.internalGet(keySpace, columnFamily, id);
                               LOGGER.debug("Got Row ID {} {} ", id, nextValue);
                            }
                            return true;
                        }
                        close();
                        nextValue = null;
                        LOGGER.debug("End of Set ");
                        return false;
                    } catch (SQLException e) {
                        LOGGER.error(e.getMessage(), e);
                        close();
                        nextValue = null;
                        return false;
                    } catch (StorageClientException e) {
                        LOGGER.error(e.getMessage(), e);
                        close();
                        nextValue = null;
                        return false;
                    }
                }

                @Override
                public void close() {
                    if (open) {
                        open = false;
                        try {
                            if (rs != null) {
                                rs.close();
                                client.dec("iterator r");
                            }
                        } catch (SQLException e) {
                            LOGGER.warn(e.getMessage(), e);
                        }
                        try {
                            if (pst != null) {
                                pst.close();
                                client.dec("iterator");
                            }
                        } catch (SQLException e) {
                            LOGGER.warn(e.getMessage(), e);
                        }
                        super.close();
                    }

                }
            });
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new StorageClientException(e.getMessage() + " SQL Statement was " + sqlStatement,
                    e);
        } finally {
            // trs and tpst will only be non null if control has not been passed
            // to the iterator.
            try {
                if (trs != null) {
                    trs.close();
                    client.dec("iterator r");
                }
            } catch (SQLException e) {
                LOGGER.warn(e.getMessage(), e);
            }
            try {
                if (tpst != null) {
                    tpst.close();
                    client.dec("iterator");
                }
            } catch (SQLException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
    }


    /**
     * @param statementParts
     * @param where
     * @param params
     * @param k
     * @param v
     * @param t
     * @param conjunctionOr
     */
    private int processEntry(String[] statementParts, StringBuilder tables,
        StringBuilder where, StringBuilder order, StringBuilder extraColumns, List<Object> params, String k, Object v,
        String[] sorts, int set) {
      String t = "a" + set;
      tables.append(MessageFormat.format(statementParts[STMT_TABLE_JOIN], t));

      if (v instanceof Iterable<?>) {
        for (Iterator<?> vi = ((Iterable<?>) v).iterator(); vi.hasNext();) {
          Object viObj = vi.next();
          
          params.add(k);
          params.add(viObj);
          where.append(" (").append(MessageFormat.format(statementParts[STMT_WHERE], t)).append(")");

          // as long as there are more add OR
          if (vi.hasNext()) {
            where.append(" OR");
          }
        }
      } else {
        params.add(k);
        params.add(v);
        where.append(" (").append(MessageFormat.format(statementParts[STMT_WHERE], t)).append(")");
      }

      // add in sorting based on the table ref and value
      if (k.equals(sorts[0])) {
        order.append(MessageFormat.format(statementParts[STMT_ORDER], t, sorts[1]));
        if ( statementParts.length > STMT_EXTRA_COLUMNS ) {
            extraColumns.append(MessageFormat.format(statementParts[STMT_EXTRA_COLUMNS], t));
        }
      }
      return set+1;
    }

}
