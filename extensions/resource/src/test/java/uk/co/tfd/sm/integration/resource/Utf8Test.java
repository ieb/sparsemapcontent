package uk.co.tfd.sm.integration.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.integration.HttpTestUtils;
import uk.co.tfd.sm.integration.IntegrationServer;
import uk.co.tfd.sm.integration.JsonTestUtils;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

public class Utf8Test {
	private static final String ADMIN_USER = "admin";
	private static final String ADMIN_PASSWORD = "admin";
	private static final String APPLICATION_JSON = "application/json; charset=utf-8";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Utf8Test.class);
	private HttpTestUtils httpTestUtils = new HttpTestUtils();
	private String resource;
	private String resourceUrl;

	public Utf8Test() throws IOException {
		IntegrationServer.start();
		resource = "/" + this.getClass().getName() + "/testUtfTest"
				+ System.currentTimeMillis();
		resourceUrl = IntegrationServer.BASEURL + resource;
	}

	@Test
	public void testUtf8() throws IOException, AuthenticationException {
		BufferedReader br = new BufferedReader(new InputStreamReader(this
				.getClass().getResourceAsStream("utf8.testpatterns"), "UTF-8"));
		String line = br.readLine();
		int i = 0;
		int low = Integer.MAX_VALUE;
		int high = Integer.MIN_VALUE;
		while (line != null) {
			testUtf8Pattern(line, new String[] { line, line, line }, line);
			i++;
			for (int j = 0; j < line.length(); j++) {
				int cp = Character.codePointAt(line, j);
				high = Math.max(high, cp);
				low = Math.min(low, cp);
			}
			LOGGER.info(
					"Passed Test Pattern {} {} Code Point Range tested from 0x{} to 0x{}",
					new Object[] { i,
							line.substring(0, Math.min(line.length(), 20)),
							Integer.toHexString(low), Integer.toHexString(high) });
			line = br.readLine();
		}
		br.close();
	}

	private JsonObject testUtf8Pattern(String testsingle,
			String[] testproperty, String testarray)
			throws AuthenticationException, ClientProtocolException,
			IOException {
		HttpPost post = new HttpPost(resourceUrl);
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				ADMIN_USER, ADMIN_PASSWORD);
		post.addHeader(new BasicScheme().authenticate(creds, post));
		List<BasicNameValuePair> v = Lists.newArrayList();
		v.add(new BasicNameValuePair("testsingle", testsingle));
		for (String o : testproperty) {
			v.add(new BasicNameValuePair("testproperty", o));
		}
		v.add(new BasicNameValuePair("testarray[]@String", testarray));
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
