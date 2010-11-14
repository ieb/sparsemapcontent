package org.sakaiproject.nakamura.lite.storage;

public interface RowHasher {

    String rowHash(String keySpace, String columnFamily, String key);

}
