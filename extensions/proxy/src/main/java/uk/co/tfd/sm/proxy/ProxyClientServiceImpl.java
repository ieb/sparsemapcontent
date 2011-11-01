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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.proxy.ProxyClientException;
import uk.co.tfd.sm.api.proxy.ProxyClientService;
import uk.co.tfd.sm.api.proxy.ProxyMethod;
import uk.co.tfd.sm.api.proxy.ProxyPostProcessor;
import uk.co.tfd.sm.api.proxy.ProxyResponse;
import uk.co.tfd.sm.api.template.TemplateService;

import com.google.common.collect.Maps;

/**
 *
 */
@Service(value = ProxyClientService.class)
@Component(immediate = true, metatype = true)
public class ProxyClientServiceImpl implements ProxyClientService {

	/**
	 * Default content type of request bodies if none has been specified.
	 */
	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	@Property(value = { "rss", "trustedLoginTokenProxyPostProcessor",
			"someothersafepostprocessor" })
	private static final String SAFE_POSTPROCESSORS = "safe.postprocessors";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ProxyClientServiceImpl.class);


	@Reference
	protected TemplateService templateService;

	private Map<String, Object> configProperties;

	private Set<String> safeOpenProcessors = new HashSet<String>();

	private boolean useJreProxy = false;

	private ThreadLocal<Map<String, Object>> boundConfig = new ThreadLocal<Map<String, Object>>();


	/**
	 * Create resources used by this component.
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	protected void activate(Map<String, Object> properties) throws Exception {
		configProperties = properties;
		String[] safePostProcessorNames = (String[]) configProperties
				.get(SAFE_POSTPROCESSORS);
		if (safePostProcessorNames == null) {
			safeOpenProcessors.add("rss");
			safeOpenProcessors.add("trustedLoginTokenProxyPostProcessor");
		} else {
			for (String pp : safePostProcessorNames) {
				safeOpenProcessors.add(pp);
			}
		}

		// allow communications via a proxy server if command line
		// java parameters http.proxyHost,http.proxyPort,http.proxyUser,
		// http.proxyPassword have been provided.
		String proxyHost = System.getProperty("http.proxyHost", "");
		if (!proxyHost.equals("")) {
			useJreProxy = true;
		}
	}

	private HttpClient getHttpClient() {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		if (useJreProxy) {
			ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
					httpclient.getConnectionManager().getSchemeRegistry(),
					ProxySelector.getDefault());
			httpclient.setRoutePlanner(routePlanner);
		}
		return httpclient;
	}

	/**
	 * Clean up resources used by this component
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	protected void deactivate(Map<String, Object> ctx) throws Exception {
	}

	/**
	 * Executes a HTTP call using a path in the JCR to point to a template and a
	 * map of properties to populate that template with. An example might be a
	 * SOAP call.
	 * 
	 * <pre>
	 * {http://www.w3.org/2001/12/soap-envelope}Envelope:{
	 *  {http://www.w3.org/2001/12/soap-envelope}Body:{
	 *   {http://www.example.org/stock}GetStockPriceResponse:{
	 *    &gt;body:[       ]
	 *    {http://www.example.org/stock}Price:{
	 *     &gt;body:[34.5]
	 *    }
	 *   }
	 *   &gt;body:[  ]
	 *  }
	 *  &gt;body:[   ]
	 *  {http://www.w3.org/2001/12/soap-envelope}encodingStyle:[http://www.w3.org/2001/12/soap-encoding]
	 * }
	 * 
	 * </pre>
	 * 
	 * @param resource
	 *            the resource containing the proxy end point specification.
	 * @param headers
	 *            a map of headers to set int the request.
	 * @param input
	 *            a map of parameters for all templates (both url and body)
	 * @param requestInputStream
	 *            containing the request body (can be null if the call requires
	 *            no body or the template will be used to generate the body)
	 * @param requestContentLength
	 *            if the requestImputStream is specified, the length specifies
	 *            the lenght of the body.
	 * @param requerstContentType
	 *            the content type of the request, if null the node property
	 *            sakai:proxy-request-content-type will be used.
	 * @throws ProxyClientException
	 */
	public ProxyResponse executeCall(Map<String, Object> config,
			Map<String, Object> headers, Map<String, Object> input,
			InputStream requestInputStream, long requestContentLength,
			String requestContentType) throws ProxyClientException {
		try {
			LOGGER.info(
					"Calling Execute Call with Config:[{}] Headers:[{}] Input:[{}] "
							+ "RequestInputStream:[{}] InputStreamContentLength:[{}] RequestContentType:[{}] ",
					new Object[] { config, headers, input, requestInputStream,
							requestContentLength, requestContentType });
			bindConfig(config);

			if (config != null
					&& config.containsKey(CONFIG_REQUEST_PROXY_ENDPOINT)) {
				// setup the post request
				String endpointURL = (String) config
						.get(CONFIG_REQUEST_PROXY_ENDPOINT);
				if (isUnsafeProxyDefinition(config)) {
					try {
						URL u = new URL(endpointURL);
						String host = u.getHost();
						if (host.indexOf('$') >= 0) {
							throw new ProxyClientException(
									"Invalid Endpoint template, relies on request to resolve valid URL "
											+ u);
						}
					} catch (MalformedURLException e) {
						throw new ProxyClientException(
								"Invalid Endpoint template, relies on request to resolve valid URL",
								e);
					}
				}
				
				LOGGER.info("Valied Endpoint Def");

				Map<String, Object> context = Maps.newHashMap(input);

				// add in the config properties from the bundle overwriting
				// everything else.
				context.put("config", configProperties);

				endpointURL = processUrlTemplate(endpointURL, context);
				
				LOGGER.info("Calling URL {} ",endpointURL);

				ProxyMethod proxyMethod = ProxyMethod.GET;
				if (config.containsKey(CONFIG_REQUEST_PROXY_METHOD)) {
					try {
						proxyMethod = ProxyMethod.valueOf((String) config
								.get(CONFIG_REQUEST_PROXY_METHOD));
					} catch (Exception e) {

					}
				}

				HttpClient client = getHttpClient();

				HttpUriRequest method = null;
				switch (proxyMethod) {
				case GET:
					if (config.containsKey(CONFIG_LIMIT_GET_SIZE)) {
						long maxSize = (Long) config.get(CONFIG_LIMIT_GET_SIZE);
						HttpHead h = new HttpHead(endpointURL);

						HttpParams params = h.getParams();
						// make certain we reject the body of a head
						params.setBooleanParameter(
								"http.protocol.reject-head-body", true);
						h.setParams(params);
						populateMessage(method, config, headers);
						HttpResponse response = client.execute(h);
						if (response.getStatusLine().getStatusCode() == 200) {
							// Check if the content-length is smaller than the
							// maximum (if any).
							Header contentLengthHeader = response
									.getLastHeader("Content-Length");
							if (contentLengthHeader != null) {
								long length = Long
										.parseLong(contentLengthHeader
												.getValue());
								if (length > maxSize) {
									return new ProxyResponseImpl(
											HttpServletResponse.SC_PRECONDITION_FAILED,
											"Response too large", response);
								}
							}
						} else {
							return new ProxyResponseImpl(response);
						}
					}
					method = new HttpGet(endpointURL);
					break;
				case HEAD:
					method = new HttpHead(endpointURL);
					break;
				case OPTIONS:
					method = new HttpOptions(endpointURL);
					break;
				case POST:
					method = new HttpPost(endpointURL);
					break;
				case PUT:
					method = new HttpPut(endpointURL);
					break;
				default:
					method = new HttpGet(endpointURL);
				}

				populateMessage(method, config, headers);

				if (requestInputStream == null
						&& !config.containsKey(CONFIG_PROXY_REQUEST_TEMPLATE)) {
					if (method instanceof HttpPost) {
						HttpPost postMethod = (HttpPost) method;
						MultipartEntity multipart = new MultipartEntity();
						for (Entry<String, Object> param : input.entrySet()) {
							String key = param.getKey();
							Object value = param.getValue();
							if (value instanceof Object[]) {
								for (Object val : (Object[]) value) {
									addPart(multipart, key, val);
								}
							} else {
								addPart(multipart, key, value);
							}
							postMethod.setEntity(multipart);
						}
					}
				} else {

					if (method instanceof HttpEntityEnclosingRequestBase) {
						String contentType = requestContentType;
						if (contentType == null
								&& config
										.containsKey(CONFIG_REQUEST_CONTENT_TYPE)) {
							contentType = (String) config
									.get(CONFIG_REQUEST_CONTENT_TYPE);

						}
						if (contentType == null) {
							contentType = APPLICATION_OCTET_STREAM;
						}
						HttpEntityEnclosingRequestBase eemethod = (HttpEntityEnclosingRequestBase) method;
						if (requestInputStream != null) {
							eemethod.setHeader(HttpHeaders.CONTENT_TYPE,
									contentType);
							eemethod.setEntity(new InputStreamEntity(
									requestInputStream, requestContentLength));
						} else {
							// build the request
							StringWriter body = new StringWriter();
							templateService.evaluate(context, body, (String) config
											.get("path"), (String) config
											.get(CONFIG_PROXY_REQUEST_TEMPLATE));
							byte[] soapBodyContent = body.toString().getBytes(
									"UTF-8");
							eemethod.setHeader(HttpHeaders.CONTENT_TYPE,
									contentType);
							eemethod.setEntity(new InputStreamEntity(
									new ByteArrayInputStream(soapBodyContent),
									soapBodyContent.length));

						}
					}
				}

				HttpResponse response = client.execute(method);
				if (response.getStatusLine().getStatusCode() == 302
						&& method instanceof HttpEntityEnclosingRequestBase) {
					// handle redirects on post and put
					String url = response.getFirstHeader("Location").getValue();
					method = new HttpGet(url);
					response = client.execute(method);
				}

				return new ProxyResponseImpl(response);
			}

		} catch (ProxyClientException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
			throw new ProxyClientException("The Proxy request specified by  "
					+ config + " failed, cause follows:", e);
		} finally {
			unbindConfig();
		}
		throw new ProxyClientException("The Proxy request specified by "
				+ config + " does not contain a valid endpoint specification ");
	}

	private void addPart(MultipartEntity multipart, String key, Object value)
			throws UnsupportedEncodingException {
		if (value instanceof String[]) {
			String[] v = (String[]) value;
			for ( String s : v) {
				multipart.addPart(key,
						new StringBody(s, Charset.forName("UTF-8")));
			}
		} else {
			multipart.addPart(key,
					new StringBody((String) value, Charset.forName("UTF-8")));
		}
	}

	private boolean isUnsafeProxyDefinition(Map<String, Object> config) {
		if (config.containsKey(ProxyPostProcessor.CONFIG_POSTPROCESSOR)) {
			String postProcessorName = (String) config
					.get(ProxyPostProcessor.CONFIG_POSTPROCESSOR);
			return !safeOpenProcessors.contains(postProcessorName);
		}
		return true;
	}


	private String processUrlTemplate(String endpointURL,
			Map<String, Object> context) throws IOException {
		Reader urlTemplateReader = new StringReader(endpointURL);
		StringWriter urlWriter = new StringWriter();
		templateService.evaluate(context, urlWriter, "urlprocessing",
				urlTemplateReader);
		return urlWriter.toString();
	}

	/**
	 * @param method
	 * @throws RepositoryException
	 */
	private void populateMessage(HttpMessage message,
			Map<String, Object> config, Map<String, Object> headers) {

		for (Entry<String, Object> header : headers.entrySet()) {
			Object o = header.getValue();
			if (o instanceof String[]) {
				for (String s : (String[]) o) {
					message.addHeader(header.getKey(), s);
				}
			} else if (o != null) {
				message.addHeader(header.getKey(), String.valueOf(o));
			}
		}

		String additionalHeaders = (String) config.get(CONFIG_PROXY_HEADER);
		if (additionalHeaders != null) {
			for (String v : StringUtils.split(additionalHeaders, ",")) {
				String[] keyVal = StringUtils.split(v, ":", 2);
				message.addHeader(keyVal[0].trim(), keyVal[1].trim());
			}
		}

	}

	/**
   *
   */
	private void unbindConfig() {
		boundConfig.set(null);
	}

	/**
	 * @param resource
	 */
	private void bindConfig(Map<String, Object> config) {
		boundConfig.set(config);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see au.edu.csu.sakai.integration.api.soapclient.ResourceSource#getResource()
	 */
	public Map<String, Object> getConfig() {
		return boundConfig.get();
	}

}
