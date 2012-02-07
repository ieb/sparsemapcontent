package uk.co.tfd.sm.memory.ehcache;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.CacheHolder;
import org.sakaiproject.nakamura.api.lite.StorageCacheManager;
import org.sakaiproject.nakamura.api.memory.Cache;
import org.sakaiproject.nakamura.api.memory.CacheManagerService;
import org.sakaiproject.nakamura.api.memory.CacheScope;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import java.util.Map;

@Component(immediate=true, metatype=true)
@Service(value=StorageCacheManager.class)
public class StorageCacheManagerImpl implements StorageCacheManager {

  
  @Reference
  private CacheManagerService cacheManagerService;

  private Map<String, Map<String, CacheHolder>> knownCaches;

  @Activate
  public void activate(Map<String, Object> props) {
	Builder<String, Map<String, CacheHolder>> b = ImmutableMap.builder();  
    Cache<CacheHolder> accesssControlCacheCache = cacheManagerService.getCache("accessControlCache", CacheScope.CLUSTERINVALIDATED);
    Cache<CacheHolder> authorizableCacheCache = cacheManagerService.getCache("authorizableCache", CacheScope.CLUSTERINVALIDATED);
    Cache<CacheHolder> contentCacheCache = cacheManagerService.getCache("contentCache", CacheScope.CLUSTERINVALIDATED);
    Cache<CacheHolder> queryCache = cacheManagerService.getCache("queryCache", CacheScope.CLUSTERINVALIDATED);
    b.put("ac", new MapDeligate<String, CacheHolder>(accesssControlCacheCache));
    b.put("au", new MapDeligate<String, CacheHolder>(authorizableCacheCache));
    b.put("cn", new MapDeligate<String, CacheHolder>(contentCacheCache));
    b.put("sparseQueryCache", new MapDeligate<String, CacheHolder>(queryCache));
    knownCaches = b.build();
  }
  
  @Deactivate
  public void deactivate(Map<String, Object> props) {
  }
  
  
  @Override
  public Map<String, CacheHolder> getAccessControlCache() {
    return getCache("ac");
  }

  @Override
  public Map<String, CacheHolder> getAuthorizableCache() {
	return getCache("au");
  }

  @Override
  public Map<String, CacheHolder> getContentCache() {
	 return getCache("cn");
  }

	@Override
	public Map<String, CacheHolder> getCache(String cacheName) {
		if ( knownCaches.containsKey(cacheName)) {
			return knownCaches.get(cacheName);
		}
		Cache<CacheHolder> cache = cacheManagerService.getCache(cacheName, CacheScope.CLUSTERINVALIDATED);
		return new MapDeligate<String, CacheHolder>(cache);
	}

}
