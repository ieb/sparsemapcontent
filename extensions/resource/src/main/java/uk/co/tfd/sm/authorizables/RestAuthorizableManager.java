package uk.co.tfd.sm.authorizables;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.SparseSessionTracker;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;

import uk.co.tfd.sm.api.authn.AuthenticationService;
import uk.co.tfd.sm.api.jaxrs.JaxRestService;
import uk.co.tfd.sm.util.http.AuthorizableHelper;
import uk.co.tfd.sm.util.http.ModificationRequest;
import uk.co.tfd.sm.util.http.ResponseUtils;

@Component(immediate = true, metatype = true)
@Service(value = JaxRestService.class)
@Path("/system/userManager")
public class RestAuthorizableManager implements JaxRestService {

	@Reference
	protected SparseSessionTracker sessionTracker;

	@Reference
	protected AuthenticationService authenticationService;

	@GET
	@Path("{type:user|group}/{userid}.{format}")
	public Response getUser(@Context HttpServletRequest request,
			@PathParam(value = "type") String authorizableType,
			@PathParam(value = "userid") String authorizableId,
			@PathParam(value = "format") final String outputFormat) {
		try {

			AuthorizableManager authorizableManager = getAuthorizableManager(request);
			final Authorizable authorizable = authorizableManager
					.findAuthorizable(authorizableId);
			Response checkType = checkType(authorizable, authorizableType);
			if (checkType != null) {
				return checkType;
			}
			Date lastModified = new Date();
			Long lm = (Long) authorizable.getProperty(Authorizable.LASTMODIFIED_FIELD);
			if ( lm == null ) {
				lm = (Long) authorizable.getProperty(Authorizable.CREATED_FIELD);
			}
			if ( lm != null ) {
				lastModified = new Date(lm);
			}
			return Response
					.ok(new StreamingOutput() {
						@Override
						public void write(OutputStream output)
								throws IOException, WebApplicationException {
							ResponseUtils.writeTree(authorizable, outputFormat,
									output);
						}
					})
					.type(MediaType.APPLICATION_JSON_TYPE.toString()
							+ "; charset=utf-8")
					.lastModified(lastModified)
					.build();

		} catch (StorageClientException e) {
			return ResponseUtils.getResponse(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					e.getMessage());
		} catch (AccessDeniedException e) {
			return ResponseUtils.getResponse(HttpServletResponse.SC_FORBIDDEN,
					e.getMessage());
		}
	}

	@POST
	@Path("{type:user|group}/{userid}")
	public Response doUpdateAuthorizable(@Context HttpServletRequest request,
			@PathParam(value = "type") String authorizableType,
			@PathParam(value = "userid") String authorizableId) {
		try {
			AuthorizableManager authorizableManager = getAuthorizableManager(request);
			Authorizable authorizable = authorizableManager
					.findAuthorizable(authorizableId);
			Response checkType = checkType(authorizable, authorizableType);
			if (checkType != null) {
				return checkType;
			}

			// process the post request.
			AuthorizableHelper authorizableHelper = new AuthorizableHelper(
					authorizableManager);
			ModificationRequest modificationRequest = new ModificationRequest();
			modificationRequest.processRequest(request);
			authorizableHelper.applyProperties(authorizable,
					modificationRequest);
			authorizableHelper.save();
			final List<String> feedback = modificationRequest.getFeedback();

			return Response
					.ok(new StreamingOutput() {
						@Override
						public void write(OutputStream output)
								throws IOException, WebApplicationException {
							ResponseUtils.writeFeedback(feedback, output);
						}
					})
					.type(MediaType.APPLICATION_JSON_TYPE.toString()
							+ "; charset=utf-8").lastModified(new Date())
					.build();

		} catch (StorageClientException e) {
			return ResponseUtils.getResponse(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					e.getMessage());
		} catch (AccessDeniedException e) {
			return ResponseUtils.getResponse(HttpServletResponse.SC_FORBIDDEN,
					e.getMessage());
		} catch (IOException e) {
			return ResponseUtils.getResponse(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					e.getMessage());
		} catch (FileUploadException e) {
			return ResponseUtils.getResponse(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					e.getMessage());
		}
	}

	private AuthorizableManager getAuthorizableManager(
			HttpServletRequest request) throws StorageClientException {
		Session session = sessionTracker.get(request);
		if (session == null) {

			session = sessionTracker.register(
					authenticationService.authenticate(request), request);
		}
		return session.getAuthorizableManager();
	}

	private Response checkType(Authorizable authorizable,
			String authorizableType) {
		if (authorizable == null) {
			return ResponseUtils.getResponse(HttpServletResponse.SC_NOT_FOUND,
					"Authorizable not found");
		}
		if (("group".equals(authorizableType) && !authorizable.isGroup())
				|| ("user".equals(authorizableType) && authorizable.isGroup())) {
			return ResponseUtils.getResponse(
					HttpServletResponse.SC_BAD_REQUEST,
					"Request found the wrong type of object");
		}
		return null;
	}

}
