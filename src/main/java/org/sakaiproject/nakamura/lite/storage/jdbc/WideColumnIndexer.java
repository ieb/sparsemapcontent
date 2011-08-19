package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.core.IsAnything;
import org.sakaiproject.nakamura.api.lite.RemoveProperty;
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
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;

public class WideColumnIndexer extends AbstractIndexer {

    private static final String SQL_INSERT_WIDESTRING_ROW = "insert-widestring-row";
    private static final String SQL_UPDATE_WIDESTRING_ROW = "update-widestring-row";
    private static final String SQL_DELETE_WIDESTRING_ROW = "delete-widestring-row";
    private static final String SQL_EXISTS_WIDESTRING_ROW = "exists-widestring-row";
    private static final Logger LOGGER = LoggerFactory.getLogger(WideColumnIndexer.class);
    private JDBCStorageClient client;
    private Map<String, String> indexColumnsNames;
    private Map<String, String> indexColumnsTypes;

    public WideColumnIndexer(JDBCStorageClient jdbcStorageClient,
            Map<String, String> indexColumnsNames, Set<String> indexColumnTypes, Map<String, Object> sqlConfig) {
        super(indexColumnsNames.keySet());
        this.client = jdbcStorageClient;
        this.indexColumnsNames = indexColumnsNames;
        Builder<String, String> b = ImmutableMap.builder();
        for (String k : Sets.union(indexColumnTypes, JDBCStorageClient.AUTO_INDEX_COLUMNS_TYPES)) {
            String[] type = StringUtils.split(k,"=",2);
            b.put(type[0], type[1]);
        }
        this.indexColumnsTypes = b.build();
    }

    public void index(Map<String, PreparedStatement> statementCache, String keySpace,
            String columnFamily, String key, String rid, Map<String, Object> values)
            throws StorageClientException, SQLException {
        ResultSet rs = null;

        try {
            Set<String> removeArrayColumns = Sets.newHashSet();
            Set<String> removeColumns = Sets.newHashSet();
            Map<String, Object[]> updateArrayColumns = Maps.newHashMap();
            Map<String, Object> updateColumns = Maps.newHashMap();
            for (Entry<String, Object> e : values.entrySet()) {
                String k = e.getKey();
                Object o = e.getValue();
                Object[] valueMembers = (o instanceof Object[]) ? (Object[]) o : new Object[] { o };
                if (shouldIndex(keySpace, columnFamily, k)) {
                    if (isColumnArray(keySpace, columnFamily, k)) {
                        if (o instanceof RemoveProperty || o == null) {
                            removeArrayColumns.add(k);
                        } else {
                            removeArrayColumns.add(k);
                            updateArrayColumns.put(k, valueMembers);
                        }
                    } else {
                        if (o instanceof RemoveProperty || o == null) {
                            removeColumns.add(k);
                        } else {
                            updateColumns.put(k, valueMembers[0]);
                        }

                    }
                }
            }

            // arrays are stored in css, so we can re-use css sql.
            PreparedStatement removeStringColumn = client.getStatement(keySpace, columnFamily,
                    JDBCStorageClient.SQL_REMOVE_STRING_COLUMN, rid, statementCache);
            int nbatch = 0;
            for (String column : removeArrayColumns) {
                removeStringColumn.clearWarnings();
                removeStringColumn.clearParameters();
                removeStringColumn.setString(1, rid);
                removeStringColumn.setString(2, column);
                removeStringColumn.addBatch();
                nbatch++;
            }
            if (nbatch > 0) {
                removeStringColumn.executeBatch();
                nbatch = 0;
            }

            // add the column values in
            PreparedStatement insertStringColumn = client.getStatement(keySpace, columnFamily,
                    JDBCStorageClient.SQL_INSERT_STRING_COLUMN, rid, statementCache);
            for (Entry<String, Object[]> e : updateArrayColumns.entrySet()) {
                for (Object o : e.getValue()) {
                    insertStringColumn.clearWarnings();
                    insertStringColumn.clearParameters();
                    insertStringColumn.setString(1, o.toString());
                    insertStringColumn.setString(2, rid);
                    insertStringColumn.setString(3, e.getKey());
                    nbatch++;
                }
            }
            if (nbatch > 0) {
                insertStringColumn.executeBatch();
                nbatch = 0;
            }
            if (removeColumns.size() == 0 && updateColumns.size() == 0) {
                return; // nothing to add or remove, do nothing.
            }

            // now update the wide column.
            PreparedStatement wideColumnExists = client.getStatement(keySpace, columnFamily,
                    SQL_EXISTS_WIDESTRING_ROW, rid, statementCache);
            wideColumnExists.clearParameters();
            wideColumnExists.setString(1, rid);
            rs = wideColumnExists.executeQuery();
            boolean exists = rs.next();
            if (exists) {
                if (removeColumns.size() > 0 && updateArrayColumns.size() == 0) {
                    // exists, columns to remove, none to update, therefore
                    // delete row this assumes that the starting point is a
                    // complete map
                    PreparedStatement deleteWideStringColumn = client.getStatement(keySpace,
                            columnFamily, SQL_DELETE_WIDESTRING_ROW, rid, statementCache);
                    deleteWideStringColumn.clearParameters();
                    deleteWideStringColumn.setString(1, rid);
                    deleteWideStringColumn.execute();
                } else {
                    //
                    // build an update query, record does not exists, but there
                    // is stuff to add
                    String[] sqlParts = StringUtils.split(client.getSql(keySpace, columnFamily,
                            SQL_UPDATE_WIDESTRING_ROW));
                    StringBuilder setOperations = new StringBuilder();
                    for (Entry<String, Object> e : updateColumns.entrySet()) {
                        if (setOperations.length() > 0) {
                            setOperations.append(" ,");
                        }
                        setOperations.append(MessageFormat.format(sqlParts[1],
                                indexColumnsNames.get(columnFamily + ":" + e.getKey())));
                    }
                    for (String toRemove : removeColumns) {
                        if (setOperations.length() > 0) {
                            setOperations.append(" ,");
                        }
                        setOperations.append(MessageFormat.format(sqlParts[1],
                                indexColumnsNames.get(columnFamily + ":" + toRemove)));
                    }
                    String finalSql = MessageFormat.format(sqlParts[0], setOperations);
                    PreparedStatement updateColumnPst = client.getStatement(finalSql,
                            statementCache);
                    updateColumnPst.clearWarnings();
                    updateColumnPst.clearParameters();
                    int i = 1;
                    for (Entry<String, Object> e : updateColumns.entrySet()) {
                        updateColumnPst.setString(i, e.getValue().toString());
                        i++;
                    }
                    for (String toRemove : removeColumns) {
                        updateColumnPst.setNull(i, toSqlType(columnFamily, toRemove));
                        i++;
                    }
                    updateColumnPst.setString(i, rid);
                    updateColumnPst.executeUpdate();
                }
            } else if (updateArrayColumns.size() > 0) {
                // part 0 is the final ,part 1 is the template for column names,
                // part 2 is the template for parameters.
                // insert into x ( columnsnames ) values ()
                StringBuilder columnNames = new StringBuilder();
                StringBuilder paramHolders = new StringBuilder();
                for (Entry<String, Object> e : updateColumns.entrySet()) {
                    if (columnNames.length() > 0) {
                        columnNames.append(" ,");
                        paramHolders.append(" ,");
                    }
                    columnNames.append(indexColumnsNames.get(columnFamily + ":" + e.getKey()));
                    paramHolders.append("?");
                }
                String finalSql = MessageFormat.format(
                        client.getSql(keySpace, columnFamily, SQL_INSERT_WIDESTRING_ROW),
                        columnNames, paramHolders);
                PreparedStatement insertColumnPst = client.getStatement(finalSql, statementCache);
                insertColumnPst.clearWarnings();
                insertColumnPst.clearParameters();
                insertColumnPst.setString(1, rid);
                int i = 2;
                for (Entry<String, Object> e : updateColumns.entrySet()) {
                    insertColumnPst.setString(i, e.getValue().toString());
                    i++;
                }
                insertColumnPst.executeUpdate();

            }

        } finally {
            if (rs != null) {
                rs.close();
            }
        }

    }

    private int toSqlType(String columnFamily, String k) {
        String type = indexColumnsTypes.get(columnFamily+":"+k);
        if ( type == null ) {
            return Types.VARCHAR;
        } else if (type.startsWith("String")) {
            return Types.VARCHAR;
        } else if (type.startsWith("int")) {
            return Types.INTEGER;
        } else if (type.startsWith("Date")) {
            return Types.DATE;
        }
        return Types.VARCHAR;
    }

    private boolean isColumnArray(String keySpace, String columnFamily, String k) {
        String type = indexColumnsTypes.get(columnFamily + ":" + k);
        if (type != null && type.endsWith("[]")) {
            return true;
        }
        return false;
    }

    public DisposableIterator<Map<String, Object>> find(String keySpace, String columnFamily,
            Map<String, Object> properties) throws StorageClientException {
        String[] keys = null;
        if ( properties != null  && properties.containsKey(StorageConstants.CUSTOM_STATEMENT_SET)) {
            String customStatement = (String) properties.get(StorageConstants.CUSTOM_STATEMENT_SET);
            keys = new String[] { 
                    "wide-"+ customStatement+ "." + keySpace + "." + columnFamily,
                    "wide-" + customStatement +  "." + columnFamily, 
                    "wide-" + customStatement, 
                    "wide-block-find." + keySpace + "." + columnFamily,
                    "wide-block-find." + columnFamily, 
                    "wide-block-find" 
           };            
        } else {
            keys = new String[] { "wide-block-find." + keySpace + "." + columnFamily,
                    "wide-block-find." + columnFamily, "wide-block-find" };            
        }
        
        final boolean rawResults = properties != null && properties.containsKey(StorageConstants.RAWRESULTS);

        String sql = client.getSql(keys);
        if (sql == null) {
            throw new StorageClientException("Failed to locate SQL statement for any of  "
                    + Arrays.toString(keys));
        }


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

        String[] statementParts = StringUtils.split(sql, ';');
        /*
         * Part 1 basic SQL template; {0} is the where clause {1} is the sort clause {2} is the from {3} is the to record
         * Part 2 where clause for non array matches; {0} is the columnName
         * Part 3 where clause for array matches (not possible to sort on array matches) {0} is the column name
         * Part 4 sort clause {0} is the list to sort by
         * Part 5 sort elements, {0} is the column, {1} is the order
         * Dont include , AND or OR, the code will add those as appropriate. 
         */

        StringBuilder whereClause = new StringBuilder();
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
                      @SuppressWarnings("unchecked")
                      Set<Entry<String, Object>> subterms = ((Map<String, Object>) v).entrySet();
                      StringBuilder subQuery = new StringBuilder();
                      for(Iterator<Entry<String, Object>> subtermsIter = subterms.iterator(); subtermsIter.hasNext();) {
                        Entry<String, Object> subterm = subtermsIter.next();
                        String subk = subterm.getKey();
                        Object subv = subterm.getValue();
                        // check that each subterm should be indexed
                        if (shouldFind(keySpace, columnFamily, subk)) {
                          set = processEntry(statementParts, subQuery, parameters, subk, subv, sorts, set, " OR ");
                        }
                      }
                      if ( subQuery.length() > 0 ) {
                          join(whereClause," AND ").append("( ").append(subQuery.toString()).append(" ) ");
                      }
                  } else {
                    // process a first level non-map value as an AND term

                      if (v instanceof Iterable<?>) {
                          for (Object vo : (Iterable<?>)v) {
                              set = processEntry(statementParts, whereClause, parameters, k, vo, sorts, set, " AND ");
                          }
                      } else {
                          set = processEntry(statementParts, whereClause, parameters, k, v, sorts, set, " AND ");
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
        if (whereClause.length() == 0) {
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
            if ( qtime > slowQueryThreshold && qtime < verySlowQueryThreshold) {
                JDBCStorageClient.SQL_LOGGER.warn("Slow Query {}ms {} params:[{}]",new Object[]{qtime,sqlStatement,Arrays.toString(parameters.toArray(new String[parameters.size()]))});
            } else if ( qtime > verySlowQueryThreshold ) {
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


    private StringBuilder join(StringBuilder sb, String joinWord) {
        if ( sb.length() > 0 ) {
            sb.append(joinWord);
        }
        return sb;
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
