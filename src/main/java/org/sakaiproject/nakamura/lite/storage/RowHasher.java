package org.sakaiproject.nakamura.lite.storage;

import org.sakaiproject.nakamura.api.lite.StorageClientException;

public interface RowHasher {

    String rowHash(String keySpace, String columnFamily, String key) throws StorageClientException;

}
