/**
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
package uk.co.tfd.sm.http.batch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.memory.Cache;
import org.sakaiproject.nakamura.api.memory.CacheManagerService;
import org.sakaiproject.nakamura.api.memory.CacheScope;

import uk.co.tfd.sm.api.template.TemplateService;

@Component(immediate = true, metatype = true)
@Service(value = Servlet.class)
@Property(name = "alias", value = "/system/batch")
public class BatchServlet extends HttpServlet {

	private static final String REQUEST_TEMPLATE = "t";

	private static final long serialVersionUID = 419598445499567027L;

	protected static final String REQUESTS_PARAMETER = "requests";

	private BatchProcessor batchProcessor;

	@Reference
	protected TemplateService templateService;
	
	@Reference
	protected CacheManagerService cacheManagerService;
	
	
	@Activate
	public void activate(Map<String, Object> properties) throws FileNotFoundException, IOException {
		Cache<String> cache = cacheManagerService.getCache(BatchProcessor.class.getName(), CacheScope.INSTANCE);		
		batchProcessor = new BatchProcessor(cache);
		
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		batchRequest(request, response, false);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		batchRequest(request, response, true);
	}

	@Override
	protected void doDelete(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.apache.sling.api.servlets.SlingAllMethodsServlet#doPut(org.apache.sling.api.SlingHttpServletRequest,
	 *      org.apache.sling.api.SlingHttpServletResponse)
	 */
	@Override
	protected void doPut(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * Takes the original request and starts the batching.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	@SuppressWarnings("unchecked")
	protected void batchRequest(HttpServletRequest request,
      HttpServletResponse response, boolean allowModify) throws IOException, ServletException {
    String json = request.getParameter(REQUESTS_PARAMETER);
    String template = request.getParameter(REQUEST_TEMPLATE);
    if ( template != null && template.length() > 0 ) {
    	if ( templateService.checkTemplateExists(template)) {
    		StringWriter processedTemplate = new StringWriter();
    		templateService.process(request.getParameterMap(), "UTF-8", processedTemplate, template);
    		json = processedTemplate.toString();
    	} else {
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Template specified in request parameter t does not exist");
    	}
    }
    
    
    batchProcessor.batchRequest(request, response, json, allowModify);
  }

}
