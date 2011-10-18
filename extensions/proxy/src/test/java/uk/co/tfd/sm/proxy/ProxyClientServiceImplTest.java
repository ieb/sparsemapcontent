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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.co.tfd.sm.api.proxy.ProxyClientException;
import uk.co.tfd.sm.api.proxy.ProxyClientService;
import uk.co.tfd.sm.api.proxy.ProxyResponse;
import uk.co.tfd.sm.proxy.http.CapturedRequest;
import uk.co.tfd.sm.proxy.http.DummyServer;
import uk.co.tfd.sm.template.TemplateServiceImpl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;


/**
 *
 */
public class ProxyClientServiceImplTest {

  /**
   * 
   */
  private static final String APPLICATION_SOAP_XML_CHARSET_UTF_8 = "application/soap+xml; charset=utf-8";
  private static final String REQUEST_TEMPLATE = "<?xml version=\"1.0\"?>\n"
      + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2001/12/soap-envelope\" "
      + "soap:encodingStyle=\"http://www.w3.org/2001/12/soap-encoding\">"
      + "<soap:Body xmlns:m=\"http://www.example.org/stock\">" + "  <m:GetStockPrice>"
      + "    <m:StockName>$stockName</m:StockName>" + "  </m:GetStockPrice>"
      + "</soap:Body>" + "</soap:Envelope>";

  private static final String STOCK_NAME = "IBM";
  private static final String RESPONSE_BODY = "<?xml version=\"1.0\"?>\n"
      + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2001/12/soap-envelope\" "
      + "soap:encodingStyle=\"http://www.w3.org/2001/12/soap-encoding\"> "
      + " <soap:Body xmlns:m=\"http://www.example.org/stock\">"
      + "  <m:GetStockPriceResponse> " + "    <m:Price>34.5</m:Price>"
      + "  </m:GetStockPriceResponse>" + "</soap:Body>" + " </soap:Envelope>";
  private static DummyServer dummyServer;
  private ProxyClientServiceImpl proxyClientServiceImpl;

  @BeforeClass
  public static void beforeClass() {
    dummyServer = new DummyServer();
  }

  @AfterClass
  public static void afterClass() {
    dummyServer.close();
  }

  @Before
  public void before() throws Exception {

    proxyClientServiceImpl = new ProxyClientServiceImpl();
    Map<String, Object> props = ImmutableMap.of();
    proxyClientServiceImpl.activate(props);
    TemplateServiceImpl templateService = new TemplateServiceImpl();
    templateService.activate(props);
	proxyClientServiceImpl.templateService = templateService;
  }

  @After
  public void after() throws Exception {
    proxyClientServiceImpl.deactivate(null);
  }

  @Test
  public void testInvokeServiceMissingNode() throws ProxyClientException {

    Map<String, Object> input = Maps.newHashMap();
    Map<String, Object> headers = Maps.newHashMap();
    try {
      ProxyResponse response = proxyClientServiceImpl.executeCall(null, headers, input,
          null, 0, null);
      try {
        response.close();
      } catch (Throwable t) {

      }
      fail();
    } catch (ProxyClientException ex) {

    }
  }

  @Test
  public void testInvokeServiceNodeNoEndPoint() throws ProxyClientException {

    Map<String, Object> config = ImmutableMap.of("path", (Object) "/testing");
    Map<String, Object> input = Maps.newHashMap();
    Map<String, Object> headers = Maps.newHashMap();
    try {
      ProxyResponse response = proxyClientServiceImpl.executeCall(config, headers, input,
          null, 0, null);
      try {
        response.close();
      } catch (Throwable t) {

      }
      fail();
    } catch (ProxyClientException ex) {
    }
  }

  @Test
  public void testInvokeServiceNodeBadEndPoint() throws Exception {
    checkBadUrl("http://${url}",
        "Invalid Endpoint template, relies on request to resolve valid URL http://${url}");
    checkBadUrl("h${url}", "Invalid Endpoint template, relies on request to resolve valid URL");
    checkBadUrl("${url}",  "Invalid Endpoint template, relies on request to resolve valid URL");
  }

  private void checkBadUrl(String badUrl, String message) throws Exception {
    
    
    Map<String, Object> config = ImmutableMap.of(
    		"path", (Object)"/testing",
    		ProxyClientService.CONFIG_REQUEST_PROXY_ENDPOINT, badUrl);
    Map<String, Object> input = Maps.newHashMap();
    Map<String, Object> headers = Maps.newHashMap();
    try {
      ProxyResponse response = proxyClientServiceImpl.executeCall(config, headers, input,
          null, 0, null);
      try {
        response.close();
      } catch (Throwable t) {

      }
      fail();
    } catch (ProxyClientException ex) {
      assertEquals(message, ex.getMessage());
    }
  }

  @Test
  public void testInvokeServiceNodeEndPoint() throws ProxyClientException,
       IOException {

   
    dummyServer.setContentType(APPLICATION_SOAP_XML_CHARSET_UTF_8);
    dummyServer.setResponseBody(RESPONSE_BODY);

    
    
    Map<String, Object> config = ImmutableMap.of(
    		"path", (Object)"/testing",
    		ProxyClientService.CONFIG_REQUEST_PROXY_ENDPOINT, dummyServer.getUrl(),
    		ProxyClientService.CONFIG_REQUEST_PROXY_METHOD, "POST",
    		ProxyClientService.CONFIG_REQUEST_CONTENT_TYPE, APPLICATION_SOAP_XML_CHARSET_UTF_8,
    		ProxyClientService.CONFIG_PROXY_REQUEST_TEMPLATE, REQUEST_TEMPLATE);
    Map<String, Object> input = Maps.newHashMap();
    Map<String, Object> headers = Maps.newHashMap();
    
    input.put("stockName", STOCK_NAME);
    headers.put("SOAPAction", "");
    ProxyResponse response = proxyClientServiceImpl.executeCall(config, headers, input,
        null, 0, null);

    CapturedRequest request = dummyServer.getRequest();
    assertEquals("Method not correct ", "POST", request.getMethod());
    assertEquals("No Soap Action in request", "", request.getHeader("SOAPAction"));
    assertEquals("Incorrect Content Type in request", APPLICATION_SOAP_XML_CHARSET_UTF_8,
        request.getContentType());

    response.close();

  }

  @Test
  public void testInvokeServiceNodeEndPointPut() throws ProxyClientException,
      IOException {



    dummyServer.setContentType(APPLICATION_SOAP_XML_CHARSET_UTF_8);
    dummyServer.setResponseBody(RESPONSE_BODY);

    Map<String, Object> config = ImmutableMap.of(
    		"path", (Object)"/testing",
    		ProxyClientService.CONFIG_REQUEST_PROXY_ENDPOINT, dummyServer.getUrl(),
    		ProxyClientService.CONFIG_REQUEST_PROXY_METHOD, "PUT",
    		ProxyClientService.CONFIG_REQUEST_CONTENT_TYPE, APPLICATION_SOAP_XML_CHARSET_UTF_8,
    		ProxyClientService.CONFIG_PROXY_REQUEST_TEMPLATE, REQUEST_TEMPLATE.getBytes());
    Map<String, Object> input = Maps.newHashMap();
    Map<String, Object> headers = Maps.newHashMap();

    
    input.put("stockName", STOCK_NAME);

    byte[] bas = new byte[1024];
    for (int i = 0; i < bas.length; i++) {
      bas[i] = (byte) (i & 0xff);
    }
    ByteArrayInputStream bais = new ByteArrayInputStream(bas);
    ProxyResponse response = proxyClientServiceImpl.executeCall(config, headers, input,
        bais, bas.length, "binary/x-data");

    CapturedRequest request = dummyServer.getRequest();
    assertEquals("Method not correct ", "PUT", request.getMethod());
    assertEquals("Incorrect Content Type in request", "binary/x-data",
        request.getContentType());

    assertArrayEquals("Request Not equal ", bas, request.getRequestBodyAsByteArray());
    response.close();
  }

  @Test
  public void testInvokeServiceNodeEndPointGet() throws ProxyClientException,
      IOException {
    testRequest("GET", "GET", RESPONSE_BODY, -1);
  }

  @Test
  public void testInvokeServiceNodeEndPointGetLimit() throws ProxyClientException,
      IOException {
    testRequest("GET", "GET", RESPONSE_BODY, 1020000);
  }

  @Test
  public void testInvokeServiceNodeEndPointGetLimitLow() throws ProxyClientException,
      IOException {
    testRequest("GET", "HEAD", null, 1);
  }

  @Test
  public void testInvokeServiceNodeEndPointOptions() throws ProxyClientException,
      IOException {
    testRequest("OPTIONS", "OPTIONS", RESPONSE_BODY, -1);
  }

  @Test
  public void testInvokeServiceNodeEndPointHead() throws ProxyClientException,
      IOException {
    testRequest("HEAD", "HEAD", null, -1);
  }

  @Test
  public void testInvokeServiceNodeEndPointOther() throws ProxyClientException,
      IOException {
    testRequest(null, "GET", RESPONSE_BODY, -1);
  }

  private void testRequest(String type, String expectedMethod, String body, long limit)
      throws ProxyClientException, IOException {





    dummyServer.setContentType(APPLICATION_SOAP_XML_CHARSET_UTF_8);
    dummyServer.setResponseBody(body);

    Builder<String, Object> b = ImmutableMap.builder();
    b.put("path", "/testing");
    b.put(ProxyClientService.CONFIG_REQUEST_PROXY_ENDPOINT, dummyServer.getUrl());
    if ( type != null ) {
    	b.put(ProxyClientService.CONFIG_REQUEST_PROXY_METHOD, type);
    }
    if ( limit != -1 ) {
    	b.put(ProxyClientService.CONFIG_LIMIT_GET_SIZE, limit);
    }
    b.put(ProxyClientService.CONFIG_REQUEST_CONTENT_TYPE, APPLICATION_SOAP_XML_CHARSET_UTF_8);
    b.put(ProxyClientService.CONFIG_PROXY_REQUEST_TEMPLATE, REQUEST_TEMPLATE.getBytes());
    Map<String, Object> config = b.build();
    Map<String, Object> input = Maps.newHashMap();
    Map<String, Object> headers = Maps.newHashMap();
    input.put("stockName", STOCK_NAME);

    ProxyResponse response = proxyClientServiceImpl.executeCall(config, headers, input,
        null, 0, null);

    CapturedRequest request = dummyServer.getRequest();
    assertEquals("Method not correct ", expectedMethod, request.getMethod());
    assertEquals("Incorrect Content Type in request", null, request.getContentType());

    assertEquals(type + "s dont have request bodies ", null,
        request.getRequestBodyAsByteArray());
    if ( body == null ) {
    	Assert.assertNull(response.getResponseBodyAsString());
    } else {
	    assertEquals(body, response.getResponseBodyAsString().trim());
	    assertEquals(APPLICATION_SOAP_XML_CHARSET_UTF_8,
	        response.getResponseHeaders().get("Content-Type")[0]);
	}
    response.close();
  }

}
