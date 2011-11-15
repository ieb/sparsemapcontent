package uk.co.tfd.sm.resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.content.Content;

import uk.co.tfd.sm.api.resource.Adaptable;

public class ResourceImplTest {

	@Test
	public void test() {
		Content content = new Content("/test/1/2/3", null);
		Session session = Mockito.mock(Session.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Adaptable parent = Mockito.mock(Adaptable.class);
		// simple cases
		ResourceImpl resourceImpl = new ResourceImpl(parent, request, response, session, content, "/test/1/2/3", "/test/1/2/3/4/5/6.xxx.yyy.z", "/test/1/2/3/4/5/6");
		check(resourceImpl,"/4/5/6.xxx.yyy.z","z","6","/test/1/2/3/4/5/6.xxx.yyy.z",new String[]{"xxx","yyy"},"/test/1/2/3",MediaType.APPLICATION_OCTET_STREAM);
		resourceImpl = new ResourceImpl(parent, request, response, session, content, "/test/1/2/3", "/test/1/2/3/4/5/6.xxx.z", "/test/1/2/3/4/5/6");
		check(resourceImpl,"/4/5/6.xxx.z","z","6","/test/1/2/3/4/5/6.xxx.z",new String[]{"xxx"},"/test/1/2/3",MediaType.APPLICATION_OCTET_STREAM);
		resourceImpl = new ResourceImpl(parent, request, response, session, content, "/test/1/2/3", "/test/1/2/3/4/5/6.z", "/test/1/2/3/4/5/6");
		check(resourceImpl,"/4/5/6.z","z","6","/test/1/2/3/4/5/6.z",new String[]{},"/test/1/2/3",MediaType.APPLICATION_OCTET_STREAM);
		resourceImpl = new ResourceImpl(parent, request, response, session, content, "/test/1/2/3", "/test/1/2/3/4/5/6", "/test/1/2/3/4/5/6");
		check(resourceImpl,"/4/5/6","","6","/test/1/2/3/4/5/6",new String[]{},"/test/1/2/3",MediaType.APPLICATION_OCTET_STREAM);
		// harder cases
		resourceImpl = new ResourceImpl(parent, request, response, session, content, "/test/1/2/3", "/test/1/2/3/4/5/6.xxx.yyy..z", "/test/1/2/3/4/5/6");
		check(resourceImpl,"/4/5/6.xxx.yyy..z","z","6","/test/1/2/3/4/5/6.xxx.yyy..z",new String[]{"xxx","yyy"},"/test/1/2/3",MediaType.APPLICATION_OCTET_STREAM);
		content.setProperty(Content.LASTMODIFIED_FIELD, System.currentTimeMillis());
		content.setProperty(Content.MIMETYPE_FIELD,"text/html");
		resourceImpl = new ResourceImpl(parent, request, response, session, content, "/test/1/2/3", "/test/1/2/3/4/5/6...xxx.z", "/test/1/2/3/4/5/6");
		check(resourceImpl,"/4/5/6...xxx.z","z","6","/test/1/2/3/4/5/6...xxx.z",new String[]{"xxx"},"/test/1/2/3","text/html");
		resourceImpl = new ResourceImpl(parent, request, response, session, content, "/test/1/2/3", "/test/1/2/3/4/5/6.z.", "/test/1/2/3/4/5/6");
		check(resourceImpl,"/4/5/6.z.","z","6","/test/1/2/3/4/5/6.z.",new String[]{},"/test/1/2/3","text/html");
		resourceImpl = new ResourceImpl(parent, request, response, session, content, "/test/1/2/3", "/test/1/2/3/4/5/6", "/test/1/2/3/4/5/6");
		check(resourceImpl,"/4/5/6","","6","/test/1/2/3/4/5/6",new String[]{},"/test/1/2/3","text/html");

		content.setProperty(Content.RESOURCE_TYPE_FIELD,"sparse/content");

		resourceImpl = new ResourceImpl(parent, request, response, session, content, "/test/1/2/3", "/test/1/2/3/", "/test/1/2/3/4/5/6");
		check(resourceImpl,"/","","","/test/1/2/3/",new String[]{},"/test/1/2/3","sparse/content");
		resourceImpl = new ResourceImpl(parent, request, response, session, content, "/test/1/2/3/", "/test/1/2/3/abc", "/test/1/2/3/4/5/6");
		check(resourceImpl,"abc","","abc","/test/1/2/3/abc",new String[]{},"/test/1/2/3/","sparse/content");

		
	}

	private void check(ResourceImpl resourceImpl, String pathInfo, String extensions, String requestName, String requestPath, String[] requestSelectors, String resolvedPath, String resourceType) {
		Assert.assertEquals(pathInfo, resourceImpl.getPathInfo());
		Assert.assertEquals(extensions, resourceImpl.getRequestExt());
		Assert.assertEquals(requestName, resourceImpl.getRequestName());
		Assert.assertEquals(requestPath, resourceImpl.getRequestPath());
		Assert.assertArrayEquals(requestSelectors, resourceImpl.getRequestSelectors());
		Assert.assertEquals(resolvedPath, resourceImpl.getResolvedPath());
		Assert.assertEquals(resourceType, resourceImpl.getResourceType());	
	}
}
