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

package uk.co.tfd.sm.http.batch;


import java.net.MalformedURLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class used to hold information about a request. eg: type, parameters, url..
 */
public class RequestInfo {

  private static final String ALLOWED_URL_CHARS = "$-_.+!*'(),/?&:;=@% ~^";
  private static final Set<String> VALID_METHODS = ImmutableSet.of("GET","POST","PUT","DELETE","OPTIONS","HEAD");
  private static final Set<String> VALID_SAFE_METHODS = ImmutableSet.of("GET","HEAD");
  private String url;
  private String method;
  private Map<String, String[]> parameters;

  public RequestInfo(String url, Map<String, String[]> parameters) throws MalformedURLException {
    setUrl(url);
    parameters = ImmutableMap.copyOf(parameters);
  }

  /**
   * Set a default requestinfo object.
   */
  public RequestInfo() {
  }

  /**
   * Get a RequestInfo object created from a JSON block. This json object has to be in the
   * form of
   * 
   * <pre>
   * [
   * {
   *   "url" : "/foo/bar",
   *   "method" : "POST",
   *   "parameters\" : {
   *     "val" : 123,
   *     "val@TypeHint" : "Long"
   *   }
   * },
   * {
   *   "url" : "/_user/a/ad/admin/public/authprofile.json",
   *   "method" : "GET"
   * }
   * ]
   * </pre>
   * 
   * @param obj
   *          The JSON object containing the information to base this RequestInfo on.
   * @throws JSONException
   *           The JSON object could not be interpreted correctly.
   * @throws MalformedURLException
   */
  public RequestInfo(JsonObject obj) throws MalformedURLException {
	  if ( obj.has("url") ) {
		  setUrl(obj.get("url").getAsString());
	  }
	  if ( obj.has("method")) {
		  setMethod(obj.get("method").getAsString());
	  } else {
		  method = "GET";
	  }

	Builder<String, String[]> builder = ImmutableMap.builder();
    if (obj.has("parameters")) {

      JsonObject data = obj.get("parameters").getAsJsonObject();

      for ( Entry<String, JsonElement> e : data.entrySet()) {
        String k = e.getKey();
        JsonElement val = e.getValue();
        if (val.isJsonArray()) {
          JsonArray arr = val.getAsJsonArray();
          String[] par = new String[arr.size()];
          for (int i = 0; i < arr.size(); i++) {
            par[i] = arr.get(i).getAsString();
          }
          builder.put(k, par);
        } else {
          String[] par = { val.toString() };
          builder.put(k, par);
        }
      }
    }
    parameters = builder.build();

  }

  /**
   * @param url
   *          The url where to fire a request on.
   * @throws MalformedURLException
   */
  public void setUrl(String url) throws MalformedURLException {
    checkValidUrl(url);
    this.url = url;
  }

  private void checkValidUrl(String url) throws MalformedURLException {
    for( char c : url.toCharArray()) {
      if ( !Character.isLetterOrDigit(c)) {
        if ( ALLOWED_URL_CHARS.indexOf(c) < 0 ) {
          throw new MalformedURLException("Invalid Character in URL request "+url+" character was 0x"+Integer.toHexString(c));
        }
      }
    }
  }

  /**
   * @return The to fire a request to.
   */
  public String getUrl() {
    return url;
  }


  public Map<String, String[]> getParameters() {
    return parameters;
  }

  /**
   * @param method
   *          the method to set
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * @return the method
   */
  public String getMethod() {
    return method;
  }

  public boolean isSafe() {
    return (method != null && VALID_SAFE_METHODS.contains(method));
  }
  
  public boolean isValid() {
	  return method != null && VALID_METHODS.contains(method) && method != null && url != null;
  }
  

}
