package uk.co.tfd.sm.resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.SparseSessionTracker;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;

import uk.co.tfd.sm.authn.AuthenticationServiceImpl;

public class DefaultResourceHandlerTest {

	@Mock
	private Repository repository;
	@Mock
	private SparseSessionTracker sparseSessionTracker;
	private DefaultResourceHandler defaultResourceHandler;
	@Mock
	private Session session;
	@Mock
	private ContentManager contentManager;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	
	public DefaultResourceHandlerTest() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Before
	public void setup() {
		defaultResourceHandler = new DefaultResourceHandler();
		defaultResourceHandler.authenticationService = new AuthenticationServiceImpl(repository);
		defaultResourceHandler.resourceFactory = new ResponseFactoryManagerImpl();
		defaultResourceHandler.sessionTracker = sparseSessionTracker;		
	}

	@Test
	public void testResource() throws StorageClientException, AccessDeniedException {
		String path = "/test/2/3/4.xxx.yyy.zz.json";
		Mockito.when(sparseSessionTracker.get(request)).thenReturn(null, session);
		Mockito.when(repository.login()).thenReturn(session);
		Mockito.when(sparseSessionTracker.register(session, request)).thenReturn(session);
		Mockito.when(session.getContentManager()).thenReturn(contentManager);
		Mockito.when(request.getMethod()).thenReturn("GET");
		Content content = new Content("/test/2/3/4", null);
		Mockito.when(contentManager.get("/test/2/3/4")).thenReturn(content);
		defaultResourceHandler.getResource(request, response, path);
		InOrder order = Mockito.inOrder(contentManager);
		order.verify(contentManager).get("/test/2/3/4.xxx.yyy.zz.json");
		order.verify(contentManager).get("/test/2/3/4.xxx.yyy.zz");
		order.verify(contentManager).get("/test/2/3/4.xxx.yyy");
		order.verify(contentManager).get("/test/2/3/4.xxx");
		order.verify(contentManager).get("/test/2/3/4");
	}


	@Test
	public void testBareResource() throws StorageClientException, AccessDeniedException {
		String path = "/test/2/3/4";
		Mockito.when(sparseSessionTracker.get(request)).thenReturn(null, session);
		Mockito.when(repository.login()).thenReturn(session);
		Mockito.when(sparseSessionTracker.register(session, request)).thenReturn(session);
		Mockito.when(session.getContentManager()).thenReturn(contentManager);
		Mockito.when(request.getMethod()).thenReturn("GET");
		Content content = new Content("/test/2/3/4", null);
		Mockito.when(contentManager.get("/test/2/3/4")).thenReturn(content);
		defaultResourceHandler.getResource(request, response, path);
		InOrder order = Mockito.inOrder(contentManager);
		order.verify(contentManager).get("/test/2/3/4");
	}
	@Test
	public void testNoResource() throws StorageClientException, AccessDeniedException {
		String path = "/testnon.existant/2/3/4.a.b.c";
		Mockito.when(sparseSessionTracker.get(request)).thenReturn(null, session);
		Mockito.when(repository.login()).thenReturn(session);
		Mockito.when(sparseSessionTracker.register(session, request)).thenReturn(session);
		Mockito.when(session.getContentManager()).thenReturn(contentManager);
		Mockito.when(request.getMethod()).thenReturn("GET");
		Content content = new Content("/test/2/3/4", null);
		Mockito.when(contentManager.get("/test/2/3/4")).thenReturn(content);
		defaultResourceHandler.getResource(request, response, path);
		InOrder order = Mockito.inOrder(contentManager);
		order.verify(contentManager).get("/testnon.existant/2/3/4.a.b.c");
		order.verify(contentManager).get("/testnon.existant/2/3/4.a.b");
		order.verify(contentManager).get("/testnon.existant/2/3/4.a");
		order.verify(contentManager).get("/testnon.existant/2/3/4");
		order.verify(contentManager).get("/testnon.existant/2/3");
		order.verify(contentManager).get("/testnon.existant/2");
		order.verify(contentManager).get("/testnon.existant");
	}

	@Test
	public void testFolder() throws StorageClientException, AccessDeniedException {
		String path = "/test/2/3/4.xxx.yyy.zz.json";
		Mockito.when(sparseSessionTracker.get(request)).thenReturn(null, session);
		Mockito.when(repository.login()).thenReturn(session);
		Mockito.when(sparseSessionTracker.register(session, request)).thenReturn(session);
		Mockito.when(session.getContentManager()).thenReturn(contentManager);
		Mockito.when(request.getMethod()).thenReturn("GET");
		Content content = new Content("/test/2/3", null);
		Mockito.when(contentManager.get("/test/2/3")).thenReturn(content);
		defaultResourceHandler.getResource(request, response, path);
		InOrder order = Mockito.inOrder(contentManager);
		order.verify(contentManager).get("/test/2/3/4.xxx.yyy.zz.json");
		order.verify(contentManager).get("/test/2/3/4.xxx.yyy.zz");
		order.verify(contentManager).get("/test/2/3/4.xxx.yyy");
		order.verify(contentManager).get("/test/2/3/4.xxx");
		order.verify(contentManager).get("/test/2/3/4");
		order.verify(contentManager).get("/test/2/3");
	}

}
