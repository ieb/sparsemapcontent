package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.RemoveProperty;
import org.sakaiproject.nakamura.api.lite.StorageClientException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;

public class WideColumnIndexer implements Indexer {

    private JDBCStorageClient client;
    private Map<String, String> indexColumnsNames;
    private Map<String, String> indexColumnsTypes;

    public WideColumnIndexer(JDBCStorageClient jdbcStorageClient,
            Map<String, String> indexColumnsNames, Set<String> indexColumnTypes) {
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
                if (client.shouldIndex(keySpace, columnFamily, k)) {
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
                    "exists-widestring-row", rid, statementCache);
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
                            columnFamily, "delete-widestring-row", rid, statementCache);
                    deleteWideStringColumn.clearParameters();
                    deleteWideStringColumn.setString(1, rid);
                    deleteWideStringColumn.execute();
                } else {
                    //
                    // build an update query, record does not exists, but there
                    // is stuff to add
                    String[] sqlParts = StringUtils.split(client.getSql(keySpace, columnFamily,
                            "update-widestring-row"));
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
                        setOperations.append(MessageFormat.format(sqlParts[2],
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
                        client.getSql(keySpace, columnFamily, "insert-widestring-row"),
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

    private boolean isColumnArray(String keySpace, String columnFamily, String k) {
        String type = indexColumnsTypes.get(columnFamily + ":" + k);
        if (type != null && type.endsWith("[]")) {
            return true;
        }
        return false;
    }

}
