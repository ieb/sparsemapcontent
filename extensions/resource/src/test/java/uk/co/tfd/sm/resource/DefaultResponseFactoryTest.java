package uk.co.tfd.sm.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;


import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.Resource;
import uk.co.tfd.sm.api.resource.ResponseFactory;
import uk.co.tfd.sm.api.resource.binding.ResponseBindingList;
import uk.co.tfd.sm.api.resource.binding.RuntimeResponseBinding;

public class DefaultResponseFactoryTest {

	@Mock
	private Adaptable adaptable;
	@Mock
	private Resource resource;
	@Mock
	private Session session;
	@Mock
	private ContentManager contentManager;
	@Mock
	private Content content;

	public DefaultResponseFactoryTest() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetBindings() {
		DefaultResponseFactory fac = new DefaultResponseFactory();
		ResponseBindingList bl = fac.getBindings();
		Assert.assertNotNull(bl);
		Iterator<RuntimeResponseBinding> i = bl.iterator();
		int n = 0;
		while (i.hasNext()) {
			n++;
			RuntimeResponseBinding b = i.next();
			Assert.assertNotNull(b);
		}
		Assert.assertTrue(n > 0);
	}

	@Test
	public void testGetResource() throws IOException, StorageClientException, AccessDeniedException {
		DefaultResponseFactory fac = new DefaultResponseFactory();
		Adaptable gr = fac.getResponse(adaptable);
		Assert.assertNotNull(gr);
		DefaultResponse dg = (DefaultResponse) gr;
		Mockito.when(adaptable.adaptTo(Resource.class)).thenReturn(resource);
		Mockito.when(adaptable.adaptTo(Session.class)).thenReturn(session);
		Mockito.when(adaptable.adaptTo(Content.class)).thenReturn(content);
		Mockito.when(adaptable.adaptTo(Date.class)).thenReturn(new Date());
		Mockito.when(resource.getRequestExt()).thenReturn(null);
		Mockito.when(session.getContentManager()).thenReturn(contentManager);
		byte[] data = "TEST".getBytes("UTF-8");
		InputStream in = new ByteArrayInputStream(data);
		Mockito.when(contentManager.getInputStream(Mockito.anyString())).thenReturn(in);
		
		
		Response response = dg.doGet();
		Object o = response.getEntity();
		int status = response.getStatus();
		Assert.assertEquals(200, status);
		StreamingOutput out = (StreamingOutput) o;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		out.write(output);
		byte[] outputData = output.toByteArray();
		Assert.assertArrayEquals(data, outputData);

	}

	@Test
	public void testGetResourceJson() throws IOException, StorageClientException, AccessDeniedException {
		DefaultResponseFactory fac = new DefaultResponseFactory();
		Adaptable gr = fac.getResponse(adaptable);
		Assert.assertNotNull(gr);
		DefaultResponse dg = (DefaultResponse) gr;
		Mockito.when(adaptable.adaptTo(Resource.class)).thenReturn(resource);
		Mockito.when(adaptable.adaptTo(Session.class)).thenReturn(session);
		Mockito.when(adaptable.adaptTo(Content.class)).thenReturn(content);
		Mockito.when(adaptable.adaptTo(Date.class)).thenReturn(new Date());
		Mockito.when(resource.getRequestExt()).thenReturn("json");
		Mockito.when(session.getContentManager()).thenReturn(contentManager);
		Mockito.when(content.getProperties()).thenReturn(ImmutableMap.of("key", (Object)"value"));
		Response response = dg.doGet();
		Object o = response.getEntity();
		Assert.assertEquals("application/json",response.getMetadata().get("Content-Type").get(0).toString());
		int status = response.getStatus();
		Assert.assertEquals(200, status);
		StreamingOutput out = (StreamingOutput) o;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		out.write(output);
		String outputData = new String(output.toByteArray(), "UTF-8");
		Assert.assertEquals("{\"key\":\"value\"}", outputData);
	}
	@Test
	public void testGetResourceXml() throws IOException, StorageClientException, AccessDeniedException {
		DefaultResponseFactory fac = new DefaultResponseFactory();
		Adaptable gr = fac.getResponse(adaptable);
		Assert.assertNotNull(gr);
		DefaultResponse dg = (DefaultResponse) gr;
		Mockito.when(adaptable.adaptTo(Resource.class)).thenReturn(resource);
		Mockito.when(adaptable.adaptTo(Session.class)).thenReturn(session);
		Mockito.when(adaptable.adaptTo(Content.class)).thenReturn(content);
		Mockito.when(adaptable.adaptTo(Date.class)).thenReturn(new Date());
		Mockito.when(resource.getRequestExt()).thenReturn("xml");
		Mockito.when(session.getContentManager()).thenReturn(contentManager);
		Mockito.when(content.getProperties()).thenReturn(ImmutableMap.of("key", (Object)"value"));
		Response response = dg.doGet();
		Assert.assertEquals("application/xml",response.getMetadata().get("Content-Type").get(0).toString());
		int status = response.getStatus();
		Assert.assertEquals(200, status);
	}
	
	@Test
	public void testGetResourceInvalid() throws IOException, StorageClientException, AccessDeniedException {
		DefaultResponseFactory fac = new DefaultResponseFactory();
		Adaptable gr = fac.getResponse(adaptable);
		Assert.assertNotNull(gr);
		DefaultResponse dg = (DefaultResponse) gr;
		Mockito.when(adaptable.adaptTo(Resource.class)).thenReturn(resource);
		Mockito.when(adaptable.adaptTo(Session.class)).thenReturn(session);
		Mockito.when(adaptable.adaptTo(Content.class)).thenReturn(content);
		Mockito.when(adaptable.adaptTo(Date.class)).thenReturn(new Date());
		Mockito.when(resource.getRequestExt()).thenReturn("invalid");
		Mockito.when(session.getContentManager()).thenReturn(contentManager);
		Mockito.when(content.getProperties()).thenReturn(ImmutableMap.of("key", (Object)"value"));
		Response response = dg.doGet();
		int status = response.getStatus();
		Assert.assertEquals(400, status);
	}
	
	@Test
	public void testGetResourceDenied() throws IOException, StorageClientException, AccessDeniedException {
		DefaultResponseFactory fac = new DefaultResponseFactory();
		Adaptable gr = fac.getResponse(adaptable);
		Assert.assertNotNull(gr);
		DefaultResponse dg = (DefaultResponse) gr;
		Mockito.when(adaptable.adaptTo(Resource.class)).thenReturn(resource);
		Mockito.when(adaptable.adaptTo(Session.class)).thenReturn(session);
		Mockito.when(adaptable.adaptTo(Content.class)).thenReturn(content);
		Mockito.when(adaptable.adaptTo(Date.class)).thenReturn(new Date());
		Mockito.when(resource.getRequestExt()).thenReturn(null);
		Mockito.when(session.getContentManager()).thenReturn(contentManager);
		Mockito.when(contentManager.getInputStream(Mockito.anyString())).thenThrow(new AccessDeniedException("test","test","test","test"));
		Mockito.when(content.getProperties()).thenReturn(ImmutableMap.of("key", (Object)"value"));
		Response response = dg.doGet();
		int status = response.getStatus();
		Assert.assertEquals(403, status);
	}

	@Test
	public void testGetResourceError() throws IOException, StorageClientException, AccessDeniedException {
		DefaultResponseFactory fac = new DefaultResponseFactory();
		Adaptable gr = fac.getResponse(adaptable);
		Assert.assertNotNull(gr);
		DefaultResponse dg = (DefaultResponse) gr;
		Mockito.when(adaptable.adaptTo(Resource.class)).thenReturn(resource);
		Mockito.when(adaptable.adaptTo(Session.class)).thenReturn(session);
		Mockito.when(adaptable.adaptTo(Content.class)).thenReturn(content);
		Mockito.when(adaptable.adaptTo(Date.class)).thenReturn(new Date());
		Mockito.when(resource.getRequestExt()).thenReturn(null);
		Mockito.when(session.getContentManager()).thenReturn(contentManager);
		Mockito.when(contentManager.getInputStream(Mockito.anyString())).thenThrow(new StorageClientException("test"));
		Mockito.when(content.getProperties()).thenReturn(ImmutableMap.of("key", (Object)"value"));
		Response response = dg.doGet();
		int status = response.getStatus();
		Assert.assertEquals(500, status);
	}

	@Test
	public void testSort() throws IOException, StorageClientException, AccessDeniedException {
		List<ResponseFactory> factories = Lists.newArrayList(
				(ResponseFactory)new DefaultResponseFactory(),
				new DefaultResponseFactory());
		Collections.sort(factories);
	}

}
