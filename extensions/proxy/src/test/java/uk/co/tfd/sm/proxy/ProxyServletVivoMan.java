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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.proxy.ProxyPostProcessor;
import uk.co.tfd.sm.api.proxy.ProxyPreProcessor;
import uk.co.tfd.sm.template.TemplateServiceImpl;

import com.google.common.collect.ImmutableMap;

/**
 * Needs a vivo instance to be available.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProxyServletVivoMan {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServletVivoMan.class);

	private ProxyServlet servlet;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	private Vector<String> headerNames;


	private Map<String, ProxyPreProcessor> proxyPreProcessors;

	@Mock
	private ProxyPreProcessor proxyPreProcessor;

	private Map<String, ProxyPostProcessor> proxyPostProcessors;

	@Mock
	private ProxyPostProcessor proxyPostProcessor;


	private ProxyClientServiceImpl proxyClientService;

	@Before
	public void setup() throws Exception {
		servlet = new ProxyServlet();
		servlet.activate(ImmutableMap.of(
				ProxyServlet.PROP_TEMPLATE_PATH,
				(Object) "src/test/config"));
		headerNames = new Vector<String>();
		proxyPreProcessors = new HashMap<String, ProxyPreProcessor>();
		proxyPreProcessors.put("rss", proxyPreProcessor);
		proxyPostProcessors = new HashMap<String, ProxyPostProcessor>();
		proxyPostProcessors.put("rss", proxyPostProcessor);
		

		
		proxyClientService = new ProxyClientServiceImpl();
		TemplateServiceImpl templateService = new TemplateServiceImpl();
		templateService.activate(ImmutableMap.of("x", (Object)"y"));
		proxyClientService.templateService = templateService;
		proxyClientService.activate(ImmutableMap.of("x",(Object)"y"));
		servlet.proxyClientService = proxyClientService;

	}

	@Test
	public void testVivoFeed() throws Exception {

		when(request.getPathInfo()).thenReturn("/tests/vivo");
		when(request.getHeaderNames()).thenReturn(headerNames.elements());
		
		Map<String, String[]> requestParameters = ImmutableMap.of("id", new String[]{"n7934"});
		when(request.getParameterMap()).thenReturn(requestParameters );
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
			
			@Override
			public void write(int v) throws IOException {
				byteArrayOutputStream.write(v);
			}
		});

		long s = System.currentTimeMillis();
		servlet.service(request, response);
		LOGGER.info("Took {} ",(System.currentTimeMillis()-s));

		verify(response, Mockito.atMost(0)).sendError(404);
		
		
		String output = byteArrayOutputStream.toString("UTF-8");
		LOGGER.info("Got Response {} ",output);
	}


}
