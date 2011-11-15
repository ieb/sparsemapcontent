package uk.co.tfd.sm.resource;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.SparseSessionTracker;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.authn.AuthenticationService;
import uk.co.tfd.sm.api.jaxrs.JaxRestService;
import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.Resource;
import uk.co.tfd.sm.api.resource.ResponseFactoryManager;
import uk.co.tfd.sm.api.resource.SafeMethodResponse;

@Component(immediate = true, metatype = true)
@Service(value = JaxRestService.class)
@Path("/")
public class DefaultResourceHandler implements JaxRestService, Adaptable {

	private static final String DEFAULT_MAPPED_ROOT_PATH = "/";

	@Property(value=DEFAULT_MAPPED_ROOT_PATH)
	protected static final String MAPPED_ROOT_PATH = "mapped-root-path";

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResourceHandler.class);

	@Reference
	protected SparseSessionTracker sessionTracker;


	@Reference
	protected ResponseFactoryManager resourceFactory;

	@Reference
	protected AuthenticationService authenticationService;

	private String basePath = "";

	
	@Activate
	public void activate(Map<String, Object> properties) {
		modified(properties);
	}
	
	@Deactivate
	public void deactivate(Map<String, Object> properties) {
		
	}
	
	@Modified
	public void modified(Map<String, Object> properties) {
		basePath = (String) properties.get(MAPPED_ROOT_PATH);
		if ( basePath == null ) {
			basePath = DEFAULT_MAPPED_ROOT_PATH;
		}
	}

	
	@Path("/{resource:.*}")
	public Adaptable getResource(@Context HttpServletRequest request, @Context HttpServletResponse response,
			@PathParam("resource") String path) throws StorageClientException,
			AccessDeniedException {
		LOGGER.debug("Got Request at {} ",request.getRequestURI());
		path = basePath + path;
		Session session = sessionTracker.get(request);
		if (session == null) {
			
			session = sessionTracker.register(authenticationService.authenticate(request), request);
		}
		ContentManager contentManager = session.getContentManager();
		
		// start with the full path, and shorten it, first by . then by /
		Content content = contentManager.get(path);
		if (content != null) {
			LOGGER.debug("Got {} at [{}] ",content,path);
			return getResponse(request, response, session, content, path, path, path);
		}
		LOGGER.debug("Nothing at [{}] ",path);
		char[] pathChars = path.toCharArray();
		String toCreatePath = path;
		boolean inname = true;
		for (int i = pathChars.length - 1; i >= 0; i--) {
			char c = pathChars[i];
			switch (c) {
			case '.':
				if (inname) {
					toCreatePath = path.substring(0, i);
					content = contentManager.get(toCreatePath);
					if (content != null) {
						LOGGER.debug("Getting response for {} {} ",path, toCreatePath);
						return getResponse(request, response, session, content, toCreatePath, path, toCreatePath);
					}
					LOGGER.debug("Nothing at [{}] ",toCreatePath);
				}
				break;
			case '/':
				inname = false;
				String testpath = path.substring(0, i);
				content = contentManager.get(testpath);
				if (content != null) {
					return getResponse(request, response, session, content, testpath,
							path, toCreatePath);
				}
				LOGGER.debug("Nothing at [{}] ",testpath);
				break;
			}
		}
		LOGGER.debug("Not Found [{}] ",path);
		return getResponse(request, response, session, null, path, path, toCreatePath);
	}

	private Adaptable getResponse(HttpServletRequest request, HttpServletResponse response, Session session,
			Content content, String resolvedPath, String requestPath, String toCreatePath) {
		Resource resource = new ResourceImpl(this,request, response, session, content, resolvedPath, requestPath, toCreatePath);
		LOGGER.debug("Processing Resource {} ",resource);
		Adaptable aresponse = resourceFactory.createResponse(resource);
		if ( response instanceof SafeMethodResponse && 
				!SafeMethodResponse.COMPATABLE_METHODS.contains(request.getMethod()) ) {
			LOGGER.warn(" Response {} is not suitable for {} methods ", response, request.getMethod());
		}
		LOGGER.debug("Mapped to Response {} ",aresponse);
		return aresponse;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T adaptTo(Class<T> type) {
		if (ResponseFactoryManager.class.equals(type)) {
			return (T) resourceFactory;
		}
		return null;
	}
}
