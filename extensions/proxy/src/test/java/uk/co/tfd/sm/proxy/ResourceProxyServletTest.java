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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import uk.co.tfd.sm.api.proxy.ProxyClientException;
import uk.co.tfd.sm.api.proxy.ProxyClientService;
import uk.co.tfd.sm.api.proxy.ProxyPostProcessor;
import uk.co.tfd.sm.api.proxy.ProxyPreProcessor;
import uk.co.tfd.sm.api.proxy.ProxyResponse;

import com.google.common.collect.ImmutableMap;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceProxyServletTest {

	private ProxyServlet servlet;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	private Vector<String> headerNames;

	@Mock
	private ProxyClientService proxyClientService;

	@Mock
	private ProxyResponse proxyResponse;

	private Map<String, ProxyPreProcessor> proxyPreProcessors;

	@Mock
	private ProxyPreProcessor proxyPreProcessor;

	private Map<String, ProxyPostProcessor> proxyPostProcessors;

	@Mock
	private ProxyPostProcessor proxyPostProcessor;

	@Captor
	private ArgumentCaptor<Map<String, Object>> configCaptor;

	@Captor
	private ArgumentCaptor<Map<String, Object>> headersCaptor;

	@Captor
	private ArgumentCaptor<Map<String, Object>> inputMapCaptor;

	@Captor
	private ArgumentCaptor<InputStream> requestInputStreamCaptor;

	@Captor
	private ArgumentCaptor<Long> requestContentLengthCaptor;

	@Captor
	private ArgumentCaptor<String> requestContentTypeCaptor;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws ProxyClientException, UnsupportedEncodingException, IOException {
		servlet = new ProxyServlet();
		servlet.activate(ImmutableMap.of(
				ProxyServlet.PROP_TEMPLATE_PATH,
				(Object) "src/test/config"));
		headerNames = new Vector<String>();
		proxyPreProcessors = new HashMap<String, ProxyPreProcessor>();
		proxyPreProcessors.put("rss", proxyPreProcessor);
		proxyPostProcessors = new HashMap<String, ProxyPostProcessor>();
		proxyPostProcessors.put("rss", proxyPostProcessor);
		
		
		when(request.getPathInfo()).thenReturn("/tests/test1");
		when(request.getHeaderNames()).thenReturn(headerNames.elements());
		when(
				proxyClientService.executeCall(Mockito.anyMap(),
						Mockito.anyMap(), Mockito.anyMap(),
						Mockito.any(InputStream.class), Mockito.anyLong(),
						Mockito.anyString())).thenReturn(proxyResponse);
		when(proxyResponse.getResponseBodyAsInputStream()).thenReturn(
				new ByteArrayInputStream("TestData".getBytes("UTF-8")));
		when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
			
			@Override
			public void write(int arg0) throws IOException {
			}
		});

	}

	@Test
	public void returnsAProxiedGet() throws Exception {

		servlet.proxyClientService = proxyClientService;
		servlet.service(request, response);

		verify(response, Mockito.atMost(0)).sendError(404);
		verify(proxyClientService).executeCall(configCaptor.capture(),
				headersCaptor.capture(), inputMapCaptor.capture(),
				requestInputStreamCaptor.capture(),
				requestContentLengthCaptor.capture(),
				requestContentTypeCaptor.capture());
	}

	@Test
	public void canDoHeaderBasicAuth() throws Exception {
		servlet.proxyClientService = proxyClientService;

		// when
		servlet.service(request, response);
		
		verify(response, Mockito.atMost(0)).sendError(404);
		verify(proxyClientService).executeCall(configCaptor.capture(),
				headersCaptor.capture(), inputMapCaptor.capture(),
				requestInputStreamCaptor.capture(),
				requestContentLengthCaptor.capture(),
				requestContentTypeCaptor.capture());
	}

	@Test
	public void canDoParameterBasicAuth() throws Exception {
		servlet.proxyClientService = proxyClientService;

		// when
		servlet.service(request, response);
		verify(response, Mockito.atMost(0)).sendError(404);
		verify(proxyClientService).executeCall(configCaptor.capture(),
				headersCaptor.capture(), inputMapCaptor.capture(),
				requestInputStreamCaptor.capture(),
				requestContentLengthCaptor.capture(),
				requestContentTypeCaptor.capture());
	}

	@Test
	public void canPostWithAContentBody() throws Exception {
		servlet.proxyClientService = proxyClientService;

		// when
		servlet.service(request, response);
		verify(response, Mockito.atMost(0)).sendError(404);
		verify(proxyClientService).executeCall(configCaptor.capture(),
				headersCaptor.capture(), inputMapCaptor.capture(),
				requestInputStreamCaptor.capture(),
				requestContentLengthCaptor.capture(),
				requestContentTypeCaptor.capture());
	}

	@Test
	public void canPutWithAContentBody() throws Exception {
		servlet.proxyClientService = proxyClientService;

		// when
		servlet.service(request, response);
		verify(response, Mockito.atMost(0)).sendError(404);
		verify(proxyClientService).executeCall(configCaptor.capture(),
				headersCaptor.capture(), inputMapCaptor.capture(),
				requestInputStreamCaptor.capture(),
				requestContentLengthCaptor.capture(),
				requestContentTypeCaptor.capture());
	}

	@Test
	public void conveysParamsToTheProxy() throws Exception {
		servlet.proxyClientService = proxyClientService;

		// when
		servlet.service(request, response);
		verify(response, Mockito.atMost(0)).sendError(404);
		verify(proxyClientService).executeCall(configCaptor.capture(),
				headersCaptor.capture(), inputMapCaptor.capture(),
				requestInputStreamCaptor.capture(),
				requestContentLengthCaptor.capture(),
				requestContentTypeCaptor.capture());
	}


}
