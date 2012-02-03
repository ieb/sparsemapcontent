package uk.co.tfd.sm.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.co.tfd.sm.api.http.ServerProtectionService.Action;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ServerProtectionServiceImplTest {

	private ServerProtectionServiceImpl serverProtectionServiceImpl;

	@Before
	public void before() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		serverProtectionServiceImpl = new ServerProtectionServiceImpl();
		Map<String, Object> properties = ImmutableMap.of(
				"secret", (Object)"change in production",
				"whitelist", new String[]{"/whitelist"},
				"hosts", new String[]{			
						"referer;localhost:8080;http://localhost:8080/",
					"csrf;localhost:8080;",
					"usercontent;localhost:8082;",
					"redirect;localhost:8080;http://localhost:8082" });
		serverProtectionServiceImpl.activate(properties);
	}
	
	
	@Test
	public void testSimpleAction() {
		Map<String, String> noParameters = ImmutableMap.of();
		Vector<String> noReferers = new Vector<String>();
		// gets to the application host are ok
		Assert.assertEquals(Action.OK, serverProtectionServiceImpl.checkAction(getRequest("GET","http://localhost:8080/testng123",null,noParameters, noReferers)));
		// check the content host is ok
		Assert.assertEquals(Action.OK, serverProtectionServiceImpl.checkAction(getRequest("GET","http://localhost:8082/testng123",null,noParameters, noReferers)));
		// posts to application hosts need referrers
		Assert.assertEquals(Action.FORBID, serverProtectionServiceImpl.checkAction(getRequest("POST","http://localhost:8080/testng123",null,noParameters, noReferers)));
		// cant do gets to hosts that are not configured
		Assert.assertEquals(Action.FORBID, serverProtectionServiceImpl.checkAction(getRequest("GET","http://invalidHost/testng123",null,noParameters, noReferers)));
	}

	@Test
	public void testPostReferer() {
		Map<String, String> noParameters = ImmutableMap.of();
		Vector<String> noReferers = new Vector<String>();
		Vector<String> referers = new Vector<String>();
		Assert.assertEquals(Action.FORBID, serverProtectionServiceImpl.checkAction(getRequest("POST","http://localhost:8080/testng123",null,noParameters, noReferers)));
		referers.clear();
		referers.add("/dada");
		Assert.assertEquals(Action.OK, serverProtectionServiceImpl.checkAction(getRequest("POST","http://localhost:8080/testng123",null,noParameters, referers)));
		referers.clear();
		referers.add("http://localhost:8080/OkLocation");
		Assert.assertEquals(Action.OK, serverProtectionServiceImpl.checkAction(getRequest("POST","http://localhost:8080/testng123",null,noParameters, referers)));
		referers.clear();
		referers.add("http://localhost:8081/BadPort");
		Assert.assertEquals(Action.FORBID, serverProtectionServiceImpl.checkAction(getRequest("POST","http://localhost:8080/testng123",null,noParameters, referers)));
		referers.clear();
		referers.add("https://localhost:8080/BadProtocol");
		Assert.assertEquals(Action.FORBID, serverProtectionServiceImpl.checkAction(getRequest("POST","http://localhost:8080/testng123",null,noParameters, referers)));
		referers.add("https://localhost:8080/BadProtocol");
		Assert.assertEquals(Action.FORBID, serverProtectionServiceImpl.checkAction(getRequest("POST","http://asdsad/testng123",null,noParameters, noReferers)));
	}

	@Test
	public void testPostCSRF() {
		Map<String, String> noParameters = ImmutableMap.of();
		Vector<String> noReferers = new Vector<String>();
		HttpServletRequest request = getRequest("GET","http://localhost:8080/testng123",null,noParameters, noReferers);
		String token = serverProtectionServiceImpl.getCSRFToken(request);
		Assert.assertNotNull(token);
		Assert.assertEquals(Action.OK, serverProtectionServiceImpl.checkAction(getRequest("POST","http://localhost:8080/testng123",null, ImmutableMap.of("_csrft",token ), noReferers)));
		Assert.assertEquals(Action.FORBID, serverProtectionServiceImpl.checkAction(getRequest("POST","http://localhost:8080/testng123",null, ImmutableMap.of("_csrft",token+"bad" ), noReferers)));
	}

	@Test
	public void testRedirect() throws UnsupportedEncodingException {
		Map<String, String> noParameters = ImmutableMap.of();
		Vector<String> noReferers = new Vector<String>();
		HttpServletRequest badRequest = getRequest("POST","http://localhost:8080/testng123","x=1&y=2",noParameters, noReferers);
		String badRequestUrl = serverProtectionServiceImpl.getRedirectIdentityUrl(badRequest, "ieb");
		Assert.assertNull(badRequestUrl);
		HttpServletRequest request = getRequest("GET","http://localhost:8080/testng123","x=1&y=2",noParameters, noReferers);
		String redirectUrl = serverProtectionServiceImpl.getRedirectIdentityUrl(request, "ieb");
		Assert.assertNotNull(redirectUrl);
		Map<String, String> params = Maps.newHashMap();
		String queryString = redirectUrl.substring(redirectUrl.indexOf("?")+1);
		String redirectURI = redirectUrl.substring(0,redirectUrl.indexOf("?"));
		for ( String kv : StringUtils.split(queryString,"&")) {
			String[] pp = StringUtils.split(kv,"=");
			params.put(URLDecoder.decode(pp[0], "UTF-8"), URLDecoder.decode(pp[1], "UTF-8"));
		}
		
		Assert.assertEquals("ieb", serverProtectionServiceImpl.getIdentity(getRequest("GET", redirectURI, queryString, params, noReferers)));
		Assert.assertNull(serverProtectionServiceImpl.getIdentity(getRequest("POST", redirectURI, queryString, params, noReferers)));
		Assert.assertNull(serverProtectionServiceImpl.getIdentity(getRequest("GET", "http://localhost:8080/testng123Bad", queryString, params, noReferers)));
		params.put("_hmac", "bad"+params.get("_hmac"));
		Assert.assertNull(serverProtectionServiceImpl.getIdentity(getRequest("GET", redirectURI, queryString, params, noReferers)));
	}

	private HttpServletRequest getRequest(String method, String url, String queryString, Map<String, String> parameters, Vector<String> referers) {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getMethod()).thenReturn(method);
		Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer(url));
		String uri = url.substring(url.indexOf("/",url.indexOf("/")+2));
		String host = url.substring(url.indexOf("/")+2, url.indexOf("/",url.indexOf("/")+2));
		Mockito.when(request.getRequestURI()).thenReturn(uri);
		Mockito.when(request.getQueryString()).thenReturn(queryString);
		Mockito.when(request.getParameter("_hmac")).thenReturn(parameters.get("_hmac"));
		Mockito.when(request.getParameter("_csrft")).thenReturn(parameters.get("_csrft"));
		Mockito.when(request.getHeader("Host")).thenReturn(host);
		Mockito.when(request.getHeaders("Referer")).thenReturn(referers.elements());
		
		return request;
	}
}
