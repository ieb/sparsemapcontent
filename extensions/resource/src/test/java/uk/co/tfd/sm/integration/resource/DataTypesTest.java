package uk.co.tfd.sm.integration.resource;

import java.io.IOException;
import java.util.List;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import uk.co.tfd.sm.integration.HttpTestUtils;
import uk.co.tfd.sm.integration.IntegrationServer;
import uk.co.tfd.sm.integration.JsonTestUtils;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

public class DataTypesTest {

	private static final String ADMIN_USER = "admin";
	private static final String ADMIN_PASSWORD = "admin";
	private static final String APPLICATION_JSON = "application/json; charset=utf-8";
	private HttpTestUtils httpTestUtils = new HttpTestUtils();

	public DataTypesTest() throws IOException {
		IntegrationServer.start();
	}


	@Test
	public void testBooleanType() throws AuthenticationException,
			ClientProtocolException, IOException {
		Boolean testsingle = true;
		Boolean[] testproperty = new Boolean[] { true,
				false, true, true };
		Boolean testarray = false;
		JsonObject json = testType("Boolean", testsingle, testproperty,
				testarray);
		JsonTestUtils.checkProperty(json, "testproperty", testproperty);
		JsonTestUtils.checkProperty(json, "testarray", new Boolean[] {testarray});
		JsonTestUtils.checkProperty(json, "testsingle", testsingle);
	}

	@Test
	public void testCalendarType() throws AuthenticationException,
			ClientProtocolException, IOException {
		String testsingle = "2011-01-30";
		String[] testproperty = new String[] { "2011-02-20", "2011-03-21", "2011-03-22",
				"2011-04-23T11:23:13+11:30" };
		String testarray = "2011-06-30T09:12:01Z";
		JsonObject json = testType("Calendar", testsingle, testproperty,
				testarray);
		JsonTestUtils.checkProperty(json, "testproperty", testproperty);
		JsonTestUtils.checkProperty(json, "testarray", new String[] {testarray});
		JsonTestUtils.checkProperty(json, "testsingle", testsingle);

	}

	@Test
	public void testDoubleType() throws AuthenticationException,
			ClientProtocolException, IOException {
		Double testsingle = 1000010010100102010210.0023;
		Double[] testproperty = new Double[] { 1.01, -1.02, 1.03, -1.04 };
		Double testarray = 2.2;
		JsonObject json = testType("Double", testsingle, testproperty,
				testarray);
		JsonTestUtils.checkProperty(json, "testproperty", testproperty);
		JsonTestUtils.checkProperty(json, "testarray", new Double[]{testarray});
		JsonTestUtils.checkProperty(json, "testsingle", testsingle);
	}

	@Test
	public void testIntegerType() throws AuthenticationException,
			ClientProtocolException, IOException {
		Integer testsingle = 1001;
		Integer[] testproperty = new Integer[] { 101, -102, 103, -104 };
		Integer testarray = 22;
		JsonObject json = testType("Integer", testsingle, testproperty,
				testarray);
		JsonTestUtils.checkProperty(json, "testproperty", testproperty);
		JsonTestUtils.checkProperty(json, "testarray", new Integer[]{testarray});
		JsonTestUtils.checkProperty(json, "testsingle", testsingle);
	}


	@Test
	public void testLongType() throws AuthenticationException,
			ClientProtocolException, IOException {
		Long testsingle = Long.MAX_VALUE;
		Long[] testproperty = new Long[] { 101L, Long.MIN_VALUE, 103L, Long.MAX_VALUE };
		Long testarray = Long.MIN_VALUE;
		JsonObject json = testType("Long", testsingle, testproperty,
				testarray);
		JsonTestUtils.checkProperty(json, "testproperty", testproperty);
		JsonTestUtils.checkProperty(json, "testarray", new Long[]{testarray});
		JsonTestUtils.checkProperty(json, "testsingle", testsingle);
	}

	
	private JsonObject testType(String type, Object testsingle,
			Object[] testproperty, Object testarray)
			throws AuthenticationException, ClientProtocolException,
			IOException {
		String resource = "/" + this.getClass().getName() + "/test" + type
				+ "Type" + System.currentTimeMillis();
		String resourceUrl = IntegrationServer.BASEURL + resource;
		HttpPost post = new HttpPost(resourceUrl);
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				ADMIN_USER, ADMIN_PASSWORD);
		post.addHeader(new BasicScheme().authenticate(creds, post));
		List<BasicNameValuePair> v = Lists.newArrayList();
		v.add(new BasicNameValuePair("testsingle@" + type, String
				.valueOf(testsingle)));
		for (Object o : testproperty) {
			v.add(new BasicNameValuePair("testproperty@" + type, String
					.valueOf(o)));
		}
		v.add(new BasicNameValuePair("testarray[]@" + type, String
				.valueOf(testarray)));
		UrlEncodedFormEntity form = new UrlEncodedFormEntity(v);
		post.setEntity(form);
		post.setHeader("Referer","/integratriontest/"+this.getClass().getName());
		httpTestUtils.execute(post, 200, APPLICATION_JSON);

		JsonObject json = JsonTestUtils.toJsonObject(httpTestUtils.get(
				resourceUrl + ".pp.json", 200, APPLICATION_JSON));
		JsonTestUtils.checkProperty(json, "_path", resource);
		return json;
	}

}
