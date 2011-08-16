package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.StorageClientException;

public interface Indexer {

    void index(Map<String, PreparedStatement> statementCache,
            String keySpace, String columnFamily, String key, String rid, Map<String, Object> values)
            throws StorageClientException, SQLException;

}
