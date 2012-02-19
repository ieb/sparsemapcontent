package org.sakaiproject.nakamura.lite;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.CacheHolder;
import org.sakaiproject.nakamura.api.lite.StorageCacheManager;

/**
 * Unmanaged Caches are used where there is nothing else provided by the client.
 * @author ieb
 *
 */
public class NullCacheManagerX implements StorageCacheManager {



    @Override
    public Map<String, CacheHolder> getAccessControlCache() {
        return null;
    }

    @Override
    public Map<String, CacheHolder> getAuthorizableCache() {
        return null;
    }

    @Override
    public Map<String, CacheHolder> getContentCache() {
        return null;
    }

    @Override
    public Map<String, CacheHolder> getCache(String cacheName) {
        return null;
    }
    
    


}
