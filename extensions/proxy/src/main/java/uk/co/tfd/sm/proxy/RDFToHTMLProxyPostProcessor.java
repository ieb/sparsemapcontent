package uk.co.tfd.sm.proxy;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.util.Iterables;
import org.sakaiproject.nakamura.api.memory.Cache;
import org.sakaiproject.nakamura.api.memory.CacheManagerService;
import org.sakaiproject.nakamura.api.memory.CacheScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.proxy.ProxyPostProcessor;
import uk.co.tfd.sm.api.proxy.ProxyResponse;
import uk.co.tfd.sm.api.template.TemplateService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component(immediate = true, metatype = true)
@Service(value = ProxyPostProcessor.class)
public class RDFToHTMLProxyPostProcessor implements ProxyPostProcessor,
		CachingProxyProcessor {

	private static final String CONFIG_CONTENT_TYPE = "content-type";

	private static final String CONFIG_CONTENT_ENCODING = "content-encoding";

	private static final String RESULT = "result";

	public static final String CONFIG_RESULT_KEY = "result-key";

	public static final String CONFIG_FINALTEMPLATE = "finaltemplate";

	public static final String CONFIG_FINALTEMPLATE_PATTERN = "finaltemplatepattern";

	public static final String CONFIG_FINALTEMPLATE_SELECTOR_PROPERTY = "finaltemplateselectorproperty";

	private static final Object CONFIG_FINALTEMPLATE_PRIORITIES = "templatepriorities";

	public static final String CONFIG_NAMESPACEMAP = "namespacemap";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RDFToHTMLProxyPostProcessor.class);

	@Reference
	protected TemplateService templateService;

	private Map<String, Integer> templatePriorities;

	@Reference
	private Resolver rdfResourceResolver;

	@Reference
	private CacheManagerService cacheManagerService;

	@SuppressWarnings("unchecked")
	public void process(Map<String, Object> config,
			Map<String, Object> templateParams, HttpServletResponse response,
			ProxyResponse proxyResponse) throws IOException {
		try {
			String namespaceMapConfig = (String) config
					.get(CONFIG_NAMESPACEMAP);
			LOGGER.info("Namespace setup {} ", namespaceMapConfig);

			ResolverHolder.set(rdfResourceResolver);
			RDFToMap rdfToMap = new RDFToMap(namespaceMapConfig, config);
			String key = (String) templateParams.get(config
					.get(CONFIG_RESULT_KEY));
			rdfToMap.readMap(
					new StringReader(proxyResponse.getResponseBodyAsString()))
					.resolveToFullJson();
			if ( cacheManagerService != null ) {
				Cache<Map<String, Object>> cache = cacheManagerService
						.getCache(RdfResourceResolver.RDFMAPS_CACHE_NAME,
								CacheScope.INSTANCE);
				rdfToMap.saveCache(cache);
			}
			Map<String, Object> fullMap = rdfToMap.toMap();
			if (key != null && fullMap.containsKey(key)) {
				fullMap.put(RESULT, ImmutableMap
						.copyOf((Map<String, Object>) fullMap.get(key)));
			}
			sendResult(key, config, fullMap, response);
		} catch (XMLStreamException e) {
			LOGGER.error(e.getMessage(), e);
			response.sendError(500,
					"Failed to parse response from remote server");
		} finally {
			ResolverHolder.clear();
		}

	}

	@SuppressWarnings("unchecked")
	private void sendResult(String key, Map<String, Object> config,
			Map<String, Object> fullMap, HttpServletResponse response)
			throws IOException {
		String templateName = (String) config.get(CONFIG_FINALTEMPLATE);
		String templateNamePattern = (String) config
				.get(CONFIG_FINALTEMPLATE_PATTERN);
		String templateSelectorProperty = (String) config
				.get(CONFIG_FINALTEMPLATE_SELECTOR_PROPERTY);
		String templatePrioritiesConfig = (String) config
				.get(CONFIG_FINALTEMPLATE_PRIORITIES);

		Builder<String, Integer> b = ImmutableMap.builder();
		if (templatePrioritiesConfig != null) {
			String[] priorityPairs = StringUtils.split(
					templatePrioritiesConfig, ",");
			for (String priorityPair : priorityPairs) {
				String[] p = StringUtils.split(priorityPair, ":");
				b.put(p[0], Integer.parseInt(p[1]));
			}
		}

		templatePriorities = b.build();
		if (templateNamePattern != null) {
			// get the most significant type, and see of we have a suitable
			// template
			LOGGER.info("Checking template Pattern {} property {}",
					templateNamePattern, templateSelectorProperty);
			Map<String, Object> base = fullMap;
			if (base.containsKey(RESULT)) {
				base = (Map<String, Object>) base.get(RESULT);
			}
			if (base.containsKey(templateSelectorProperty)) {
				Object o = base.get(templateSelectorProperty);
				LOGGER.info("Selector is {}", o);
				Iterable<Object> ov = null;
				if ( o instanceof Iterable ) {
					ov = (Iterable<Object>) o;
				} else if ( o instanceof Object[] ) {
					ov = Iterables.of((Object[]) o);
				} else {
					ov = Iterables.of(new Object[]{String.valueOf(o)}); 
				}
				int templatePriority = -1;
				for (Object v : ov) {
					String oe = String.valueOf(v);
					int i = oe.indexOf("#");
					if (i >= 0) {
						oe = oe.substring(i + 1);
					}
					String testTemplateName = MessageFormat.format(
							templateNamePattern, oe);
					if (templateService.checkTemplateExists(testTemplateName)) {
						LOGGER.info("Using Template {} ", testTemplateName);
						int thisPriority = 0;
						if (templatePriorities.containsKey(oe)) {
							thisPriority = templatePriorities.get(oe);
						}
						if (thisPriority > templatePriority) {
							templatePriority = thisPriority;
							templateName = testTemplateName;
						}
					} else {
						LOGGER.info("Template {} does not exist ",
								testTemplateName);
					}
				}
			}
		}
		if (templateName != null
				&& !templateService.checkTemplateExists(templateName)) {
			throw new IOException("Cant find template " + templateName
					+ " specified by " + CONFIG_FINALTEMPLATE);
		}
		LOGGER.info("Template setup {} ", templateName);
		String contentType = (String) config.get(CONFIG_CONTENT_TYPE);
		String contentEncoding = (String) config.get(CONFIG_CONTENT_ENCODING);

		if (templateName != null) {
			LOGGER.info("Rendering with {} as the base of the result map.", key);
			if (contentType != null) {
				response.setContentType(contentType);
			} else {
				response.setContentType("application/octet");
			}
			if (contentEncoding != null) {
				response.setCharacterEncoding(contentEncoding);
			}
			templateService.process(fullMap, "UTF-8", response.getWriter(),
					templateName);
		} else {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeHierarchyAdapter(Resource.class, new ResourceSerializer()).create();
			if (fullMap.containsKey(RESULT)) {
				response.getWriter().write(gson.toJson(fullMap.get(RESULT)));
			} else {
				response.getWriter().write(gson.toJson(fullMap));
			}
		}
	}

	public String getName() {
		return "RDFToHTMLResolvedJsonProxyPostProcessor";
	}

	public boolean sendCached(Map<String, Object> config,
			Map<String, Object> templateParams, HttpServletResponse response) throws IOException {
		String cacheKey = (String) config.get("cachekey");

		if (cacheManagerService != null && cacheKey != null) {
			ResolverHolder.set(rdfResourceResolver);
			try {
			String key = (String) templateParams.get(cacheKey);
			if (key != null) {
				Cache<Map<String, Object>> cache = cacheManagerService
						.getCache(RdfResourceResolver.RDFMAPS_CACHE_NAME,
								CacheScope.INSTANCE);
				if (cache.containsKey(key)) {
					Map<String, Object> cachedMap = cache.get(key);
					if ( cachedMap != null ) {
						Map<String, Object> result = Maps.newHashMap();
						result.put(RESULT, cachedMap);
						sendResult(key, config, result, response);
						return true;
					}
				}
			}
			} finally {
				ResolverHolder.set(rdfResourceResolver);				
			}
		}
		return false;
	}

	
}