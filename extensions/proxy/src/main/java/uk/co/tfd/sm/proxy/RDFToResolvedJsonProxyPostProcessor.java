package uk.co.tfd.sm.proxy;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.memory.Cache;
import org.sakaiproject.nakamura.api.memory.CacheManagerService;
import org.sakaiproject.nakamura.api.memory.CacheScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.proxy.ProxyPostProcessor;
import uk.co.tfd.sm.api.proxy.ProxyResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component(immediate = true, metatype = true)
@Service(value = ProxyPostProcessor.class)
public class RDFToResolvedJsonProxyPostProcessor implements ProxyPostProcessor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RDFToResolvedJsonProxyPostProcessor.class);

	public static final String CONFIG_NAMESPACEMAP = "namespacemap";

	@Reference
	private Resolver rdfResourceResolver;

	@Reference
	private CacheManagerService cacheManagerService;

	public void process(Map<String, Object> config,
			Map<String, Object> templateParams, HttpServletResponse response,
			ProxyResponse proxyResponse) throws IOException {
		String namespaceMapConfig = (String) templateParams
				.get(CONFIG_NAMESPACEMAP);
		try {
			ResolverHolder.set(rdfResourceResolver);
			RDFToMap rdfToMap = new RDFToMap(namespaceMapConfig, config);
			response.getWriter().append(
					rdfToMap.readMap(
							new StringReader(proxyResponse
									.getResponseBodyAsString()))
							.resolveToFullJson().toJson(false));
			if (cacheManagerService != null) {
				Cache<Map<String, Object>> cache = cacheManagerService
						.getCache(RdfResourceResolver.RDFMAPS_CACHE_NAME,
								CacheScope.INSTANCE);
				rdfToMap.saveCache(cache);
			}
		} catch (XMLStreamException e) {
			LOGGER.error(e.getMessage(), e);
			response.sendError(500,
					"Failed to parse response from remote server");
		} finally {
			ResolverHolder.clear();
		}

	}

	public boolean sendCached(Map<String, Object> config,
			Map<String, Object> templateParams, HttpServletResponse response)
			throws IOException {
		String cacheKey = (String) config.get("cachekey");
		if (cacheManagerService != null && cacheKey != null) {
			String key = (String) templateParams.get(cacheKey);
			if (key != null) {
				ResolverHolder.set(rdfResourceResolver);
				try {
					Cache<Map<String, Object>> cache = cacheManagerService
							.getCache(RdfResourceResolver.RDFMAPS_CACHE_NAME,
									CacheScope.INSTANCE);
					if (cache.containsKey(key)) {
						Gson gson = new GsonBuilder()
								.setPrettyPrinting()
								.registerTypeHierarchyAdapter(Resource.class,
										new ResourceSerializer()).create();
						response.getWriter().write(
								gson.toJson(cache.get(cacheKey)));
						return true;
					}
				} finally {
					ResolverHolder.clear();
				}
			}
		}
		return false;
	}

	public String getName() {
		return "RDFToResolvedJsonProxyPostProcessor";
	}

}
