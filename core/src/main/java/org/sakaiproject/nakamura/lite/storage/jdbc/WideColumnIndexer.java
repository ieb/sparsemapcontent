package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.CacheHolder;
import org.sakaiproject.nakamura.api.lite.RemoveProperty;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.StorageConstants;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.lite.storage.spi.CachableDisposableIterator;
import org.sakaiproject.nakamura.lite.storage.spi.DirectCacheAccess;
import org.sakaiproject.nakamura.lite.storage.spi.DisposableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class WideColumnIndexer extends AbstractIndexer implements CachingIndexer {

    private static final String SQL_INSERT_WIDESTRING_ROW = "insert-widestring-row";
    private static final String SQL_UPDATE_WIDESTRING_ROW = "update-widestring-row";
    private static final String SQL_DELETE_WIDESTRING_ROW = "delete-widestring-row";
    private static final String SQL_EXISTS_WIDESTRING_ROW = "exists-widestring-row";
    private static final int SQL_QUERY_TEMPLATE_PART = 0;
    private static final int SQL_WHERE_PART = 1;
    private static final int SQL_WHERE_ARRAY_PART = 2;
    private static final int SQL_WHERE_ARRAY_WHERE_PART = 3;
    private static final int SQL_SORT_CLAUSE_PART = 4;
    private static final int SQL_SORT_LIST_PART = 5;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WideColumnIndexer.class);
    /**
     * Values to exclude from the query cache key.
     */
    private static final Set<String> EXCLUDE_CACHE_KEYS = ImmutableSet.of(StorageConstants.CACHEABLE);
    
    /**
     * Set of keys in query properties that cant be cached, normally because invalidation is too hard. 
     */
    private static final Set<String> DONT_CACHE_KEYS = ImmutableSet.of(StorageConstants.ITEMS, StorageConstants.PAGE, StorageConstants.SORT);
    private JDBCStorageClient client;
    private Map<String, String> indexColumnsNames;
    private Map<String, String> indexColumnsTypes;
    private int gets;

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
                        if (o instanceof RemoveProperty || o == null || valueMembers.length == 0) {
                            removeArrayColumns.add(k);
                        } else {
                            removeArrayColumns.add(k);
                            updateArrayColumns.put(k, valueMembers);
                        }
                    } else {
                        if (o instanceof RemoveProperty || o == null || valueMembers.length == 0) {
                            removeColumns.add(k);
                        } else {
                            updateColumns.put(k, valueMembers[0]);
                        }

                    }
                }
            }
            
            if (!StorageClientUtils.isRoot(key) 
                    && getColumnName(keySpace, columnFamily, Content.PARENT_HASH_FIELD) != null) {
                String parent = StorageClientUtils.getParentObjectPath(key);
                String hash = client.rowHash(keySpace, columnFamily, parent);
                LOGGER.debug("Hash of {}:{}:{} is {} ", new Object[] { keySpace, columnFamily,
                        parent, hash });
                updateColumns.put(Content.PARENT_HASH_FIELD, hash);
                invalidate(keySpace, columnFamily, ImmutableMap.of(Content.PARENT_HASH_FIELD, (Object) hash));
            }

            
            LOGGER.debug("Removing Array {} ",removeArrayColumns);
            LOGGER.debug("Updating Array {} ",updateArrayColumns);
            LOGGER.debug("Removing  {} ",removeColumns);
            LOGGER.debug("Updating  {} ",updateColumns);

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
                LOGGER.debug("Removing {} {} ",rid,column);
                nbatch++;
            }
            if (nbatch > 0) {
                long t = System.currentTimeMillis();
                removeStringColumn.executeBatch();
                checkSlow(t, client.getSql(keySpace, columnFamily, JDBCStorageClient.SQL_REMOVE_STRING_COLUMN));
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
                    insertStringColumn.addBatch();
                    LOGGER.debug("Inserting {} {} {} ",new Object[]{o.toString(),rid,e.getKey()});
                    nbatch++;
                }
            }
            if (nbatch > 0) {
                long t = System.currentTimeMillis();
                insertStringColumn.executeBatch();
                checkSlow(t, client.getSql(keySpace, columnFamily, JDBCStorageClient.SQL_INSERT_STRING_COLUMN));
                nbatch = 0;
            }
            if (removeColumns.size() == 0 && updateColumns.size() == 0) {
                return; // nothing to add or remove, do nothing.
            }

            if (removeColumns.size() > 0 && updateColumns.size() == 0) {
                // exists, columns to remove, none to update, therefore
                // delete row this assumes that the starting point is a
                // complete map
                PreparedStatement deleteWideStringColumn = client.getStatement(keySpace,
                        columnFamily, SQL_DELETE_WIDESTRING_ROW, rid, statementCache);
                deleteWideStringColumn.clearParameters();
                deleteWideStringColumn.setString(1, rid);
                long t = System.currentTimeMillis();
                deleteWideStringColumn.execute();
                checkSlow(t, client.getSql(keySpace, columnFamily, SQL_DELETE_WIDESTRING_ROW));
                LOGGER.debug("Executed {} with {} ",deleteWideStringColumn, rid);
            } else if ( updateColumns.size() > 0 || removeColumns.size() > 0) {
                //
                // build an update query, record does not exists, but there
                // is stuff to add
                String[] sqlParts = StringUtils.split(client.getSql(keySpace, columnFamily,
                        SQL_UPDATE_WIDESTRING_ROW),";");
                StringBuilder setOperations = new StringBuilder();
                for (Entry<String, Object> e : updateColumns.entrySet()) {
                    join(setOperations," ,").append(MessageFormat.format(sqlParts[1],
                            getColumnName(keySpace, columnFamily, e.getKey())));
                }
                for (String toRemove : removeColumns) {
                    join(setOperations," ,").append(MessageFormat.format(sqlParts[1],
                            getColumnName(keySpace, columnFamily, toRemove)));
                }
                String finalSql = MessageFormat.format(sqlParts[0], setOperations);
                LOGGER.debug("Performing {} ",finalSql);
                PreparedStatement updateColumnPst = client.getStatement(finalSql,
                        statementCache);
                updateColumnPst.clearWarnings();
                updateColumnPst.clearParameters();
                int i = 1;
                for (Entry<String, Object> e : updateColumns.entrySet()) {
                    updateColumnPst.setString(i, e.getValue().toString());
                    LOGGER.debug("   Param {} {} ",i,e.getValue().toString());
                    i++;
                }
                for (String toRemove : removeColumns) {
                    updateColumnPst.setNull(i, toSqlType(columnFamily, toRemove));
                    LOGGER.debug("   Param {} NULL ",i);
                    i++;
                }
                LOGGER.debug("   Param {} {} ",i, rid);
                updateColumnPst.setString(i, rid);
                long t = System.currentTimeMillis();
                int n = updateColumnPst.executeUpdate();
                checkSlow(t, finalSql);
                if ( n == 0  ) {
                    // part 0 is the final ,part 1 is the template for column names,
                    // part 2 is the template for parameters.
                    // insert into x ( columnsnames ) values ()
                    StringBuilder columnNames = new StringBuilder();
                    StringBuilder paramHolders = new StringBuilder();
                    for (Entry<String, Object> e : updateColumns.entrySet()) {
                        columnNames.append(" ,").append(getColumnName(keySpace, columnFamily, e.getKey()));
                        paramHolders.append(" ,").append("?");
                    }
                    finalSql = MessageFormat.format(
                            client.getSql(keySpace, columnFamily, SQL_INSERT_WIDESTRING_ROW),
                            columnNames.toString(), paramHolders.toString());
                    LOGGER.debug("Insert SQL {} ",finalSql);
                    PreparedStatement insertColumnPst = client.getStatement(finalSql, statementCache);
                    insertColumnPst.clearWarnings();
                    insertColumnPst.clearParameters();
                    insertColumnPst.setString(1, rid);
                    LOGGER.debug("   Param 1 {} ",rid);
                    i = 2;
                    for (Entry<String, Object> e : updateColumns.entrySet()) {
                        LOGGER.debug("   Param {} {} ",i,e.getValue().toString());
                        insertColumnPst.setString(i, e.getValue().toString());
                        i++;
                    }
                    t = System.currentTimeMillis();
                    insertColumnPst.executeUpdate();
                    checkSlow(t, finalSql);
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

    }

    private void checkSlow(long t, String sql) {
        t = System.currentTimeMillis() - t;
        if ( t > 100 ) {
            JDBCStorageClient.SQL_LOGGER.info("Slow Query {} {} ",t, sql);
        }        
    }

    private String getColumnName(String keySpace, String columnFamily, String key) {
        return indexColumnsNames.get(columnFamily + ":" + key); 
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

    public DisposableIterator<Map<String, Object>> find(final String keySpace, final String columnFamily,
            Map<String, Object> properties, final DirectCacheAccess cachingManager) throws StorageClientException {
        
        final boolean rawResults = properties != null && properties.containsKey(StorageConstants.RAWRESULTS);
        
        DisposableIterator<Map<String, Object>> cachedIterator = cachedFind(keySpace, columnFamily, properties, rawResults, cachingManager);
        if ( cachedIterator != null ) {
            return cachedIterator;
        }
        
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
        
        

        String sql = client.getSql(keys);
        if (sql == null) {
            throw new StorageClientException("Failed to locate SQL statement for any of  "
                    + Arrays.toString(keys));
        }


        // collect information on paging
        long page = 0;
        long items = 25;
        String sortProp = null;
        if (properties != null) {
          if (properties.containsKey(StorageConstants.PAGE)) {
            page = Long.valueOf(String.valueOf(properties.get(StorageConstants.PAGE)));
          }
          if (properties.containsKey(StorageConstants.ITEMS)) {
            items = Long.valueOf(String.valueOf(properties.get(StorageConstants.ITEMS)));
          }
          sortProp = (String) properties.get(StorageConstants.SORT);
        }
        long offset = page * items;

        // collect information on sorting
        List<String> sortingList = Lists.newArrayList();
        if (sortProp != null) {
          String[] sorts = StringUtils.split(sortProp);
          if (sorts.length == 1) {
              if ( shouldIndex(keySpace, columnFamily, sorts[0]) && !isColumnArray(keySpace, columnFamily, sorts[0]) ) {
                  sortingList.add(getColumnName(keySpace, columnFamily, sorts[0]));
                  sortingList.add("asc");
              }
          } else if (sorts.length > 1) {
              for ( int i = 0; i < sorts.length; i+=2) {
                  if ( shouldIndex(keySpace, columnFamily, sorts[0]) && !isColumnArray(keySpace, columnFamily, sorts[i]) ) {
                      sortingList.add(getColumnName(keySpace, columnFamily, sorts[0]));
                      sortingList.add(sorts[i+1]);
                  }
              }
          }
        }
        String[] sorts = sortingList.toArray(new String[sortingList.size()]);
        String[] statementParts = StringUtils.split(sql, ';');
        /*
         * Part 0 basic SQL template; {0} is the where clause {1} is the sort clause {2} is the from {3} is the to record
         *   eg select rid from css where {0} {1} LIMIT {2} ROWS {3}
         * Part 1 where clause for non array matches; {0} is the columnName
         *   eg {0} = ?
         * Part 2 where clause for array matches (not possible to sort on array matches) {0} is the table alias, {1} is the where clause
         *   eg rid in ( select {0}.rid from css {0} where {1} )
         * Part 3 the where clause for array matches {0} is the table alias
         *   eg {0}.cid = ? and {0}.v = ?  
         * Part 3 sort clause {0} is the list to sort by
         *   eg sort by {0}
         * Part 4 sort elements, {0} is the column, {1} is the order
         *   eg {0} {1}
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
                          set = processEntry(statementParts, keySpace, columnFamily, subQuery, parameters, subk, subv, sorts, set, " OR ");
                        }
                      }
                      if ( subQuery.length() > 0 ) {
                          join(whereClause," AND ").append("( ").append(subQuery.toString()).append(" ) ");
                      }
                  } else {
                    // process a first level non-map value as an AND term

                      if (v instanceof Iterable<?>) {
                          for (Object vo : (Iterable<?>)v) {
                              set = processEntry(statementParts, keySpace, columnFamily, whereClause, parameters, k, vo, sorts, set, " AND ");
                          }
                      } else {
                          set = processEntry(statementParts, keySpace, columnFamily, whereClause, parameters, k, v, sorts, set, " AND ");
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
        // there was no where clause generated
        // to avoid returneing everything, we wont return anything.
        if (whereClause.length() == 0) {
            return cacheResults(
                    keySpace,
                    columnFamily,
                    properties,
                    new PreemptiveCachedMapIterator(client, keySpace, columnFamily, ImmutableMap.of("rows",
                            (Object) ImmutableList.of()),  rawResults, cachingManager));
        }

        StringBuilder sortClause = new StringBuilder();
        if ( statementParts.length > SQL_SORT_CLAUSE_PART ) {
            StringBuilder sortList = new StringBuilder();
            for ( int i = 0; i < sorts.length; i+= 2) {
                if (shouldFind(keySpace, columnFamily, sorts[0])) {
                    join(sortList, ", ").append(MessageFormat.format(statementParts[SQL_SORT_LIST_PART], sorts[i], sorts[i+1]));
                }
            }
            if ( sortList.length() > 0 ) {
                sortClause.append(MessageFormat.format(statementParts[SQL_SORT_CLAUSE_PART], sortList.toString()));
            }
        }

        final String sqlStatement = MessageFormat.format(statementParts[SQL_QUERY_TEMPLATE_PART],
            whereClause.toString(), sortClause.toString(), items, offset);

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
            PreparedStatement pst = tpst;
            ResultSet rs = trs;
            tpst = null;
            trs = null;
            return client.registerDisposable(cacheResults(keySpace, columnFamily, properties, new PreemptiveCachedMapIterator(client, keySpace, columnFamily, rs, pst, rawResults, cachingManager)));
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
    private int processEntry(String[] statementParts, String keySpace, String columnFamily, StringBuilder subQuery,
            List<Object> params, String key, Object value, String[] sorts, int tableIndex,
            String logicalJoin) {
        if ( isColumnArray(keySpace, columnFamily, key)) {
            String tableName = "a"+tableIndex;
            tableIndex++;
            if (value instanceof Iterable<?>) {
                StringBuilder arraySubQuery = new StringBuilder();
                // SQL_WHERE_ARRAY_WHERE_PART is ( {0}cid = ? AND {0}v = ? ) 
                for (Iterator<?> valueIterator = ((Iterable<?>) value).iterator(); valueIterator.hasNext();) {
                    Object o = valueIterator.next();
                    params.add(key);
                    params.add(o);
                    join(arraySubQuery, " OR ").append(MessageFormat.format(statementParts[SQL_WHERE_ARRAY_WHERE_PART],tableName));                    
                }
                // SQL_WHERE_ARRAY_PART is rid in (select rid from css {0} where {1} )
                if ( arraySubQuery.length() > 0 ) {
                    join(subQuery, logicalJoin).append(MessageFormat.format(statementParts[SQL_WHERE_ARRAY_PART], tableName, arraySubQuery));
                }
            } else {
                params.add(key);
                params.add(value);
                String whereClause = MessageFormat.format(statementParts[SQL_WHERE_ARRAY_WHERE_PART],tableName);
                join(subQuery, logicalJoin).append(MessageFormat.format(statementParts[SQL_WHERE_ARRAY_PART], tableName, whereClause));
            }
        } else {
            String column = getColumnName(keySpace, columnFamily, key);
            if (value instanceof Iterable<?>) {
                StringBuilder arraySubQuery = new StringBuilder();
                // SQL_WHERE_PART is {0} = ?
                for (Iterator<?> valueIterator = ((Iterable<?>) value).iterator(); valueIterator.hasNext();) {
                  Object o = valueIterator.next();
                  params.add(o);
                  join(arraySubQuery, " OR ").append(MessageFormat.format(statementParts[SQL_WHERE_PART], column));
                }
                if ( arraySubQuery.length() > 0 ) {
                    join(subQuery, logicalJoin).append(" ( ").append(arraySubQuery.toString()).append(" ) ");
                }
              } else {
                params.add(value);
                LOGGER.debug("Adding {} {} ",statementParts[SQL_WHERE_PART],column);
                join(subQuery, logicalJoin).append(MessageFormat.format(statementParts[SQL_WHERE_PART], column));
              }
            
        }
        return tableIndex;

    }

    
    // Query Caching -----------------------------------------------------------------------------------
    
    
    @Override
    public void invalidate(String keySpace, String columnFamily, Map<String, Object> queryProperties) {
        Map<String, CacheHolder> queryCache = client.getQueryCache();
        if ( queryCache == null ) {
            return;
        }
        String cacheKey = getCacheKey(keySpace, columnFamily, queryProperties);
        if ( cacheKey != null ) {
            queryCache.remove(cacheKey);
        }
    }
    
    private String getCacheKey(String keySpace, String columnFamily,
            Map<String, Object> queryProperties) {
        List<String> keys = Lists.newArrayList(queryProperties.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        sb.append(keySpace).append(";").append(columnFamily);
        boolean hasKey = false;
        for ( String key : keys ) {
            if ( DONT_CACHE_KEYS.contains(key)) {
                hasKey = false;
                LOGGER.debug("Query cant be cached becuase it contains {} ",key);
                break;
            }
            if ( !EXCLUDE_CACHE_KEYS.contains(key) ) {
                sb.append(key).append(":").append(queryProperties.get(key)).append(";");
                hasKey = true;
            }
        }
        if ( hasKey ) {
            // we might want to hash this to prevent the key getting massive.
            return sb.toString();
        }
        return null;
    }

    private DisposableIterator<Map<String, Object>> cachedFind(String keySpace,
            String columnFamily, Map<String, Object> queryProperties, boolean rawResults, DirectCacheAccess cachingManager) {
        Map<String, CacheHolder> queryCache = client.getQueryCache();
        if ( queryCache == null ) {
            gets++;
            if ( gets % 1000 == 0 ) {
                LOGGER.info("No Query Cache in use, please provide one.");
            }
            return null;
        }
        String cacheKey = getCacheKey(keySpace, columnFamily, queryProperties);
        if ( cacheKey != null ) {
            CacheHolder ch = queryCache.get(cacheKey);
            if ( ch != null ) {
                Map<String, Object> m = ch.get();
                if ( m != null ) {
                    return new PreemptiveCachedMapIterator(client, keySpace, columnFamily, m, rawResults, cachingManager);
                }
            }
        }
        return null;
    }
    


    private DisposableIterator<Map<String, Object>> cacheResults(String keySpace,
            String columnFamily, Map<String, Object> queryProperties,
            CachableDisposableIterator<Map<String, Object>> disposableIterator) {
        Map<String, CacheHolder> queryCache = client.getQueryCache();
        if ( queryCache == null ) {
            return disposableIterator;
        }
        String cacheKey = getCacheKey(keySpace, columnFamily, queryProperties);
        if ( cacheKey != null ) {
            Map<String, Object> m = disposableIterator.getResultsMap();
            if ( m != null ) {
                queryCache.put(cacheKey, new CacheHolder(m));
            }
        }
        return disposableIterator;
    }




}
