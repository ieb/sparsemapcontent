package org.sakaiproject.nakamura.lite.storage;

import java.util.Map;

public interface StorageClientListener {

    void delete(String keySpace, String columnFamily, String key);

    void after(String keySpace, String columnFamily, String key, Map<String, Object> mapAfter);

    void before(String keySpace, String columnFamily, String key, Map<String, Object> mapBefore);

    void commit();

    void begin();

    void rollback();

}
