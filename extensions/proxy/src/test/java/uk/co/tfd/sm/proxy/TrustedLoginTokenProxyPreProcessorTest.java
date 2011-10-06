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
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TrustedLoginTokenProxyPreProcessorTest {

  private final String secret = "e2KS54H35j6vS5Z38nK40";

  private TrustedLoginTokenProxyPreProcessor proxyPreProcessor;

  @Mock
  HttpServletRequest request;
  
  
  Map<String, Object> headers;

  Map<String, Object> templateParams;

  @Before
  public void setup() throws Exception {
    proxyPreProcessor = new TrustedLoginTokenProxyPreProcessor();
    headers = Maps.newHashMap();
    templateParams = Maps.newHashMap();
  }

  @Test
  public void nameIsAsExpected() {
    assertEquals("trusted-token", proxyPreProcessor.getName());
  }

  @Test
  public void processorAddsValidHashToHeaders() throws Exception {
    // when
    proxyPreProcessor.preProcessRequest(request, headers, templateParams);

    // then
    assertNotNull(headers
        .get(TrustedLoginTokenProxyPreProcessor.SECURE_TOKEN_HEADER_NAME));
    String[] tokenParts = ((String) headers.get(
        TrustedLoginTokenProxyPreProcessor.SECURE_TOKEN_HEADER_NAME)).split(";");
    String theirHash = tokenParts[0];
    assertEquals(theirHash, myHash(tokenParts));

  }
  
  @Test
  public void reflectsPortParameterAsConfigured() {
    Map<String, Object> props = ImmutableMap.of("port",(Object)"8080");
	proxyPreProcessor.activate(props);
    proxyPreProcessor.preProcessRequest(request, headers, templateParams);
    
    // then
    assertEquals(8080, templateParams.get("port"));
  }


  private String myHash(String[] tokenParts) throws Exception {
    String user = tokenParts[1];
    String timestamp = tokenParts[2];
    final String message = user
        + TrustedLoginTokenProxyPreProcessor.TOKEN_SEPARATOR + timestamp;
    final String hmac = Signature.calculateRFC2104HMAC(message, secret);
    return hmac;
  }


}
