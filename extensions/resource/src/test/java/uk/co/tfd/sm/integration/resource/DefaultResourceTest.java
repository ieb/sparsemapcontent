package uk.co.tfd.sm.integration.resource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.co.tfd.sm.integration.HttpTestUtils;
import uk.co.tfd.sm.integration.IntegrationServer;
import uk.co.tfd.sm.integration.JsonTestUtils;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DefaultResourceTest {

	private static final String ADMIN_USER = "admin";
	private static final String ADMIN_PASSWORD = "admin";
	private static final String APPLICATION_JSON = "application/json; charset=utf-8";
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private Random random;
	private HttpTestUtils httpTestUtils = new HttpTestUtils();

	public DefaultResourceTest() throws IOException {
		IntegrationServer.start();
	}

	@Before
	public void before() throws IOException {
		random = new Random(0xDEADBEEF);
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

		httpTestUtils.execute(post, 403, null);

		post = new HttpPost(resourceUrl);
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				ADMIN_USER, ADMIN_PASSWORD);
		post.addHeader(new BasicScheme().authenticate(creds, post));
		form = new UrlEncodedFormEntity(Lists.newArrayList(
				new BasicNameValuePair("testproperty", "testvalue1"),
				new BasicNameValuePair("testproperty", "testvalue2"),
				new BasicNameValuePair("testproperty", "testvalue3"),
				new BasicNameValuePair("testproperty", "testvalue4"),
				new BasicNameValuePair("testproperty", "testvalue5"),
				new BasicNameValuePair("testint[]@Integer", "1001")));
		post.setEntity(form);
		post.setHeader("Referer","/integratriontest/"+this.getClass().getName());
		httpTestUtils.execute(post, 200, APPLICATION_JSON);

		JsonObject json = JsonTestUtils.toJsonObject(httpTestUtils.get(
				resourceUrl + ".pp.json", 200, APPLICATION_JSON));

		JsonTestUtils.checkProperty(json, "testproperty", new String[] {
				"testvalue1", "testvalue2", "testvalue3", "testvalue4",
				"testvalue5" });

		JsonTestUtils.checkProperty(json, "testint", new int[] { 1001 });

		JsonTestUtils.checkProperty(json, "_path", resource);
	}


	@Test
	public void testUpload() throws ClientProtocolException, IOException,
			AuthenticationException {
		String resource = "/" + this.getClass().getName() + "/testUpload"
				+ System.currentTimeMillis();
		String resourceUrl = IntegrationServer.BASEURL + resource;
		HttpPost post = new HttpPost(resourceUrl);
		MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity
				.addPart("title", new StringBody("TestUploadFail", UTF8));
		multipartEntity.addPart("desc", new StringBody("TestUploadFail", UTF8));
		byte[] b = new byte[10240];
		random.nextBytes(b);
		ByteArrayBody bab = new ByteArrayBody(b, "testUpload.bin", "test/bin");
		multipartEntity.addPart("fileX", bab);

		post.setEntity(multipartEntity);

		httpTestUtils.execute(post, 403, null);

		// do it again and authenticate
		post = new HttpPost(resourceUrl);
		multipartEntity = new MultipartEntity();
		multipartEntity.addPart("title", new StringBody("TestUploadPassTitle",
				UTF8));
		multipartEntity.addPart("desc", new StringBody("TestUploadPass", UTF8));
		bab = new ByteArrayBody(b, "test/bin", "testUpload.bin");
		multipartEntity.addPart("fileA", bab);

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				ADMIN_USER, ADMIN_PASSWORD);
		post.addHeader(new BasicScheme().authenticate(creds, post));
		post.setEntity(multipartEntity);
		post.setHeader("Referer","/integratriontest/"+this.getClass().getName());
		JsonElement jsonElement = httpTestUtils.execute(post, 200,
				APPLICATION_JSON);
		System.err.println(jsonElement);
		Set<String> responseSet = JsonTestUtils.toResponseSet(jsonElement);

		Assert.assertTrue(responseSet.contains("Multipart Upload"));
		Assert.assertTrue(responseSet.contains("Added title"));
		Assert.assertTrue(responseSet.contains("Added desc"));
		Assert.assertTrue(responseSet.contains("Saved Stream testUpload.bin"));

		HttpResponse response = httpTestUtils.get(resourceUrl + "/testUpload.bin");
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		byte[] responseBody = IOUtils.toByteArray(response.getEntity()
				.getContent());
		Assert.assertArrayEquals(b, responseBody);

		// subpaths that dont exist should give 404 on GET
		httpTestUtils.get(resourceUrl + "/testUpload.bin/subpath.pp.json", 404, null);

		JsonObject fileProperties = JsonTestUtils.toJsonObject(httpTestUtils
				.get(resourceUrl + "/testUpload.bin.pp.json", 200, APPLICATION_JSON));

		// we should get the object we asked for
		JsonTestUtils.checkProperty(fileProperties, "_path", resource
				+ "/testUpload.bin");

		JsonTestUtils.checkProperty(fileProperties, "desc", "TestUploadPass");
		JsonTestUtils.checkProperty(fileProperties, "title",
				"TestUploadPassTitle");

	}

}
