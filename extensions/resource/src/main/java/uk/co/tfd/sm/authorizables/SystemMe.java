package uk.co.tfd.sm.authorizables;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.SparseSessionTracker;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.authn.AuthenticationService;
import uk.co.tfd.sm.api.jaxrs.JaxRestService;
import uk.co.tfd.sm.util.http.ResponseUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

@Component(immediate = true, metatype = true)
@Service(value = JaxRestService.class)
@Path("/system/me")
public class SystemMe implements JaxRestService {

	private static final String ANON_BODY = "{\"user\":{\"anon\":true,\"subjects\":[],\"superUser\":false},"
			+ "\"eventbus\":\"/system/uievent/default/anon\","
			+ "\"profile\":{\"basic\":{\"access\":\"everybody\","
			+ "\"elements\":{\"lastName\":{\"value\":\"User\"},"
			+ "\"email\":{\"value\":\"anon@sakai.invalid\"},"
			+ "\"firstName\":{\"value\":\"Anonymous\"}}},"
			+ "\"rep:userId\":\"anonymous\"},\"messages\":"
			+ "{\"unread\":0},\"contacts\":{},\"groups\":[]}";

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemMe.class);

	@Reference
	protected SparseSessionTracker sessionTracker;

	@Reference
	protected AuthenticationService authenticationService;

	@GET
	public Response doSystemMe(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {

		try {

			Session session = getSession(request, response);
			LOGGER.info("In System me, user is {} ",session.getUserId());
			if (User.ANON_USER.equals(session.getUserId())) {
				return Response.ok(ANON_BODY)
						.type(MediaType.APPLICATION_JSON + "; charset=utf-8")
						.lastModified(new Date())
						.header("x-varyb-user", User.ANON_USER).build();
			}

			AuthorizableManager authorizableManager = session
					.getAuthorizableManager();
			User user = (User) authorizableManager.findAuthorizable(session
					.getUserId());

			final Builder<String, Object> meFeed = ImmutableMap.builder();
			String id = user.getId();
			Builder<String, Object> userValue = ImmutableMap.builder();
			userValue.put("userid", id);
			userValue.put("userStoragePrefix", "~" + id + "/");
			userValue
					.put("userProfilePath", "/~" + id + "/public/authprofile/");
			userValue.put("superUser", user.isAdmin());
			userValue.put("properties", user.getProperties());
			userValue.put("subjects", new String[0]); // fix
			userValue.put("locale", Locale.UK); // fix
			Builder<String, Object> profile = ImmutableMap.builder();
			profile.put(
					"aboutme",
					ImmutableMap.of("init", "true", "path", "/~" + id
							+ "/public/authprofile/aboutme"));
			profile.put(
					"publications",
					ImmutableMap.of("publications", "true", "path", "/~" + id
							+ "/public/authprofile/publications"));
			profile.put("basic",
					ImmutableMap.of("access", "everybody", "elements",
							ImmutableMap.of(
									"lastName",
									ImmutableMap.of("value",
											emptyNull(user.getProperty("lastName"))),
									"firstName",
									ImmutableMap.of("value",
											emptyNull(user.getProperty("firstName"))),
									"email",
									ImmutableMap.of("value",
											emptyNull(user.getProperty("email"))))));
			profile.put("userid", id);
			profile.put("counts", ImmutableMap.of("contactCounts", 0,
					"membershipsCount", 0, "contentCount", 0,
					"countLastUpdate", System.currentTimeMillis()));
			Builder<String, Object> contacts = ImmutableMap.builder();
			contacts.put("accepted", 0);
			contacts.put("pending", 0);
			contacts.put("ACCEPTED", 3);
			contacts.put("invited", 0);
			meFeed.put("user", userValue.build());
			meFeed.put("eventbus", "/system/uievent/default/" + id);
			meFeed.put("profile", profile.build());
			meFeed.put("contacts", contacts.build());
			meFeed.put("groups", new String[0]);

			Date lastModified = new Date();
			Long lm = (Long) user.getProperty(Authorizable.LASTMODIFIED_FIELD);
			if (lm == null) {
				lm = (Long) user.getProperty(Authorizable.CREATED_FIELD);
			}
			if (lm != null) {
				lastModified = new Date(lm);
			}
			return Response
					.ok(new StreamingOutput() {
						@Override
						public void write(OutputStream output)
								throws IOException, WebApplicationException {
							ResponseUtils.writeTree(meFeed.build(), "json",
									output);
						}
					})
					.type(MediaType.APPLICATION_JSON_TYPE.toString()
							+ "; charset=utf-8").lastModified(lastModified)
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

	private Object emptyNull(Object value) {
		if ( value == null ) {
			return "";
		}
		return value;
	}

	private Session getSession(HttpServletRequest request,
			HttpServletResponse response) throws StorageClientException {
		Session session = sessionTracker.get(request);
		if (session == null) {

			session = sessionTracker.register(
					authenticationService.authenticate(request, response),
					request);
		}
		return session;
	}


}
