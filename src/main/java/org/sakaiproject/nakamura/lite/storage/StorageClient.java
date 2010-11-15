package org.sakaiproject.nakamura.lite.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

public interface StorageClient {

    Map<String, Object> get(String keySpace, String columnFamily, String key)
            throws StorageClientException;

    void insert(String keySpace, String columnFamily, String key, Map<String, Object> values)
            throws StorageClientException;

    void remove(String keySpace, String columnFamily, String key) throws StorageClientException;

    InputStream streamBodyOut(String keySpace, String columnFamily, String contentId,
            String contentBlockId, Map<String, Object> content) throws StorageClientException, AccessDeniedException, IOException;

    Map<String, Object> streamBodyIn(String keySpace, String columnFamily, String contentId,
            String contentBlockId, Map<String, Object> content, InputStream in)
            throws StorageClientException, AccessDeniedException, IOException;

}
