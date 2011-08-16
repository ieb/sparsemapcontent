package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.sakaiproject.nakamura.api.lite.RemoveProperty;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.lite.content.InternalContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class BatchInsertIndexer extends KeyValueIndexer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchInsertIndexer.class);

    public BatchInsertIndexer(JDBCStorageClient jdbcStorageClient, Set<String> indexColumns, Map<String, Object> sqlConfig) {
        super(jdbcStorageClient, indexColumns, sqlConfig);
    }

    public void index(Map<String, PreparedStatement> statementCache, String keySpace, String columnFamily, String key, String rid, Map<String, Object> values) throws StorageClientException, SQLException {
        Set<PreparedStatement> removeSet = Sets.newHashSet();
        // execute the updates and add the necessary inserts.
        Map<PreparedStatement, List<Entry<String, Object>>> insertSequence = Maps
                .newHashMap();

        Set<PreparedStatement> insertSet = Sets.newHashSet();

        for (Entry<String, Object> e : values.entrySet()) {
            String k = e.getKey();
            Object o = e.getValue();
            if (shouldIndex(keySpace, columnFamily, k)) {
                if ( o instanceof RemoveProperty || o == null ) {
                    PreparedStatement removeStringColumn = client.getStatement(keySpace,
                            columnFamily, JDBCStorageClient.SQL_REMOVE_STRING_COLUMN, rid, statementCache);
                    removeStringColumn.setString(1, rid);
                    removeStringColumn.setString(2, k);
                    removeStringColumn.addBatch();
                    removeSet.add(removeStringColumn);
                } else {
                    // remove all previous values
                    PreparedStatement removeStringColumn = client.getStatement(keySpace,
                            columnFamily, JDBCStorageClient.SQL_REMOVE_STRING_COLUMN, rid, statementCache);
                    removeStringColumn.setString(1, rid);
                    removeStringColumn.setString(2, k);
                    removeStringColumn.addBatch();
                    removeSet.add(removeStringColumn);
                    // insert new values, as we just removed them we know we can insert, no need to attempt update
                    // the only thing that we know is the colum value changes so we have to re-index the whole
                    // property
                    Object[] valueMembers = (o instanceof Object[]) ? (Object[]) o : new Object[] { o };
                    for (Object ov : valueMembers) {
                        String valueMember = ov.toString();
                        PreparedStatement insertStringColumn = client.getStatement(keySpace,
                            columnFamily, JDBCStorageClient.SQL_INSERT_STRING_COLUMN, rid, statementCache);
                        insertStringColumn.setString(1, valueMember);
                        insertStringColumn.setString(2, rid);
                        insertStringColumn.setString(3, k);
                        insertStringColumn.addBatch();
                        LOGGER.debug("Insert Index {} {}", k, valueMember);
                        insertSet.add(insertStringColumn);
                        List<Entry<String, Object>> insertSeq = insertSequence
                        .get(insertStringColumn);
                        if (insertSeq == null) {
                          insertSeq = Lists.newArrayList();
                          insertSequence.put(insertStringColumn, insertSeq);
                        }
                        insertSeq.add(e);
                    }
                }
            }
        }

        if ( !StorageClientUtils.isRoot(key)) {
            // create a holding map containing a rowhash of the parent and then process the entry to generate a update operation.
            Map<String, Object> autoIndexMap = ImmutableMap.of(InternalContent.PARENT_HASH_FIELD, (Object)client.rowHash(keySpace, columnFamily, StorageClientUtils.getParentObjectPath(key)));
            for ( Entry<String, Object> e : autoIndexMap.entrySet()) {
                // remove all previous values
                PreparedStatement removeStringColumn = client.getStatement(keySpace,
                        columnFamily, JDBCStorageClient.SQL_REMOVE_STRING_COLUMN, rid, statementCache);
                removeStringColumn.setString(1, rid);
                removeStringColumn.setString(2, e.getKey());
                removeStringColumn.addBatch();
                removeSet.add(removeStringColumn);
                PreparedStatement insertStringColumn = client.getStatement(keySpace,
                        columnFamily, JDBCStorageClient.SQL_INSERT_STRING_COLUMN, rid, statementCache);
                insertStringColumn.setString(1, (String)e.getValue());
                insertStringColumn.setString(2, rid);
                insertStringColumn.setString(3, e.getKey());
                insertStringColumn.addBatch();
                LOGGER.debug("Insert {} {}", e.getKey(), e.getValue());
                insertSet.add(insertStringColumn);
                List<Entry<String, Object>> insertSeq = insertSequence
                        .get(insertStringColumn);
                if (insertSeq == null) {
                    insertSeq = Lists.newArrayList();
                    insertSequence.put(insertStringColumn, insertSeq);
                }
                insertSeq.add(e);
            }
        }

        LOGGER.debug("Remove set {}", removeSet);

        for (PreparedStatement pst : removeSet) {
            pst.executeBatch();
        }

        LOGGER.debug("Insert set {}", insertSet);
        for (PreparedStatement pst : insertSet) {
            int[] res = pst.executeBatch();
            List<Entry<String, Object>> insertSeq = insertSequence.get(pst);
            for (int i = 0; i < res.length; i++ ) {
                Entry<String, Object> e = insertSeq.get(i);
                if ( res[i] <= 0 && res[i] != -2 ) { // Oracle drivers respond with -2 on a successful insert when the number is not known http://download.oracle.com/javase/1.3/docs/guide/jdbc/spec2/jdbc2.1.frame6.html
                    LOGGER.warn("Index failed for {} {} ", new Object[] { rid, e.getKey(),
                            e.getValue() });
                    
                } else {
                    LOGGER.debug("Index inserted for {} {} ", new Object[] { rid, e.getKey(),
                            e.getValue() });

                }
            }
        }
    }

}
