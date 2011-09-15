package uk.co.tfd.sm.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permission;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.common.ContentTypeUtils;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.FileResource;
import com.bradmcevoy.http.FolderResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class MiltonContentResource implements FileResource, FolderResource {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MiltonContentResource.class);
	private static final Map<Method, Permission> METHOD_PERMISSIONS = getMethodPermissionMap();
	private static final Set<Method> REDIRECT_METHODS = ImmutableSet.of(
			Method.ACL, Method.COPY, Method.DELETE, Method.MKCALENDAR,
			Method.MKCOL, Method.MOVE, Method.POST, Method.PROPPATCH,
			Method.PUT);
	private static final long LONG_MAX_AGE = 3600L * 24L * 45L;

	private String name;
	private Repository repository;
	private String path;
	private Content content;
	private Session session;

	public MiltonContentResource(String path, Session session, Content content) {
		this.name = StorageClientUtils.getObjectName(path);
		this.path = path;
		this.content = content;
		this.session = session;
		LOGGER.info("Created content with content object of {} {} ", this, this.content);
	}

	private static Map<Method, Permission> getMethodPermissionMap() {
		Builder<Method, Permission> b = ImmutableMap.builder();
		b.put(Method.ACL,
				Permissions.CAN_ANYTHING_ACL.combine(Permissions.CAN_READ));
		b.put(Method.CONNECT, Permissions.CAN_READ);
		b.put(Method.COPY, Permissions.CAN_READ); // need to be able to write to
													// the destination
		b.put(Method.DELETE,
				Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
		b.put(Method.GET, Permissions.CAN_READ);
		b.put(Method.HEAD, Permissions.CAN_READ);
		b.put(Method.LOCK, Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
		b.put(Method.MKCALENDAR,
				Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
		b.put(Method.MKCOL, Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
		b.put(Method.MOVE, Permissions.CAN_READ.combine(Permissions.CAN_WRITE)); // need
																					// to
																					// check
																					// destination
		b.put(Method.OPTIONS, Permissions.CAN_READ);
		b.put(Method.POST, Permissions.CAN_READ.combine(Permissions.CAN_WRITE)); // might
																					// need
																					// more
																					// here
		b.put(Method.PROPFIND, Permissions.CAN_READ);
		b.put(Method.PROPPATCH,
				Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
		b.put(Method.PUT, Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
		b.put(Method.REPORT, Permissions.CAN_READ);
		b.put(Method.TRACE, Permissions.CAN_READ);
		b.put(Method.UNLOCK,
				Permissions.CAN_READ.combine(Permissions.CAN_WRITE));
		return b.build();
	}

	public void copyTo(CollectionResource toCollection, String name)
			throws NotAuthorizedException, BadRequestException,
			ConflictException {
		try {
			session.getContentManager().copy(
					path,
					StorageClientUtils.newPath(
							((MiltonContentResource) toCollection).getPath(),
							name), true);
		} catch (StorageClientException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ConflictException(this, e.getMessage());
		} catch (AccessDeniedException e) {
			LOGGER.error(e.getMessage(), e);
			throw new NotAuthorizedException(toCollection);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ConflictException(this, e.getMessage());
		}
	}

	public String getUniqueId() {
		LOGGER.info("Getting Unique ID from {} ",content);
		return content.getId();
	}

	public String getName() {
		LOGGER.info("Getting name from {} ",content);
		return name;
	}

	public String getPath() {
		return path;
	}

	public Object authenticate(String user, String password) {
		LOGGER.info("Authenticating agains the resource ");
		try {
			if (user == null || User.ANON_USER.equals(user)) {
				return repository.login();
			}
			return repository.login(user, password);
		} catch (ClientPoolException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (StorageClientException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (AccessDeniedException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	public boolean authorise(Request request, Method method, Auth auth) {
		Session session = (Session) auth.getTag();
		if (session == null) {
			LOGGER.info("Not Authorized, session == null ");
			return false;
		}
		Permission permission = METHOD_PERMISSIONS.get(method);
		if (permission == null) {
			LOGGER.info("Not Authorized, permissions == null ");
			return false;
		}
		try {
			session.getAccessControlManager().check(Security.ZONE_CONTENT,
					path, permission);
			LOGGER.info("Authorized {} ", permission);
			return true;
		} catch (AccessDeniedException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (StorageClientException e) {
			LOGGER.error(e.getMessage(), e);
		}
		LOGGER.info("Authorize Failed ");
		return false;
	}

	public String getRealm() {
		LOGGER.info("Get Realm ");
		return null;
	}

	public Date getModifiedDate() {
		LOGGER.info("Get Modifled ");
		if (content != null) {
			if ( content.hasProperty(Content.LASTMODIFIED_FIELD)) {
				return new Date(
						(Long) content.getProperty(Content.LASTMODIFIED_FIELD));
			} else {
				return new Date();				
			}
		}
		return new Date(0L);
	}

	public String checkRedirect(Request request) {
		LOGGER.info("Check Redirect ");
		if (REDIRECT_METHODS.contains(request.getMethod())) {
			return path;
		}
		return null;
	}

	public void delete() throws NotAuthorizedException, ConflictException,
			BadRequestException {
		LOGGER.info("Delete ");
		try {
			Iterable<String> i = content.listChildPaths();
			if (i.iterator().hasNext()) {
				throw new ConflictException(this,
						"Cant delete if there are child resources");
			}
			session.getContentManager().delete(content.getPath());
		} catch (AccessDeniedException e) {
			throw new NotAuthorizedException(this);
		} catch (StorageClientException e) {
			throw new BadRequestException(this, e.getMessage());
		}
	}

	public void sendContent(OutputStream out, Range range,
			Map<String, String> params, String contentType) throws IOException,
			NotAuthorizedException, BadRequestException {
		LOGGER.info("Send Content ");
		try {
			InputStream in;
			try {
				in = session.getContentManager().getInputStream(path);
			} catch (IOException e) {
				throw new BadRequestException(this, e.getMessage());
			}
			if ( in == null ) {
				return;
			}
			byte[] buffer = new byte[10240];
			if ( range != null ) {
				try {
					in.skip(range.getStart());
				} catch (IOException e) {
					throw new BadRequestException(this, e.getMessage());
				}
			}
			for (;;) {
				int nr;
				try {
					nr = in.read(buffer);
				} catch (IOException e) {
					throw new BadRequestException(this, e.getMessage());
				}
				if (nr == -1) {
					break;
				}
				out.write(buffer, 0, nr);
			}
		} catch (StorageClientException e) {
			throw new BadRequestException(this, e.getMessage());
		} catch (AccessDeniedException e) {
			throw new NotAuthorizedException(this);
		}
	}

	public Long getMaxAgeSeconds(Auth auth) {
		LOGGER.info("Get Max Age for {} ", auth);
		Session session = (Session) auth.getTag();
		if (session == null || User.ANON_USER.equals(session.getUserId())) {
			return LONG_MAX_AGE;
		}
		try {
			User anonUser = (User) session.getAuthorizableManager()
					.findAuthorizable(User.ANON_USER);
			if (session.getAccessControlManager().can(anonUser,
					Security.ZONE_CONTENT, path, Permissions.CAN_READ)) {
				return LONG_MAX_AGE;
			}
		} catch (StorageClientException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (AccessDeniedException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	public String getContentType(String accepts) {
		LOGGER.info("Get Content type for {} ", content);
		if (content == null) {
			return null;
		}
		String contentType = (String) content
				.getProperty(Content.MIMETYPE_FIELD);
		if ( contentType == null ) {
			return null;
		}
		return ContentTypeUtils.findAcceptableContentType(contentType, accepts);
	}

	public Long getContentLength() {
		LOGGER.info("Get Content length {} ", content);
		if (content == null) {
			return null;
		}
		return (Long) content.getProperty(Content.LENGTH_FIELD);
	}

	public void moveTo(CollectionResource rDest, String name)
			throws ConflictException, NotAuthorizedException,
			BadRequestException {
		LOGGER.info("Move to {} {} ", rDest, name);
		try {
			session.getContentManager().moveWithChildren(
					path,
					StorageClientUtils.newPath(
							((MiltonContentResource) rDest).getPath(), name));
		} catch (StorageClientException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ConflictException(this, e.getMessage());
		} catch (AccessDeniedException e) {
			LOGGER.error(e.getMessage(), e);
			throw new NotAuthorizedException(rDest);
		}
	}

	public String processForm(Map<String, String> parameters,
			Map<String, FileItem> files) throws BadRequestException,
			NotAuthorizedException, ConflictException {
		LOGGER.info("Process form {} {} ", parameters, files);
		// TODO Auto-generated method stub
		return null;
	}

	public Date getCreateDate() {
		if (content == null) {
			return null;
		}
		Long created = (Long) content.getProperty(Content.CREATED_FIELD);
		if ( created != null ) {
			return new Date(created);
		} 
		return new Date(); 
	}

	public CollectionResource createCollection(String newName)
			throws NotAuthorizedException, ConflictException,
			BadRequestException {
		LOGGER.info("Create Collection ", newName);
		try {
			String newPath = StorageClientUtils.newPath(path, newName);
			ContentManager contentManager = session.getContentManager();
			Content newContent = contentManager.get(newPath);
			if (newContent != null) {
				throw new ConflictException(this, "Collection already exists");
			}
			newContent = new Content(newPath, null);
			contentManager.update(newContent);
			newContent = contentManager.get(newPath);
			return new MiltonContentResource(newPath, session, newContent);
		} catch (StorageClientException e) {
			throw new BadRequestException(this, e.getMessage());
		} catch (AccessDeniedException e) {
			throw new NotAuthorizedException(this);
		}
	}

	public Resource child(String childName) {
		LOGGER.info("Get Child ", childName);
		try {
			String newPath = StorageClientUtils.newPath(path, childName);
			Content c = session.getContentManager().get(newPath);
			if (c != null) {
				return new MiltonContentResource(newPath, session, c);
			}
		} catch (StorageClientException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (AccessDeniedException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	public List<? extends Resource> getChildren() {
		// this needs to be disposed by the system.
		LOGGER.info("Get Children ");
		final Iterator<Content> children = content.listChildren().iterator();
		return Lists
				.immutableList(new PreemptiveIterator<MiltonContentResource>() {

					private MiltonContentResource resource;

					@Override
					protected boolean internalHasNext() {
						while (children.hasNext()) {
							Content n = children.next();
							if ( n != null ) {
								resource = new MiltonContentResource(n.getPath(),
										session, n);
								return true;
							}
						}
						resource = null;
						return false;
					}

					@Override
					protected MiltonContentResource internalNext() {
						return resource;
					}

				});
	}

	public Resource createNew(String newName, InputStream inputStream,
			Long length, String contentType) throws IOException,
			ConflictException, NotAuthorizedException, BadRequestException {
		LOGGER.info("Create new {} ", newName);
		try {
			String newPath = StorageClientUtils.newPath(path, newName);
			ContentManager contentManager = session.getContentManager();
			Content newContent = contentManager.get(newPath);
			if (newContent == null) {
				newContent = new Content(newPath, ImmutableMap.of(
						Content.MIMETYPE_FIELD, (Object) contentType));
			} else {
				newContent.setProperty(Content.MIMETYPE_FIELD,
						(Object) contentType);
			}
			contentManager.update(newContent);
			contentManager.writeBody(newPath, inputStream);
			newContent = contentManager.get(newPath);
			return new MiltonContentResource(newPath, session, newContent);
		} catch (StorageClientException e) {
			throw new BadRequestException(this, e.getMessage());
		} catch (AccessDeniedException e) {
			throw new NotAuthorizedException(this);
		}
	}

}
