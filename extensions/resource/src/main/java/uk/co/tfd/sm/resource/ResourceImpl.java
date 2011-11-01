package uk.co.tfd.sm.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.ContentType;
import uk.co.tfd.sm.api.resource.Resource;
import uk.co.tfd.sm.api.resource.ResourceErrorException;
import uk.co.tfd.sm.api.resource.ResourceForbiddenException;

public class ResourceImpl implements Resource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceImpl.class);
	private HttpServletRequest request;
	private Session session;
	private Content content;
	private String resolvedPath;
	private String requestPath;
	private String pathInfo;
	private String requestName;
	private String[] requestSelectors;
	private String requestExt;
	private Adaptable resourceHandler;

	public ResourceImpl(Adaptable resourceHandler, HttpServletRequest request,
			Session session, Content content, String resolvedPath,
			String requestPath) {
		this.resourceHandler = resourceHandler;
		this.request = request;
		this.session = session;
		this.content = content;
		this.resolvedPath = resolvedPath;
		this.requestPath = requestPath;
		this.pathInfo = requestPath.substring(resolvedPath.length());
		int lastSlash = pathInfo.lastIndexOf('/');
		if (lastSlash == pathInfo.length() - 1) {
			setParts("");
		} else if (lastSlash >= 0) {
			setParts(pathInfo.substring(lastSlash + 1));
		} else {
			setParts(pathInfo);
		}
	}

	private void setParts(String namePathInfo) {
		String[] parts = StringUtils.split(namePathInfo, '.');
		switch (parts.length) {
		case 0:
			this.requestName = "";
			this.requestSelectors = new String[0];
			this.requestExt = "";
			break;
		case 1:
			this.requestName = parts[0];
			this.requestSelectors = new String[0];
			this.requestExt = "";
			break;
		case 2:
			this.requestName = parts[0];
			this.requestSelectors = new String[0];
			this.requestExt = parts[1];
			break;
		default:
			this.requestName = parts[0];
			this.requestSelectors = Arrays.copyOfRange(parts, 2,
					parts.length - 1);
			this.requestExt = parts[1];
			break;
		}
	}


	@SuppressWarnings("unchecked")
	public <T> T adaptTo(Class<T> type) {
		try {
			if (Session.class.equals(type)) {
				return (T) session;
			} else if (ContentType.class.equals(type)) {
				
				return (T) new ContentType(content);
			} else if (InputStream.class.equals(type)) {
				if ( content == null ) {
					return null;
				}
				return (T) session.getContentManager().getInputStream(
						content.getPath());
			} else if (Content.class.equals(type)) {
				return (T) content;
			} else if (HttpServletRequest.class.equals(type)) {
				return (T) request;
			} else {
				return (T) resourceHandler.adaptTo(type);
			}
		} catch (StorageClientException e) {
			throw new ResourceErrorException(e.getMessage(), e);
		} catch (AccessDeniedException e) {
			throw new ResourceForbiddenException(e.getMessage(), e);
		} catch (IOException e) {
			throw new ResourceErrorException(e.getMessage(), e);
		}
	}

}
