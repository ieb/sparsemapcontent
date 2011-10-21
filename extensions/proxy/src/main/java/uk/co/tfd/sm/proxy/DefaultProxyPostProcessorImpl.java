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


import uk.co.tfd.sm.api.proxy.ProxyPostProcessor;
import uk.co.tfd.sm.api.proxy.ProxyResponse;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 *
 */
public class DefaultProxyPostProcessorImpl implements ProxyPostProcessor {

  /**
   * {@inheritDoc}
   * @throws IOException
   * @see uk.co.tfd.sm.api.proxy.ProxyPostProcessor#process(org.apache.sling.api.SlingHttpServletResponse, uk.co.tfd.sm.api.proxy.ProxyResponse)
   */
  public void process(Map<String, Object> config, Map<String, Object> templateParams, 
      HttpServletResponse response, ProxyResponse proxyResponse) throws IOException {
    for (Entry<String, String[]> h : proxyResponse.getResponseHeaders().entrySet()) {
      for (String v : h.getValue()) {
        response.setHeader(h.getKey(), v);
      }
    }
    int code = proxyResponse.getResultCode();
    response.setStatus(code);
    IOUtils.copy(proxyResponse.getResponseBodyAsInputStream(), response
          .getOutputStream());
  }

  /**
   * {@inheritDoc}
   * @see uk.co.tfd.sm.api.proxy.ProxyPostProcessor#getName()
   */
  public String getName() {
    return "default";
  }

}
