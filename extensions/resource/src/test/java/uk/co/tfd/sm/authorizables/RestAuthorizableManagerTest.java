package uk.co.tfd.sm.authorizables;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.SparseSessionTracker;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.lite.BaseMemoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.authn.AuthenticationService;
import uk.co.tfd.sm.util.http.ParameterUtil;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class RestAuthorizableManagerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestAuthorizableManagerTest.class);
	private BaseMemoryRepository baseMemoryRepository;
	private Session session;
	private RestAuthorizableManager restAuthorizableManager;
	@Mock
	private AuthenticationService authenticationService;
	@Mock
	private SparseSessionTracker sparseSessionTracker;
	@Mock
	private HttpServletRequest request;
	private Session userSession;
	
	public RestAuthorizableManagerTest() {
		MockitoAnnotations.initMocks(this);
	}
	@Before
	public void before() throws ClientPoolException, StorageClientException, AccessDeniedException, ClassNotFoundException, IOException {
		baseMemoryRepository = new BaseMemoryRepository();
		session = baseMemoryRepository.getRepository().loginAdministrative();
		restAuthorizableManager = new RestAuthorizableManager();
		restAuthorizableManager.authenticationService = authenticationService;
		restAuthorizableManager.sessionTracker = sparseSessionTracker;
		
		session.getAuthorizableManager().createUser("testuser", "TestUser", "testpassword", null);
		userSession = baseMemoryRepository.getRepository().loginAdministrative("testuser");
		
		
		
	}
	
	
	@Test
	public void test() throws StorageClientException, WebApplicationException, IOException {
		Mockito.when(authenticationService.authenticate(request)).thenReturn(session);
		Mockito.when(sparseSessionTracker.register(session, request)).thenReturn(session);
		Response response = restAuthorizableManager.getUser(request, "user", "testuser", "pp");
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatus());
	    StreamingOutput entity = (StreamingOutput) response.getEntity();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    entity.write(baos);
	    String output = new String(baos.toByteArray(),"UTF-8");
	    JsonParser parser = new JsonParser();
	    LOGGER.info("Response {} ",output);
	    JsonObject o = parser.parse(output).getAsJsonObject();
	    Assert.assertEquals("admin",o.get(Authorizable.CREATED_BY_FIELD).getAsString());
	    Assert.assertEquals("testuser",o.get(Authorizable.ID_FIELD).getAsString());
	    Assert.assertEquals("TestUser",o.get(Authorizable.NAME_FIELD).getAsString());
	    Assert.assertEquals("u",o.get("type").getAsString());
	}
	
	
	@Test
	public void testUpdate() throws StorageClientException, WebApplicationException, IOException, AccessDeniedException {
		Mockito.when(authenticationService.authenticate(request)).thenReturn(session);
		Mockito.when(sparseSessionTracker.register(session, request)).thenReturn(session);
		Mockito.when(request.getParameterMap()).thenReturn(ParameterUtil.getParameters());
		Mockito.when(request.getMethod()).thenReturn("POST");
		Response response = restAuthorizableManager.doUpdateAuthorizable(request, "user", "testuser");
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatus());
	    StreamingOutput entity = (StreamingOutput) response.getEntity();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    entity.write(baos);
	    String output = new String(baos.toByteArray(),"UTF-8");
	    ParameterUtil.checkResponse(output);
	    ParameterUtil.testProperties(session.getAuthorizableManager().findAuthorizable("testuser").getProperties());
	}

}
