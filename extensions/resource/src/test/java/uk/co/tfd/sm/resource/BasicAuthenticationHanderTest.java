package uk.co.tfd.sm.resource;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;
import uk.co.tfd.sm.authn.BasicAuthenticationHandler;

public class BasicAuthenticationHanderTest {

	private HttpServletRequest request;
	private BasicAuthenticationHandler bh;
	
	@Before
	public void before() {
		bh = new BasicAuthenticationHandler();
		request = Mockito.mock(HttpServletRequest.class);
	}

	@Test
	public void testNone() {
		Mockito.when(request.getHeader("Authorization")).thenReturn(null);
		Assert.assertNull(bh.getCredentials(request));
	}
	
	@Test
	public void testInvalid() {
		Mockito.when(request.getHeader("Authorization")).thenReturn("blasdflkj");
		Assert.assertNull(bh.getCredentials(request));
	}

	@Test
	public void testInvalidNoPassword() throws UnsupportedEncodingException {
		Mockito.when(request.getHeader("Authorization")).thenReturn("Basic "+Base64.encodeBase64URLSafeString("user:".getBytes("UTF-8")));
		AuthenticationServiceCredentials credentials = bh.getCredentials(request);
		Assert.assertNull(credentials);
	}

	@Test
	public void testPassword() throws UnsupportedEncodingException {
		Mockito.when(request.getHeader("Authorization")).thenReturn("Basic "+Base64.encodeBase64URLSafeString("user:password".getBytes("UTF-8")));
		AuthenticationServiceCredentials credentials = bh.getCredentials(request);
		Assert.assertNotNull(credentials);
		Assert.assertEquals("user", credentials.getUserName());
		Assert.assertEquals("password", credentials.getPassword());
	}

	@Test
	public void testPasswordColon() throws UnsupportedEncodingException {
		Mockito.when(request.getHeader("Authorization")).thenReturn("Basic "+Base64.encodeBase64URLSafeString("user:pas:sword".getBytes("UTF-8")));
		AuthenticationServiceCredentials credentials = bh.getCredentials(request);
		Assert.assertNotNull(credentials);
		Assert.assertEquals("user", credentials.getUserName());
		Assert.assertEquals("pas:sword", credentials.getPassword());
	}

}
