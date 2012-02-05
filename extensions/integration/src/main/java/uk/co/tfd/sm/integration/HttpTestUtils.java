package uk.co.tfd.sm.integration;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class HttpTestUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTestUtils.class);
	private HttpClient defaultHttpClient;

	public HttpTestUtils() {
		defaultHttpClient = new DefaultHttpClient();
	}

	public JsonElement execute(HttpUriRequest post, int code,
			String contentType) throws ClientProtocolException, IOException {
		 return execute(post, code, contentType, false);
	}

	public JsonElement execute(HttpUriRequest post, int code,
			String contentType, boolean echo) throws ClientProtocolException, IOException {
		post.setHeader("Referer", "/integrationtests");
		HttpResponse response = defaultHttpClient.execute(post);
		Assert.assertEquals(code, response.getStatusLine().getStatusCode());
		if (code >= 200 && code < 300) {
			Assert.assertEquals(contentType,
					response.getHeaders("Content-Type")[0].getValue());
			String jsonBody = IOUtils.toString(response.getEntity()
					.getContent());
			if ( echo ) {
				LOGGER.info("Got {} ", jsonBody);
			}
			JsonParser parser = new JsonParser();
			return parser.parse(jsonBody);
		}
		IOUtils.toString(response.getEntity().getContent());

		return null;
	}

	public JsonElement get(String uri, int code,
			String contentType) throws ClientProtocolException, IOException {
		return execute(new HttpGet(uri), code, contentType);
	}

	public JsonElement get(String uri, int code,
			String contentType, boolean echo) throws ClientProtocolException, IOException {
		return execute(new HttpGet(uri), code, contentType, echo );
	}

	public HttpResponse execute(HttpUriRequest request) throws ClientProtocolException, IOException {
		return defaultHttpClient.execute(request);
	}
	public HttpResponse get(String request) throws ClientProtocolException, IOException {
		return defaultHttpClient.execute(new HttpGet(request));
	}
	

}
