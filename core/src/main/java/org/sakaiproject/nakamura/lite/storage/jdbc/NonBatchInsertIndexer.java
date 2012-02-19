package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sakaiproject.nakamura.api.lite.RemoveProperty;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class NonBatchInsertIndexer extends KeyValueIndexer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NonBatchInsertIndexer.class);

    public NonBatchInsertIndexer(JDBCStorageClient jdbcStorageClient, Set<String> indexColumns, Map<String, Object> sqlConfig) {
        super(jdbcStorageClient, indexColumns, sqlConfig);
    }

    public void index( Map<String, PreparedStatement> statementCache, String keySpace, String columnFamily, String key, String rid, Map<String, Object> values) throws StorageClientException, SQLException {
        String rowId = client.getDebugRowId(keySpace, columnFamily, key);
        for (Entry<String, Object> e : values.entrySet()) {
            String k = e.getKey();
            Object o = e.getValue();
            if (shouldIndex(keySpace, columnFamily, k)) {
                if (o instanceof RemoveProperty || o == null) {
                    PreparedStatement removeStringColumn = client.getStatement(keySpace,
                            columnFamily, JDBCStorageClient.SQL_REMOVE_STRING_COLUMN, rid, statementCache);
                    removeStringColumn.clearWarnings();
                    removeStringColumn.clearParameters();
                    removeStringColumn.setString(1, rid);
                    removeStringColumn.setString(2, k);
                    int nrows = removeStringColumn.executeUpdate();
                    if (nrows == 0) {
                        Map<String, Object> m = client.get(keySpace, columnFamily, key);
                        LOGGER.debug(
                                "Column Not present did not remove {} {} Current Column:{} ",
                                new Object[] { rowId , k, m });
                    } else {
                        LOGGER.debug("Removed Index {} {} {} ",
                                new Object[]{rowId, k, nrows});
                    }
                } else {
                    PreparedStatement removeStringColumn = client.getStatement(keySpace,
                            columnFamily, JDBCStorageClient.SQL_REMOVE_STRING_COLUMN, rid, statementCache);
                    removeStringColumn.clearWarnings();
                    removeStringColumn.clearParameters();
                    removeStringColumn.setString(1, rid);
                    removeStringColumn.setString(2, k);
                    int nrows = removeStringColumn.executeUpdate();
                    if (nrows == 0) {
                        Map<String, Object> m = client.get(keySpace, columnFamily, key);
                        LOGGER.debug(
                                "Column Not present did not remove {} {} Current Column:{} ",
                                new Object[] { rowId, k, m });
                    } else {
                        LOGGER.debug("Removed Index {} {} {} ",
                                new Object[]{rowId, k, nrows});
                    }
                    Object[] os = (o instanceof Object[]) ? (Object[]) o : new Object[] { o };
                    for (Object ov : os) {
                        String v = ov.toString();
                        PreparedStatement insertStringColumn = client.getStatement(keySpace,
                                columnFamily, JDBCStorageClient.SQL_INSERT_STRING_COLUMN, rid, statementCache);
                        insertStringColumn.clearWarnings();
                        insertStringColumn.clearParameters();
                        insertStringColumn.setString(1, v);
                        insertStringColumn.setString(2, rid);
                        insertStringColumn.setString(3, k);
                        LOGGER.debug("Non Batch Insert Index {} {}", k, v);
                        if (insertStringColumn.executeUpdate() == 0) {
                            throw new StorageClientException("Failed to save "
                                    + rowId + "  column:["
                                    + k + "] ");
                        } else {
                            LOGGER.debug("Inserted Index {} {} [{}]",
                                    new Object[] { rowId,
                                            k, v });
                        }
                    }
                }
            }
        }

        if (!StorageClientUtils.isRoot(key)) {
            String parent = StorageClientUtils.getParentObjectPath(key);
            String hash = client.rowHash(keySpace, columnFamily, parent);
            LOGGER.debug("Hash of {}:{}:{} is {} ", new Object[] { keySpace, columnFamily,
                    parent, hash });
            Map<String, Object> autoIndexMap = ImmutableMap.of(
                    Content.PARENT_HASH_FIELD, (Object) hash);
            for (Entry<String, Object> e : autoIndexMap.entrySet()) {
                String k = e.getKey();
                Object v = e.getValue();
                PreparedStatement removeStringColumn = client.getStatement(keySpace, columnFamily,
                        JDBCStorageClient.SQL_REMOVE_STRING_COLUMN, rid, statementCache);
                removeStringColumn.clearWarnings();
                removeStringColumn.clearParameters();
                removeStringColumn.setString(1, rid);
                removeStringColumn.setString(2, k);
                int nrows = removeStringColumn.executeUpdate();
                if (nrows == 0) {
                    Map<String, Object> m = client.get(keySpace, columnFamily, key);
                    LOGGER.debug(
                            "Column Not present did not remove {} {} Current Column:{} ",
                            new Object[] { rowId, k, m });
                } else {
                    LOGGER.debug(
                            "Removed Index {} {} {} ",
                            new Object[] { rowId, k, nrows });
                }

                PreparedStatement insertStringColumn = client.getStatement(keySpace, columnFamily,
                        JDBCStorageClient.SQL_INSERT_STRING_COLUMN, rid, statementCache);
                insertStringColumn.clearWarnings();
                insertStringColumn.clearParameters();
                insertStringColumn.setString(1, v.toString());
                insertStringColumn.setString(2, rid);
                insertStringColumn.setString(3, k);
                LOGGER.debug("Non Batch Insert Index {} {}", k, v);
                if (insertStringColumn.executeUpdate() == 0) {
                    throw new StorageClientException("Failed to save "
                            + rowId + "  column:[" + k
                            + "] ");
                } else {
                    LOGGER.debug("Inserted Index {} {} [{}]",
                            new Object[] { rowId, k, v });
                }
            }
        }

    }


}
