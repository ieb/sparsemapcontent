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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.proxy.ProxyPostProcessor;
import uk.co.tfd.sm.api.proxy.ProxyResponse;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Will verify two required templateParams (i.e. <code>hostname</code> and
 * <code>port</code>) and then process.
 * 
 * 1) Added a new PostProcessor called <code>TrustedLoginTokenProxyPostProcessor</code>
 * and added it to the list of trusted PostProcessors in
 * <code>ProxyClientServiceImpl</code>. <br/>
 * 2) <code>TrustedLoginTokenProxyPostProcessor</code> (i.e. "
 * <code>trustedLoginTokenProxyPostProcessor</code>") is now bound to the two proxy nodes:
 * a)
 * <code>bundles/proxy/src/main/resources/SLING-INF/content/var/proxy/s23/site.json</code>
 * and b)
 * <code>bundles/proxy/src/main/resources/SLING-INF/content/var/proxy/s23/sites.json</code>
 * .<br/>
 * 3) <code>TrustedLoginTokenProxyPostProcessor</code> now verifies that the ${hostname}
 * and ${port} values are required and MUST match the values for these two variables as
 * defined in the PreProcessor: <code>TrustedLoginTokenProxyPreProcessor</code>.
 * 
 * This should prevent any shenanigans with this particular proxy. L
 */
@Service
@Component
@Properties(value = {
    @Property(name = "service.vendor", value = "The Sakai Foundation"),
    @Property(name = "service.description", value = "Will verify two required templateParams (i.e. hostname and port) and then process.") })
public class TrustedLoginTokenProxyPostProcessor implements ProxyPostProcessor {
  public static final String NAME = "trustedLoginTokenProxyPostProcessor";
  protected transient DefaultProxyPostProcessorImpl dpppi = new DefaultProxyPostProcessorImpl();

  @Reference
  protected transient TrustedLoginTokenProxyPreProcessor tltppp;

  private static final Logger LOG = LoggerFactory
      .getLogger(TrustedLoginTokenProxyPostProcessor.class);

  /**
   * {@inheritDoc}
   * 
   * @see uk.co.tfd.sm.api.proxy.ProxyPostProcessor#process(java.util.Map,
   *      org.apache.sling.api.SlingHttpServletResponse,
   *      uk.co.tfd.sm.api.proxy.ProxyResponse)
   */
  public void process(Map<String, Object> config, Map<String, Object> templateParams,
      HttpServletResponse response, ProxyResponse proxyResponse) throws IOException {
    LOG.debug(
        "process(Map<String, Object> {}, SlingHttpServletResponse response, ProxyResponse proxyResponse)",
        templateParams);
    if (templateParams == null || !tltppp.hostname.equals(templateParams.get("hostname"))
        || tltppp.port != (Integer) templateParams.get("port")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    // just use DefaultProxyPostProcessorImpl behavior
    dpppi.process(config, templateParams, response, proxyResponse);
    return;
  }

  /**
   * {@inheritDoc}
   * 
   * @see uk.co.tfd.sm.api.proxy.ProxyPostProcessor#getName()
   */
  public String getName() {
    return NAME;
  }

}
