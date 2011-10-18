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
package uk.co.tfd.sm.batch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.tfd.sm.batch.BatchServlet.REQUESTS_PARAMETER;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.batch.BatchServlet;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 *
 */
public class BatchServletTest {


  private static final Logger LOGGER = LoggerFactory.getLogger(BatchServletTest.class);

private BatchServlet servlet;
  
  @Mock
  private HttpServletRequest request;
  
  @Mock
  private HttpServletResponse response;
  
  public BatchServletTest() {
	  MockitoAnnotations.initMocks(this);
  }

  @Before
  public void setUp() throws Exception {
    servlet = new BatchServlet();

  }

  @Test
  public void testInvalidRequest() throws ServletException, IOException {
    when(request.getParameter(REQUESTS_PARAMETER)).thenReturn("marlformedparameter");
    servlet.doGet(request, response);
    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,
        "Failed to parse the " + REQUESTS_PARAMETER + " parameter");
  }

  @Test
  public void testSimpleRequest() throws Exception {
    String json = "[{\"url\" : \"/foo/bar\",\"method\" : \"POST\",\"parameters\" : {\"val\" : 123,\"val@TypeHint\" : \"Long\"}}]";
    
    when(request.getParameter(REQUESTS_PARAMETER)).thenReturn(json);
    when(request.getRemoteUser()).thenReturn("admin");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(baos);

    RequestDispatcher dispatcher = mock(RequestDispatcher.class);
    when(request.getRequestDispatcher(Mockito.anyString())).thenReturn(dispatcher);
    when(response.getWriter()).thenReturn(writer);
    servlet.doPost(request, response);
    String result = baos.toString("UTF-8");
    JsonParser jsonParser = new JsonParser();
    JsonElement parsedResult = jsonParser.parse(result);
    Assert.assertTrue(parsedResult.isJsonObject());
    Assert.assertNotNull(parsedResult.getAsJsonObject().get("results"));
    Assert.assertTrue(parsedResult.getAsJsonObject().get("results").isJsonArray());
    Assert.assertTrue(parsedResult.getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("success").getAsBoolean());
    LOGGER.info("Result {} {} ", result, parsedResult);
  }

}
