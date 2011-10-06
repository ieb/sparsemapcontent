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

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import uk.co.tfd.sm.api.proxy.ProxyResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the ProxyResponse holder by wrapping the HttpMethod request.
 */
public class ProxyResponseImpl implements ProxyResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyResponseImpl.class);
	private HttpResponse response;
	private Map<String, String[]> headers = new HashMap<String, String[]>();
	private List<InputStream> leakedInputStreams = Lists.newArrayList();
	private int code;
	private String responseCause;

	/**
	 * @param result
	 * @param method
	 */
	public ProxyResponseImpl(HttpResponse response) {
		this.response = response;
		code = response.getStatusLine().getStatusCode();
		responseCause = response.getStatusLine().getReasonPhrase();

		for (org.apache.http.Header header : response.getAllHeaders()) {
			String name = header.getName();
			String[] values = headers.get(name);
			if (values == null) {
				values = new String[] { header.getValue() };
			} else {
				String[] newValues = new String[values.length + 1];
				System.arraycopy(values, 0, newValues, 0, values.length);
				newValues[values.length] = header.getValue();
				values = newValues;
			}

			boolean add = true;
			// We ignore JSESSIONID cookies coming back.
			if (name.toLowerCase().equals("set-cookie")) {
				for (String v : values) {
					if (v.contains("JSESSIONID")) {
						add = false;
						break;
					}
				}
			}
			if (add) {
				headers.put(name, values);
			}
		}
	}

	public ProxyResponseImpl(int responseCode, String responseMessage,
			HttpResponse response) {
		this(response);
		code = responseCode;
		responseCause = responseMessage;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see uk.co.tfd.sm.api.proxy.ProxyResponse#getResultCode()
	 */
	public int getResultCode() {
		return code;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see uk.co.tfd.sm.api.proxy.ProxyResponse#getResponseHeaders()
	 */
	public Map<String, String[]> getResponseHeaders() {
		return headers;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see uk.co.tfd.sm.api.proxy.ProxyResponse#getResponseBodyAsInputStream()
	 */
	public InputStream getResponseBodyAsInputStream() throws IOException {
		InputStream in = response.getEntity().getContent();
		leakedInputStreams.add(in);
		return in;
	}
	

	/**
	 * {@inheritDoc}
	 * 
	 * @see uk.co.tfd.sm.api.proxy.ProxyResponse#getResponseBodyAsString()
	 */
	public String getResponseBodyAsString() throws IOException {
		HttpEntity entity = response.getEntity();
		if ( entity == null ) {
			return null;
		}
		String contentEncoding = getContentEncoding(entity);
		InputStream in = entity.getContent();
		BufferedReader r = new BufferedReader(new InputStreamReader(in,
				contentEncoding));
		StringBuilder sb = new StringBuilder();
		for (;;) {
			String l = r.readLine();
			if (l == null) {
				break;
			}
			sb.append(l).append("\n");
		}
		r.close();
		in.close();
		return sb.toString();
	}

	private String getContentEncoding(HttpEntity entity) {
		String contentEncoding = null;
		if ( entity == null ) {
			return "UTF-8";
		}
		Header contentEncodingHeader = entity.getContentEncoding();
		if (contentEncodingHeader != null) {
			contentEncoding = contentEncodingHeader.getValue();
		}
		if (contentEncoding == null) {
			Header contentTypeHeader = entity.getContentType();
			if (contentTypeHeader != null) {
				HeaderElement[] entries = contentTypeHeader.getElements();
				for (HeaderElement h : entries) {
					NameValuePair charset = h.getParameterByName("charset");
					if (charset != null) {
						contentEncoding = charset.getValue();
						break;
					}
				}
			}
		}
		if (contentEncoding == null) {
			contentEncoding = "UTF-8";
		}
		return contentEncoding;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see uk.co.tfd.sm.api.proxy.ProxyResponse#close()
	 */
	public void close() {
		for ( InputStream in : leakedInputStreams) {
			try {
				in.close();
			} catch (IOException e) {
				LOGGER.debug(e.getMessage(),e);
			}
		}
	}

	/**
	 * @return the cause
	 */
	public String getCause() {
		return responseCause;
	}

}
