package uk.co.tfd.sm.proxy;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;


import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.memory.Cache;
import org.sakaiproject.nakamura.api.memory.CacheManagerService;
import org.sakaiproject.nakamura.api.memory.CacheScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.proxy.ProxyClientException;
import uk.co.tfd.sm.api.proxy.ProxyClientService;
import uk.co.tfd.sm.api.proxy.ProxyResponse;

import com.google.common.collect.ImmutableMap;

@Component(immediate = true, metatype = true)
@Service(value = Resolver.class)
public class RdfResourceResolver implements Resolver {

	private static final Map<String, Object> EMPTY_MAP = ImmutableMap.of();

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RdfResourceResolver.class);

	@Reference
	private ProxyClientService proxyClientService;
	
	@Reference
	private CacheManagerService cacheManagerService;

	@SuppressWarnings("unchecked")
	public Map<String, Object> get(String key,
			Map<String, Object> resolverConfig) throws IOException {
		Cache<Map<String, Object>> cache = cacheManagerService.getCache("rdfmaps", CacheScope.INSTANCE);
		if ( cache.containsKey(key)) {
			return cache.get(key);
		}
		String targetPath = (String) resolverConfig.get("resolver-endpoint");
		if ( targetPath == null ) {
			targetPath = (String) resolverConfig.get(ProxyClientService.CONFIG_REQUEST_PROXY_ENDPOINT);
		}
		try {
			ProxyResponse proxyResponse = proxyClientService.executeCall(
					ImmutableMap.of(
							ProxyClientService.CONFIG_REQUEST_PROXY_ENDPOINT,
							(Object) targetPath,
							ProxyClientService.CONFIG_REQUEST_PROXY_METHOD,
							"GET"), EMPTY_MAP, ImmutableMap.of("vid",
							(Object) key), null, -1L, null);
			String namespaceMapConfig = (String) resolverConfig
					.get(RDFToHTMLProxyPostProcessor.CONFIG_NAMESPACEMAP);
			RDFToMap rdfToMap = new RDFToMap(namespaceMapConfig, this,
					resolverConfig);
			rdfToMap.readMap(
					new StringReader(proxyResponse.getResponseBodyAsString()))
					.resolveToFullJson();
			Map<String, Object> fullMap = rdfToMap.toMap();
			for ( Entry<String, Object> e : fullMap.entrySet()) {
				Object o = e.getValue();
				if ( o instanceof Map) {
					cache.put(e.getKey(), (Map<String, Object>) e.getValue());
				}
			}
			if (key != null && fullMap.containsKey(key)) {
				fullMap = ImmutableMap.copyOf((Map<String, Object>) fullMap
						.get(key));
			}
			cache.put(key, fullMap);
			return fullMap;
		} catch (ProxyClientException e) {
			LOGGER.warn(e.getMessage(), e);
		} catch (XMLStreamException e) {
			LOGGER.warn(e.getMessage(), e);
		}
		return EMPTY_MAP;
	}

}
