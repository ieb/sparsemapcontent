package uk.co.tfd.sm.integration.resource;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.integration.IntegrationServer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DefaultResourceTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultResourceTest.class);
	private DefaultHttpClient defaultHttpClient;

	@Before
	public void before() throws IOException {
		IntegrationServer.start();
		defaultHttpClient = new DefaultHttpClient();

	}

	@Test
	public void testPost() throws ClientProtocolException, IOException,
			AuthenticationException {
		String resource = "/" + this.getClass().getName() + "/testPost"
				+ System.currentTimeMillis();
		String resourceUrl = IntegrationServer.BASEURL + resource;
		HttpPost post = new HttpPost(resourceUrl);
		UrlEncodedFormEntity form = new UrlEncodedFormEntity(
				Lists.newArrayList(new BasicNameValuePair("testproperty",
						"testvalue")));
		post.setEntity(form);
		HttpResponse response = defaultHttpClient.execute(post);
		String jsonBody = IOUtils.toString(response.getEntity().getContent());
		LOGGER.info("Got {} ", jsonBody);
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());

		post = new HttpPost(resourceUrl);
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				"admin", "admin");
		post.addHeader(new BasicScheme().authenticate(creds, post));
		form = new UrlEncodedFormEntity(Lists.newArrayList(
				new BasicNameValuePair("testproperty", "testvalue1"),
				new BasicNameValuePair("testproperty", "testvalue2"),
				new BasicNameValuePair("testproperty", "testvalue3"),
				new BasicNameValuePair("testproperty", "testvalue4"),
				new BasicNameValuePair("testproperty", "testvalue5"),
				new BasicNameValuePair("testint[]@Integer", "1001")
				));
		post.setEntity(form);
		response = defaultHttpClient.execute(post);
		jsonBody = IOUtils.toString(response.getEntity().getContent());
		LOGGER.info("Got {} ", jsonBody);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		HttpGet get = new HttpGet(resourceUrl + ".pp.json");
		response = defaultHttpClient.execute(get);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Assert.assertEquals("application/json",
				response.getHeaders("Content-Type")[0].getValue());
		jsonBody = IOUtils.toString(response.getEntity().getContent());
		LOGGER.info("Got {} ", jsonBody);
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(jsonBody).getAsJsonObject();
		JsonElement testProp = json.get("testproperty");
		Assert.assertNotNull(testProp);
		JsonArray properties = testProp.getAsJsonArray();
		List<String> l = Lists.newArrayList();
		for ( Iterator<JsonElement> ie = properties.iterator(); ie.hasNext(); ) {
			l.add(ie.next().getAsString());
		}
		Assert.assertArrayEquals(new String[] { "testvalue1", "testvalue2",
				"testvalue3", "testvalue4", "testvalue5" },
				l.toArray(new String[l.size()]));
		
		testProp = json.get("testint");
		Assert.assertNotNull(testProp);
		Assert.assertTrue(testProp.isJsonArray());
		properties = testProp.getAsJsonArray();
		Assert.assertEquals(1, properties.size());
		Assert.assertEquals(1001, properties.get(0).getAsInt());
		

		testProp = json.get("_path");
		Assert.assertNotNull(testProp);
		Assert.assertEquals(resource, testProp.getAsString());
	}

}
