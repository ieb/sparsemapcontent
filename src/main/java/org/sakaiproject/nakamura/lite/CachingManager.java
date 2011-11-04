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
import org.sakaiproject.nakamura.lite.storage.RowHasher;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Extend this class to add caching to a Manager class.
 */
public abstract class CachingManager implements DirectCacheAccess {

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

        CacheHolder cacheHolder = getFromCacheInternal(cacheKey);
        if (cacheHolder != null ) {
            m = cacheHolder.get();
            if ( m != null ) {
                LOGGER.debug("Cache Hit {} {} {} ", new Object[] { cacheKey, cacheHolder, m });
                hit++;
            }
        }
        if (m == null) {
            m = client.get(keySpace, columnFamily, key);
            miss++;
            if (m != null) {
                LOGGER.debug("Cache Miss, Found Map {} {}", cacheKey, m);
            }
            putToCacheInternal(cacheKey, new CacheHolder(m));
        }
        calls++;
        if ((calls % 10000) == 0) {
            getLogger().info("Cache Stats Hits {} Misses {}  hit% {}", new Object[] { hit, miss,
                    ((100 * hit) / (hit + miss)) });
        }
        return m;
    }
    public void putToCache(String cacheKey, CacheHolder cacheHolder) {
        if ( client instanceof RowHasher ) {
            putToCacheInternal(cacheKey, cacheHolder);
        }
    }

    private void putToCacheInternal(String cacheKey, CacheHolder cacheHolder) {
        if (sharedCache != null) {
            sharedCache.put(cacheKey, cacheHolder);
        }
    }
    public CacheHolder getFromCache(String cacheKey) {
        if ( client instanceof RowHasher ) {
            return getFromCacheInternal(cacheKey);
        }
        return null;
    }
    private CacheHolder getFromCacheInternal(String cacheKey) {
        if (sharedCache != null && sharedCache.containsKey(cacheKey)) {
            return sharedCache.get(cacheKey);
        }
        return null;
    }

    protected abstract Logger getLogger();

    /**
     * Combine the parameters into a key suitable for storage and lookup in the cache.
     * @param keySpace
     * @param columnFamily
     * @param key
     * @return the cache key
     * @throws StorageClientException 
     */
    private String getCacheKey(String keySpace, String columnFamily, String key) throws StorageClientException {
        if ( client instanceof RowHasher) {
            return ((RowHasher) client).rowHash(keySpace, columnFamily, key);
        }
        return keySpace + ":" + columnFamily + ":" + key;
    }

    /**
     * Remove this object from the cache. Note, StorageClient uses the word
     * remove to mean delete. This method should do the same.
     * 
     * @param keySpace
     * @param columnFamily
     * @param key
     * @throws StorageClientException 
     */
    protected void removeCached(String keySpace, String columnFamily, String key) throws StorageClientException {
        if (sharedCache != null) {
            // insert a replacement. This should cause an invalidation message to propagate in the cluster.
            putToCacheInternal(getCacheKey(keySpace, columnFamily, key), new CacheHolder(null));
        }
        client.remove(keySpace, columnFamily, key);

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
        String cacheKey = null;
        if ( sharedCache != null ) {
            cacheKey = getCacheKey(keySpace, columnFamily, key);
        }
        if ( sharedCache != null && !probablyNew ) {
            CacheHolder ch = getFromCacheInternal(cacheKey);
            if ( ch != null && ch.get() == null ) {
                return; // catch the case where another method creates while something is in the cache.
                // this is a big assumption since if the item is not in the cache it will get updated
                // there is no difference in sparsemap between create and update, they are all insert operations
                // what we are really saying here is that inorder to update the item you have to have just got it
                // and if you failed to get it, your update must have been a create operation. As long as the dwell time
                // in the cache is longer than the lifetime of an active session then this will be true.
                // if the lifetime of an active session is longer (like with a long running background operation)
                // then you should expect to see race conditions at this point since the marker in the cache will have 
                // gone, and the marker in the database has gone, so the put operation, must be a create operation.
                // To change this behavior we would need to differentiate more strongly between new and update and change 
                // probablyNew into certainlyNew, but that would probably break the BASIC assumption of the whole system.
            }
        }
        LOGGER.debug("Saving {} {} {} {} ", new Object[] { keySpace, columnFamily, key,
                encodedProperties });
        client.insert(keySpace, columnFamily, key, encodedProperties, probablyNew);
        if ( sharedCache != null ) {
            sharedCache.remove(cacheKey);
        }
    }

}
