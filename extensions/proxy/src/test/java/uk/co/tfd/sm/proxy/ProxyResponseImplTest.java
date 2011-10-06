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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ProxyResponseImplTest {

	@Mock
	private HttpResponse response;
	
	@Mock
	private StatusLine statusLine;
	
	@Before
	public void before() {
		when(response.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(200);
		when(statusLine.getReasonPhrase()).thenReturn("OK");
	}

	@Test
	public void constructWithMultiValuedHeader() {
		when(response.getAllHeaders()).thenReturn(
				new Header[] { new BasicHeader("Accept", "text/plain"),
						new BasicHeader("Accept", "text/html") });
		ProxyResponseImpl proxyResponse = new ProxyResponseImpl(200, "Ok",
				response);

		Map<String, String[]> proxyResponseHeaders = proxyResponse
				.getResponseHeaders();
		assertEquals(2, proxyResponseHeaders.get("Accept").length);
	}

	@Test
	public void accessorsJustHandBackWhatIsOnTheMethodObject() throws Exception {
		// given
		int result = 200;
		methodHasAResponseBody();
		when(response.getAllHeaders()).thenReturn(
				new Header[0]);

		// when
		ProxyResponseImpl proxyResponse = new ProxyResponseImpl(200, "ok",
				response);

		// then
		assertEquals(result, proxyResponse.getResultCode());
		IOUtils.toString(response.getEntity().getContent());
		assertEquals(IOUtils.toString(response.getEntity().getContent()),
				proxyResponse.getResponseBodyAsString().trim());
	}

	@Test
	public void throwsAwayJSESSIONIDCookie() {
		// given
		when(response.getAllHeaders()).thenReturn(
				new Header[] {
				new BasicHeader("set-cookie", "supercoolness=extreme"),
				new BasicHeader("set-cookie", "JSESSIONID-30sdkf2-3dkfjsie") });

		// when
		ProxyResponseImpl proxyResponse = new ProxyResponseImpl(200, "OK",
				response);

		// then
		assertTrue(proxyResponse.getResponseHeaders().containsKey("set-cookie"));
		for (String headerValue : proxyResponse.getResponseHeaders().get(
				"set-cookie")) {
			assertFalse(headerValue.contains("JSESSIONID"));
		}
	}
	
	private void methodHasAResponseBody() throws Exception {
		String body = "Hello, world.";
		when(response.getEntity()).thenReturn(new StringEntity(body));
	}

}
