package org.sakaiproject.nakamura.lite;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.CacheHolder;
import org.sakaiproject.nakamura.api.lite.StorageCacheManager;
import org.sakaiproject.nakamura.lite.storage.spi.ConcurrentLRUMap;

public class DummyStorageCacheManager implements StorageCacheManager {

    private Map<String, CacheHolder> cache = new ConcurrentLRUMap<String, CacheHolder>();

    @Override
    public Map<String, CacheHolder> getAccessControlCache() {
        return cache;
    }

    @Override
    public Map<String, CacheHolder> getAuthorizableCache() {
        return cache;
    }

    @Override
    public Map<String, CacheHolder> getContentCache() {
        return cache;
    }

    @Override
    public Map<String, CacheHolder> getCache(String cacheName) {
        return cache;
    }

}
