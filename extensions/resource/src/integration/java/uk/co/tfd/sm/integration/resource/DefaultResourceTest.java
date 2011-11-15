package uk.co.tfd.sm.integration.resource;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.integration.IntegrationServer;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DefaultResourceTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResourceTest.class);
	private DefaultHttpClient defaultHttpClient;

	@Before
	public void before() throws IOException {
		IntegrationServer.start();
		defaultHttpClient = new DefaultHttpClient();

	}

	@Test
	public void testPost() throws ClientProtocolException, IOException, AuthenticationException {
		String resource = IntegrationServer.BASEURL + "/"
		+ this.getClass().getName() + "/testPost"+System.currentTimeMillis();
		HttpPost post = new HttpPost(resource);
		UrlEncodedFormEntity form = new UrlEncodedFormEntity(Lists.newArrayList(
				new BasicNameValuePair("testproperty", "testvalue")));
		post.setEntity(form);
		HttpResponse response = defaultHttpClient.execute(post);
		String jsonBody = IOUtils.toString(response.getEntity().getContent());
		LOGGER.info("Got {} ",jsonBody);
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		
		post = new HttpPost(resource);
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials("admin", "admin");
		post.addHeader(new BasicScheme().authenticate(creds, post));
		form = new UrlEncodedFormEntity(Lists.newArrayList(
				new BasicNameValuePair("testproperty", "testvalue")));
		post.setEntity(form);
		response = defaultHttpClient.execute(post);
		jsonBody = IOUtils.toString(response.getEntity().getContent());
		LOGGER.info("Got {} ",jsonBody);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
	
		HttpGet get = new HttpGet(resource+".pp.json");
		response = defaultHttpClient.execute(get);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals("application/json", response.getHeaders("Content-Type")[0].getValue());
		jsonBody = IOUtils.toString(response.getEntity().getContent());
		LOGGER.info("Got {} ",jsonBody);
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(jsonBody).getAsJsonObject();
		JsonElement testProp = json.get("testproperty");
		Assert.assertNotNull(testProp);
		Assert.assertEquals("testvalue", testProp.getAsString());
	}


}
