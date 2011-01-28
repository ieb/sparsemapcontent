package org.sakaiproject.nakamura.api.lite;

import java.util.Map;

/**
 * Provides Cache implementations for all the three areas represented as Maps.
 * If an implementation of this interface is present it will be used.
 */
public interface StorageCacheManager {

    Map<String, CacheHolder> getAccessControlCache();

    Map<String, CacheHolder> getAuthorizableCache();

    Map<String, CacheHolder> getContentCache();

}
