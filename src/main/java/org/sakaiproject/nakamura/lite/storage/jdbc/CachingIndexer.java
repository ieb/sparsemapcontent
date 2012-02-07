package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.util.Map;

/**
 * Caching indexers cache result sets and have a method to allow external
 * classes to invalidate cache rows.
 * 
 * @author ieb
 * 
 */
public interface CachingIndexer {

    /**
     * Invalidate a cache entry, based on the query properties.
     * @param keyspace
     * @param columnFamily
     * @param queryProperties
     */
    void invalidate(String keyspace, String columnFamily, Map<String, Object> queryProperties);
}
