package org.sakaiproject.nakamura.lite.storage.spi;

import org.sakaiproject.nakamura.api.lite.CacheHolder;

/**
 * Caching Managers that implement this interface use the same keys in the cache
 * as is used by the underlying storage client and so the underlying storage
 * client can directly access the cache with its own cache keys.
 * 
 * @author ieb
 * 
 */
public interface DirectCacheAccess {

    void putToCache(String cacheKey, CacheHolder cacheHolder);

    void putToCache(String cacheKey, CacheHolder cacheHolder, boolean respectDeletes);

    CacheHolder getFromCache(String cacheKey);

}
