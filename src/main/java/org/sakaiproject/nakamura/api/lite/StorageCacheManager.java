package org.sakaiproject.nakamura.api.lite;

import java.util.Map;

/**
 * Provides Cache implementations for all the three areas represented as Maps.
 * If an implementation of this interface is present it will be used.
 */
public interface StorageCacheManager {

    /**
     * @return a Cache, implementing the Map interface, although the keys wont
     *         clash should be a separate memory space from the other caches to
     *         prevent memory poisoning. It would be theoretically possible to
     *         generate a cache ID for some content that could be shared in the
     *         authorizable or access control space, however thats finding a key
     *         matching a pattern that collides with a specific pattern after
     *         both have been hashed with SHA1. The probability or random
     *         collision in SHA1 is 1 in 1E14, so generating a collision for 2
     *         string matching specific patterns is probably far greater than
     *         that.
     */
    Map<String, CacheHolder> getAccessControlCache();

    /**
     * @return Should be a separate cache, not sharing the same memory space as
     *         others, see above for why.
     */
    Map<String, CacheHolder> getAuthorizableCache();

    /**
     * @return Should be a separate cache, not sharing the same memory space as
     *         others, see above for why.
     */
    Map<String, CacheHolder> getContentCache();
    
    
    /**
     * Get a named cache.
     * @param cacheName
     * @return
     */
    Map<String, CacheHolder> getCache(String cacheName);

    
    
}
