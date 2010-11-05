package org.sakaiproject.nakamura.lite.storage;

import java.util.Map;

public interface StorageClient {

    Map<String, Object> get(String keySpace, String columnFamily, String key)
            throws StorageClientException;

    void insert(String keySpace, String columnFamily, String key, Map<String, Object> values)
            throws StorageClientException;

    void remove(String keySpace, String columnFamily, String key) throws StorageClientException;

}
