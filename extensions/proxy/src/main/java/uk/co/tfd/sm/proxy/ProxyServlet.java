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
package uk.co.tfd.sm.proxy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.proxy.ProxyClientException;
import uk.co.tfd.sm.api.proxy.ProxyClientService;
import uk.co.tfd.sm.api.proxy.ProxyPostProcessor;
import uk.co.tfd.sm.api.proxy.ProxyPreProcessor;
import uk.co.tfd.sm.api.proxy.ProxyResponse;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * This servlet binds to a resource that defines an end point.
 * 
 */
@Component(immediate = true, metatype = true)
@Service(value = Servlet.class)
@org.apache.felix.scr.annotations.Properties(value = { @Property(name = "alias", value = "/proxy") })
public class ProxyServlet extends HttpServlet {


	private static final boolean DEFAULT_CACHE_CONFIG = false;

	@Property(boolValue=DEFAULT_CACHE_CONFIG)
	private static final String CACHE_CONFIG = "cache-config";


	protected static final String DEFAULT_TEMPLATE_PATH = "proxy/config";

	
	@Property(value=DEFAULT_TEMPLATE_PATH)
	protected static final String PROP_TEMPLATE_PATH = "templatePath";

	private static final String SAKAI_PROXY_REQUEST_BODY = "Sakai-Proxy-Request-Body";

	private static final String PUT_METHOD = "PUT";

	private static final String POST_METHOD = "POST";

	public static final String PROXY_PATH_PREFIX = "/var/proxy/";

	/**
   *
   */
	private static final String SAKAI_REQUEST_STREAM_BODY = "sakai:request-stream-body";

	/**
   *
   */
	private static final String BASIC_PASSWORD = ":basic-password";

	/**
   *
   */
	private static final String BASIC_USER = ":basic-user";

	/**
   *
   */
	private static final long serialVersionUID = -3190208378955330531L;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ProxyServlet.class);

	@Reference
	transient ProxyClientService proxyClientService;

	private transient ProxyPostProcessor defaultPostProcessor = new DefaultProxyPostProcessorImpl();

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = ProxyPreProcessor.class, bind = "bindPreProcessor", unbind = "unbindPreProcessor")
	Map<String, ProxyPreProcessor> preProcessors = new ConcurrentHashMap<String, ProxyPreProcessor>();

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, referenceInterface = ProxyPostProcessor.class, bind = "bindPostProcessor", unbind = "unbindPostProcessor")
	Map<String, ProxyPostProcessor> postProcessors = new ConcurrentHashMap<String, ProxyPostProcessor>();

	private static final Set<String> HEADER_BLACKLIST = ImmutableSet.of("Host",
			"Content-Length", "Content-Type", "Authorization");

	private static final String CLASSPATH_PREFIX = "uk/co/tfd/sm/proxy";


	private String baseFile;

	/**
	 * There will almost certainly never be enough valid proxy maps to need this
	 * to be cleared.
	 */
	private Map<String, Map<String, Object>> configCache = Maps
			.newConcurrentMap();


	private boolean cacheConfig;

	@Activate
	protected void activate(Map<String, Object> properties) {
		baseFile = toString(properties.get(PROP_TEMPLATE_PATH),
				DEFAULT_TEMPLATE_PATH);
		cacheConfig = toBoolean(properties.get(CACHE_CONFIG),DEFAULT_CACHE_CONFIG);
	}

	private String toString(Object configValue, String defaultValue) {
		if (configValue == null) {
			return defaultValue;
		}
		return String.valueOf(configValue);
	}

	private boolean toBoolean(Object configValue, boolean defaultValue) {
		if (configValue == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(String.valueOf(configValue));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			boolean proxyStream = false;
			if (POST_METHOD.equals(request.getMethod())
					|| PUT_METHOD.equals(request.getMethod())) {
				String proxyRequest = request
						.getHeader(SAKAI_PROXY_REQUEST_BODY);
				proxyStream = Boolean.parseBoolean(proxyRequest);
			}

			String path = request.getPathInfo();
			Map<String, Object> config = getConfig(path);
			if (config == null) {
				response.sendError(404);
				return;
			}

			if (!proxyStream) {
				String streamBody = (String) config
						.get(SAKAI_REQUEST_STREAM_BODY);
				if (streamBody != null) {
					proxyStream = Boolean.parseBoolean(streamBody);
				}
			}

			Builder<String, Object> headersBuilder = ImmutableMap.builder();
			Builder<String, Object> templateParamsBuilder = ImmutableMap
					.builder();

			for (Enumeration<?> enames = request.getHeaderNames(); enames
					.hasMoreElements();) {

				String name = (String) enames.nextElement();
				if (HEADER_BLACKLIST.contains(name)) {
					continue;
				}
				if (name.equals(BASIC_USER)) {
					templateParamsBuilder.put(BASIC_USER,
							request.getHeader(BASIC_USER));
				} else if (name.equals(BASIC_PASSWORD)) {
					templateParamsBuilder.put(BASIC_USER,
							request.getHeader(BASIC_USER));
				} else if (!name.startsWith(":")) {
					headersBuilder.put(
							name,
							toSimpleString((String[]) EnumerationUtils.toList(
									request.getHeaders(name)).toArray(
									new String[0])));
				}
			}

			Map<String, Object> headers = headersBuilder.build();

			// collect the parameters and store into a mutable map.
			Map<String, String[]> rpm = request.getParameterMap();
			for (Entry<String, String[]> e : rpm.entrySet()) {
				templateParamsBuilder.put(e.getKey(),
						toSimpleString(e.getValue()));
			}

			Map<String, Object> templateParams = templateParamsBuilder.build();

			// we might want to pre-process the headers
			if (config.containsKey(ProxyPreProcessor.CONFIG_PREPROCESSOR)) {
				String preprocessorName = (String) config
						.get(ProxyPreProcessor.CONFIG_PREPROCESSOR);
				ProxyPreProcessor preprocessor = preProcessors
						.get(preprocessorName);
				if (preprocessor != null) {
					preprocessor.preProcessRequest(request, headers,
							templateParams);
				} else {
					LOGGER.warn(
							"Unable to find pre processor of name {} for node {} ",
							preprocessorName, path);
				}
			}
			ProxyPostProcessor postProcessor = defaultPostProcessor;
			// we might want to post-process the headers
			if (config.containsKey(ProxyPostProcessor.CONFIG_POSTPROCESSOR)) {
				String postProcessorName = (String) config
						.get(ProxyPostProcessor.CONFIG_POSTPROCESSOR);
				if (postProcessors.containsKey(postProcessorName))
					postProcessor = postProcessors.get(postProcessorName);
				if (postProcessor == null) {
					LOGGER.warn(
							"Unable to find post processor of name {} for node {} ",
							postProcessorName, path);
					postProcessor = defaultPostProcessor;
				}
			}
			if ( postProcessor instanceof CachingProxyProcessor) {
				CachingProxyProcessor cachingPostProcessor = (CachingProxyProcessor) postProcessor;
				if ( cachingPostProcessor.sendCached(config, templateParams, response)) {
					return;
				}
			}

			ProxyResponse proxyResponse = proxyClientService.executeCall(
					config, headers, templateParams, null, -1, null);
			try {
				postProcessor.process(config, templateParams, response, proxyResponse);
			} finally {
				proxyResponse.close();
			}
		} catch (IOException e) {
			throw e;
		} catch (ProxyClientException e) {
			response.sendError(500, e.getMessage());
		}
	}

	private Object toSimpleString(String[] a) {
		if (a == null) {
			return null;
		}
		if (a.length == 1) {
			return a[0];
		}
		return a;
	}

	private Map<String, Object> getConfig(String pathInfo) throws IOException {
		if (pathInfo == null) {
			return null;
		}

		if (cacheConfig && configCache.containsKey(pathInfo)) {
			return configCache.get(pathInfo);
		} else {
			Properties p = new Properties();
			InputStream in = this.getClass().getClassLoader()
					.getResourceAsStream(CLASSPATH_PREFIX + pathInfo);
			if (in != null) {
				LOGGER.info("Loading Classpath {} ", CLASSPATH_PREFIX+pathInfo);
				p.load(in);
				in.close();
			} else {
				LOGGER.info("Not Loading Classpath {} ", CLASSPATH_PREFIX+pathInfo);
				File configFile = new File(baseFile, pathInfo);
				if (!configFile.exists() || !configFile.isFile()
						|| !configFile.canRead()) {
					LOGGER.info("Not Loading File {} ", configFile.getAbsoluteFile());
					return null;
				}
				LOGGER.info("Loading File {} ", configFile.getAbsoluteFile());
				FileReader fr = new FileReader(configFile);
				p.load(fr);
				fr.close();
			}
			Builder<String, Object> b = ImmutableMap.builder();
			for (Entry<Object, Object> e : p.entrySet()) {
				String k = String.valueOf(e.getKey());
				b.put(k, e.getValue());
			}
			Map<String, Object> config = b.build();
			configCache.put(pathInfo, config);
			return config;
		}
	}

	protected void bindPreProcessor(ProxyPreProcessor proxyPreProcessor) {
		preProcessors.put(proxyPreProcessor.getName(), proxyPreProcessor);
	}

	protected void unbindPreProcessor(ProxyPreProcessor proxyPreProcessor) {
		preProcessors.remove(proxyPreProcessor.getName());
	}

	protected void bindPostProcessor(ProxyPostProcessor proxyPostProcessor) {
		postProcessors.put(proxyPostProcessor.getName(), proxyPostProcessor);
	}

	protected void unbindPostProcessor(ProxyPostProcessor proxyPostProcessor) {
		postProcessors.remove(proxyPostProcessor.getName());
	}

}
