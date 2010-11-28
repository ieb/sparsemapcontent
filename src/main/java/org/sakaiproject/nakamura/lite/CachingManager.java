package org.sakaiproject.nakamura.lite;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.lite.accesscontrol.CacheHolder;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CachingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingManager.class);
    private Map<String, CacheHolder> sharedCache;
    private StorageClient client;
    private int hit;
    private int miss;
    private long calls;

    public CachingManager(StorageClient client, Map<String, CacheHolder> sharedCache) {
        this.client = client;
        this.sharedCache = sharedCache;
    }

    protected Map<String, Object> getCached(String keySpace, String columnFamily, String key)
            throws StorageClientException {
        Map<String, Object> m;
        String cacheKey = getCacheKey(keySpace, columnFamily, key);
        if (sharedCache != null && sharedCache.containsKey(cacheKey)) {
            CacheHolder aclCacheHolder = sharedCache.get(cacheKey);
            m = aclCacheHolder.get();
            hit++;
        } else {
            m = client.get(keySpace, columnFamily, key);
            miss++;
            if (sharedCache != null) {
                if (m != null) {
                    LOGGER.info("Found Map {} {}", cacheKey, m);
                }
                sharedCache.put(cacheKey, new CacheHolder(m));
            }
        }
        calls++;
        if ((calls % 1000) == 0) {
            LOGGER.info("Cache Stats Hits {} Misses {}  hit% {}", new Object[] { hit, miss,
                    ((100 * hit) / (hit + miss)) });
        }
        return m;
    }

    private String getCacheKey(String keySpace, String columnFamily, String key) {
        return keySpace + ":" + columnFamily + ":" + key;
    }

    protected void removeFromCache(String keySpace, String columnFamily, String key) {
        if (sharedCache != null) {
            sharedCache.remove(getCacheKey(keySpace, columnFamily, key));
        }
    }

    protected void putCached(String keySpace, String columnFamily, String key,
            Map<String, Object> encodedProperties) throws StorageClientException {
        removeFromCache(keySpace, columnFamily, key);
        client.insert(keySpace, columnFamily, key, encodedProperties);
    }

}
