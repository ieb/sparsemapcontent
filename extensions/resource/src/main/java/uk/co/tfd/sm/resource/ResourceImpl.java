package uk.co.tfd.sm.resource;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;

import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.Resource;
import uk.co.tfd.sm.api.resource.ResourceErrorException;
import uk.co.tfd.sm.api.resource.ResourceForbiddenException;
import uk.co.tfd.sm.api.resource.binding.ResponseBindingList;
import uk.co.tfd.sm.api.resource.binding.RuntimeResponseBinding;

import com.google.common.collect.Lists;

public class ResourceImpl implements Resource {

	private static final String RESOURCE_TYPE_FIELD = "resourceType";
	private static final String DEFAULT_RESOURCE_TYPE = MediaType.APPLICATION_OCTET_STREAM;
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
	private ResponseBindingList responseBindingList;
	private String resourceType;
	private HttpServletResponse response;
	private Date lastModified;
	private MediaType mediaType;
	/**
	 * The last name path is the longest path before a / in the request URL. 
	 * It indicates the resource that should be created if one would be created.
	 */
	private String toCreatePath;

	public ResourceImpl(Adaptable resourceHandler, HttpServletRequest request,
			HttpServletResponse response, Session session, Content content, String resolvedPath,
			String requestPath, String lastNamePath) {
		this.resourceHandler = resourceHandler;
		this.request = request;
		this.response = response;
		this.session = session;
		this.content = content;
		this.resolvedPath = resolvedPath;
		this.requestPath = requestPath;
		this.pathInfo = requestPath.substring(resolvedPath.length());
		this.toCreatePath = lastNamePath;
		int lastSlash = pathInfo.lastIndexOf('/');
		if (lastSlash == pathInfo.length() - 1) {
			this.requestName = "";
			setParts(""); // ends with /
		} else {
			int dot = pathInfo.indexOf(".", lastSlash+1);
			if ( dot >= 0 ) {
				this.requestName = pathInfo.substring(lastSlash+1,dot);
				setParts(pathInfo.substring(dot)); // there was a /
			} else {
				this.requestName = pathInfo.substring(lastSlash+1);
				setParts("");
			}
		}
		List<RuntimeResponseBinding> bindingList = Lists.newArrayList();
		resourceType = getType();
		mediaType = MediaType.valueOf(resourceType);
		lastModified = getLastModified();
		String method = request.getMethod();
		String bindingExt = checkAny(requestExt);
		for (String selector : checkAny(requestSelectors)) {
			bindingList.add(new RuntimeResponseBinding(method, resourceType,
					selector, bindingExt));
		}
		responseBindingList = new ResponseBindingList(
				bindingList.toArray(new RuntimeResponseBinding[bindingList
						.size()]));
	}

	private Date getLastModified() {
		if ( content != null && content.hasProperty(Content.LASTMODIFIED_FIELD)) {
			return new Date((Long) content.getProperty(Content.LASTMODIFIED_FIELD));
		}
		return new Date(0);
	}

	private String[] checkAny(String[] spec) {
		if ( spec == null || spec.length == 0 ) {
			return new String[]{BindingSearchKey.ANY};
		}
		return spec;
	}

	private String checkAny(String spec) {
		if ( spec == null  ) {
			return BindingSearchKey.ANY;
		}
		return spec;
	}

	private String getType() {
		if ( content != null ) {
			if (content.hasProperty(RESOURCE_TYPE_FIELD)) {
				return (String) content.getProperty(RESOURCE_TYPE_FIELD);
			}
			if (content.hasProperty(Content.MIMETYPE_FIELD)) {
				return (String) content.getProperty(Content.MIMETYPE_FIELD);
			}
		}
		return DEFAULT_RESOURCE_TYPE;
	}

	private void setParts(String namePathInfo) {
		String[] parts = StringUtils.split(namePathInfo, '.');
		switch (parts.length) {
		case 0:
			this.requestSelectors = new String[0];
			this.requestExt = "";
			break;
		case 1:
            this.requestSelectors = new String[0];
			this.requestExt = parts[0];
			break;
		default:
			this.requestSelectors = Arrays.copyOfRange(parts, 0,
					parts.length - 1);
			this.requestExt = parts[parts.length - 1];
			break;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T adaptTo(Class<T> type) {
		try {
			if (Resource.class.equals(type)) {
				return (T) this;
			} else if (ResponseBindingList.class.equals(type)) {
				return (T) responseBindingList;
			} else if ( MediaType.class.equals(type)) {
				return (T) mediaType;
			} else if ( Date.class.equals(type)) {
				return (T) lastModified;
			} else if (Session.class.equals(type)) {
				return (T) session;
			} else if (InputStream.class.equals(type)) {
				if (content == null) {
					return null;
				}
				return (T) session.getContentManager().getInputStream(
						content.getPath());
			} else if (Content.class.equals(type)) {
				return (T) content;
			} else if (HttpServletRequest.class.equals(type)) {
				return (T) request;
			} else if (HttpServletResponse.class.equals(type)) {
				return (T) response;
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

	public String getResolvedPath() {
		return resolvedPath;
	}

	public String getRequestPath() {
		return requestPath;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public String[] getRequestSelectors() {
		return requestSelectors;
	}

	public String getRequestExt() {
		return requestExt;
	}

	public String getRequestName() {
		return requestName;
	}

	public String getResourceType() {
		return resourceType;
	}
	
	public String getToCreatePath() {
		return toCreatePath;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format(
				"requestPath=[{0}] resource path=[{1}], resourceType=[{2}], selectors={3}, ext=[{4}], toCreatePath=[{5}]",
			    requestPath, resolvedPath, resourceType, Arrays.toString(requestSelectors),
				requestExt, toCreatePath);
	}

}
