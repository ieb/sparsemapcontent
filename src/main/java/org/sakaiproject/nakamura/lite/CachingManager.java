/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.lite;

import org.sakaiproject.nakamura.api.lite.CacheHolder;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Extend this class to add caching to a Manager class.
 */
public abstract class CachingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingManager.class);
    private Map<String, CacheHolder> sharedCache;
    private StorageClient client;
    private int hit;
    private int miss;
    private long calls;

    /**
     * Create a new {@link CachingManager}
     * @param client a client to the underlying storage engine
     * @param sharedCache the cache where the objects will be stored
     */
    public CachingManager(StorageClient client, Map<String, CacheHolder> sharedCache) {
        this.client = client;
        this.sharedCache = sharedCache;
    }

    /**
     * Try to retrieve an object from the cache.
     * Has the side-effect of loading an uncached object into cache the first time.
     * @param keySpace the key space we're operating in.
     * @param columnFamily the column family for the object
     * @param key the object key
     * @return the object or null if not cached and not found
     * @throws StorageClientException
     */
    protected Map<String, Object> getCached(String keySpace, String columnFamily, String key)
            throws StorageClientException {
        Map<String, Object> m = null;
        String cacheKey = getCacheKey(keySpace, columnFamily, key);


        if (sharedCache != null && sharedCache.containsKey(cacheKey)) {
            CacheHolder cacheHolder = sharedCache.get(cacheKey);
            if (cacheHolder != null) {
                m = cacheHolder.get();
                LOGGER.debug("Cache Hit {} {} {} ",new Object[]{cacheKey, cacheHolder, m});
                hit++;
            }
        }
        if (m == null) {
            m = client.get(keySpace, columnFamily, key);
            miss++;
            if (sharedCache != null) {
                if (m != null) {
                    LOGGER.debug("Cache Miss, Found Map {} {}", cacheKey, m);
                }
                sharedCache.put(cacheKey, new CacheHolder(m));
            }
        }
        calls++;
        if ((calls % 1000) == 0) {
            getLogger().info("Cache Stats Hits {} Misses {}  hit% {}", new Object[] { hit, miss,
                    ((100 * hit) / (hit + miss)) });
        }
        return m;
    }

    protected abstract Logger getLogger();

    /**
     * Combine the parameters into a key suitable for storage and lookup in the cache.
     * @param keySpace
     * @param columnFamily
     * @param key
     * @return the cache key
     */
    private String getCacheKey(String keySpace, String columnFamily, String key) {
        return keySpace + ":" + columnFamily + ":" + key;
    }

    /**
     * Remove this object from the cache.
     * @param keySpace
     * @param columnFamily
     * @param key
     */
    protected void removeFromCache(String keySpace, String columnFamily, String key) {
        if (sharedCache != null) {
            sharedCache.remove(getCacheKey(keySpace, columnFamily, key));
        }
    }
    

    /**
     * Put an object in the cache
     * @param keySpace
     * @param columnFamily
     * @param key
     * @param encodedProperties the object to be stored
     * @param probablyNew whether or not this object is new.
     * @throws StorageClientException
     */
    protected void putCached(String keySpace, String columnFamily, String key,
            Map<String, Object> encodedProperties, boolean probablyNew)
            throws StorageClientException {
        LOGGER.debug("Saving {} {} {} {} ", new Object[] { keySpace, columnFamily, key, encodedProperties});
        client.insert(keySpace, columnFamily, key, encodedProperties, probablyNew);
        removeFromCache(keySpace, columnFamily, key);
    }

}
