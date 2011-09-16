package org.sakaiproject.nakamura.api.lite;

import java.util.Map;

public interface ColumnFamilyCacheManager extends StorageCacheManager {

    public Map<String, CacheHolder> getCache(String columnFamily);


}
