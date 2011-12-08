package uk.co.tfd.sm.util.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.lite.BaseMemoryRepository;

import com.google.common.collect.Lists;

public class ModificationRequestTest {

	protected static final String BOUNDARY = "mimeboundary1";

	@Test
	public void test() throws IOException, FileUploadException,
			StorageClientException, AccessDeniedException {
		ModificationRequest m = new ModificationRequest();

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getMethod()).thenReturn("POST");

		
		Map<String, String[]> parameters = ParameterUtil.getParameters();

		Mockito.when(request.getParameterMap()).thenReturn(parameters);
		m.processRequest(request);
		
		ParameterUtil.testParameters(m);
		
	}

	
	

	@Test
	public void testStreamContent() throws IOException, FileUploadException,
			StorageClientException, AccessDeniedException, ClassNotFoundException {
		BaseMemoryRepository memoryRepository = new BaseMemoryRepository();
		Session adminSession = memoryRepository.getRepository().loginAdministrative();
		ContentManager contentManager = adminSession.getContentManager();
		ContentHelper contentHelper = new ContentHelper(contentManager);
		
		String path = "/test/content";
		contentManager.update(new Content(path,null));
		Content content = contentManager.get(path);
		ContentRequestStreamProcessor contentRequestStreamProcessor = new ContentRequestStreamProcessor(content, contentManager, contentHelper);
		
		ModificationRequest m = new ModificationRequest(contentRequestStreamProcessor);
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getMethod()).thenReturn("POST");
		Mockito.when(request.getContentType()).thenReturn("multipart/form-data; boundary="+BOUNDARY);
		List<List<String>> parts = Lists.newArrayList();
		parts.add(Lists.newArrayList("Content-Disposition: form-data; name=\"a\"","value_for_a"));
		parts.add(Lists.newArrayList("Content-Disposition: form-data; name=\"b\"","value_for_b"));
		parts.add(Lists.newArrayList("Content-Disposition: form-data; name=\"c\"; filename=\"testfile.txt\" ","Content-Type: text/plain","A very short file"));
		parts.add(Lists.newArrayList("Content-Disposition: form-data; name=\"a\"","value_for_a2"));
		parts.add(Lists.newArrayList("Content-Disposition: form-data; name=\"b\"","value_for_b2"));
		parts.add(Lists.newArrayList("Content-Disposition: form-data; name=\"b\"","value_for_b3"));
		final InputStream postInputStream = getMultipatStream(BOUNDARY,parts);
	
		ServletInputStream inputStream = new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return postInputStream.read();
			}
		};
		Mockito.when(request.getInputStream()).thenReturn(inputStream);
		m.processRequest(request);
		contentHelper.applyProperties(content, m);
		contentHelper.save();
		
		List<String> feedback = m.getFeedback();
		Assert.assertArrayEquals(new String[]{"Multipart Upload","Added a","Added b","Saved Stream testfile.txt","Added a","Added b","Added b"}, feedback.toArray());
		
		Content c = contentManager.get(path);
		Assert.assertNotNull(c);
		Assert.assertEquals("value_for_a2", c.getProperty("a"));
		Assert.assertArrayEquals(new String[]{"value_for_b2","value_for_b3"}, (Object[]) c.getProperty("b"));
		c = contentManager.get(path+"/testfile.txt");
		Assert.assertNotNull(c);
		Assert.assertEquals("value_for_a", c.getProperty("a"));
		Assert.assertEquals("value_for_b", c.getProperty("b"));
		Assert.assertEquals("text/plain", c.getProperty(Content.MIMETYPE_FIELD));
		Assert.assertEquals("A very short file", IOUtils.toString(contentManager.getInputStream(path+"/testfile.txt"),"UTF-8"));
		
	}
	
	
	@Test
	public void testUpdateAuthorizable() throws ClientPoolException, StorageClientException, AccessDeniedException, ClassNotFoundException, IOException, FileUploadException {
		BaseMemoryRepository memoryRepository = new BaseMemoryRepository();
		Session adminSession = memoryRepository.getRepository().loginAdministrative();
		AuthorizableManager authorizableManager = adminSession.getAuthorizableManager();
		AuthorizableHelper authorizableHelper = new AuthorizableHelper(authorizableManager);
		
		Authorizable a = authorizableHelper.getOrCreateAuthorizable("testuser", "user");
		ModificationRequest modificationRequest = new ModificationRequest(null);
		
		Map<String, String[]> parameters = ParameterUtil.getParameters();
		
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getMethod()).thenReturn("POST");
		Mockito.when(request.getParameterMap()).thenReturn(parameters);

		modificationRequest.processRequest(request);
		
		ParameterUtil.testParameters(modificationRequest);

		authorizableHelper.applyProperties(a, modificationRequest);
		authorizableHelper.save();
		
		Authorizable authorizable = authorizableManager.findAuthorizable("testuser");
		
		ParameterUtil.testProperties(authorizable.getProperties());


	}

	protected InputStream getMultipatStream(String boundary, List<List<String>> parts) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append("preeamble");
		for ( List<String> part : parts) {
			sb.append("\r\n--").append(boundary).append("\r\n");
			for ( int i = 0; i < part.size()-1; i++) {
				sb.append(part.get(i)).append("\r\n");
			}
			sb.append("\r\n");
			sb.append(part.get(part.size()-1));
		}
		sb.append("\r\n--").append(boundary).append("--\r\n");
		return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
	}

}
