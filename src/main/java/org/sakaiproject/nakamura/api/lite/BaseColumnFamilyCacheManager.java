package org.sakaiproject.nakamura.api.lite;

import java.util.Map;

public  abstract class BaseColumnFamilyCacheManager implements ColumnFamilyCacheManager {

    public Map<String, CacheHolder> getAccessControlCache() {
        throw new UnsupportedOperationException("Use getCache(String columnFamily)");
    }

    public Map<String, CacheHolder> getAuthorizableCache() {
        throw new UnsupportedOperationException("Use getCache(String columnFamily)");
    }

    public Map<String, CacheHolder> getContentCache() {
        throw new UnsupportedOperationException("Use getCache(String columnFamily)");
    }

    /**
     * This method deals with backward compatibility of StorageCacheManager which was developed when 
     * @param configuration
     * @param columnFamily
     * @param storageCacheManager
     * @return
     */
    public static Map<String, CacheHolder> getCache(Configuration configuration, String columnFamily,
            StorageCacheManager storageCacheManager) {
        if ( storageCacheManager instanceof ColumnFamilyCacheManager ) {
            return ((ColumnFamilyCacheManager) storageCacheManager).getCache(columnFamily);
        }
        if ( configuration.getAclColumnFamily().equals(columnFamily)) {
            return storageCacheManager.getAccessControlCache();
        }
        if ( configuration.getAuthorizableColumnFamily().equals(columnFamily)) {
            return storageCacheManager.getAuthorizableCache();
        }
        if ( configuration.getContentColumnFamily().equals(columnFamily)) {
            return storageCacheManager.getContentCache();
        }
        return null;
    }

}
