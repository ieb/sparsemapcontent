package uk.co.tfd.sm.authn.token;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.nakamura.api.memory.CacheManagerService;
import org.sakaiproject.nakamura.api.memory.CacheScope;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;
import uk.co.tfd.sm.authn.TrustedCredentials;
import uk.co.tfd.sm.cluster.ClusterServiceImpl;
import uk.co.tfd.sm.memory.ehcache.MapCacheImpl;

import com.google.common.collect.Maps;

public class CookieTokenAuthenticationServiceImplTest {

	private static final long RUN_ID = System.currentTimeMillis();
	private CookieTokenAuthenticationServiceImpl cookieTokenAuthenticationServiceImpl;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	
	public CookieTokenAuthenticationServiceImplTest() {
		MockitoAnnotations.initMocks(this);
	}
	@Before
	public void before() throws MalformedObjectNameException, AttributeNotFoundException, InstanceNotFoundException, NullPointerException, MBeanException, ReflectionException, NoSuchAlgorithmException, IOException {
		cookieTokenAuthenticationServiceImpl = new CookieTokenAuthenticationServiceImpl();
		CacheManagerService cacheManager = Mockito.mock(CacheManagerService.class);
		Mockito.when(cacheManager.getCache(Mockito.anyString(), (CacheScope) Mockito.any())).thenReturn(new MapCacheImpl<Object>());
		cookieTokenAuthenticationServiceImpl.cacheManager = cacheManager;
		
		ClusterServiceImpl clusterService =  new ClusterServiceImpl();
		cookieTokenAuthenticationServiceImpl.clusterService = clusterService;
		Map<String, Object> properites = Maps.newHashMap();

		properites.put("cookie-name", "sm-auth");
		properites.put("keys-per-server", 10);
		properites.put("key-ttl", 1800000L);
		properites.put("cookie-age", 2000);
		properites.put("secure-transport", false);
		System.setProperty("sling.home", "target/slingTestHome"+RUN_ID);
		clusterService.activate(properites);
		cookieTokenAuthenticationServiceImpl.activate(properites);
	}
	@Test
	public void testNoAuth() {
		Assert.assertNull(cookieTokenAuthenticationServiceImpl.getCredentials(request));
	}
	
	@Test
	public void testAuth() {
		ArgumentCaptor<Cookie> cookieCapture = ArgumentCaptor.forClass(Cookie.class);
		cookieTokenAuthenticationServiceImpl.refreshCredentials(new TrustedCredentials("ieb"), request, response);
		Mockito.verify(response, Mockito.atLeastOnce()).addCookie(cookieCapture.capture());	
		Cookie cookie = cookieCapture.getValue();
		Mockito.when(request.getCookies()).thenReturn(new Cookie[]{cookie});
		AuthenticationServiceCredentials cred = cookieTokenAuthenticationServiceImpl.getCredentials(request);
		Assert.assertNotNull(cred);
		Assert.assertEquals("ieb", cred.getUserName());
		cred = cookieTokenAuthenticationServiceImpl.getCredentials(request);
		Assert.assertNotNull(cred);
		Assert.assertEquals("ieb", cred.getUserName());
		
		TokenAuthenticationHandler th = new TokenAuthenticationHandler();
		th.tokenAuthenticationService = cookieTokenAuthenticationServiceImpl;
		cred = th.getCredentials(request);
		Assert.assertNotNull(cred);
		Assert.assertEquals("ieb", cred.getUserName());
	}
	
	@Test
	public void testBadAuth() throws UnsupportedEncodingException {
		String cookieValue = Base64
		.encodeBase64URLSafeString(StringUtils.join(
				new Object[] { "121", "ieb", System.currentTimeMillis()+10000L,
						"invalidHash" }, ";").getBytes("UTF-8"));
		Mockito.when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("sm_auth", cookieValue)});
		AuthenticationServiceCredentials cred = cookieTokenAuthenticationServiceImpl.getCredentials(request);
		Assert.assertNull(cred);
		
		TokenAuthenticationHandler th = new TokenAuthenticationHandler();
		th.tokenAuthenticationService = cookieTokenAuthenticationServiceImpl;
		cred = th.getCredentials(request);
		Assert.assertNull(cred);
	}
	

}
