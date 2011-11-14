package uk.co.tfd.sm.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;

import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.Resource;

public class DefaultGetResponse implements Adaptable {
	
		private Adaptable adaptable;

		public DefaultGetResponse(Adaptable adaptable) {
			this.adaptable = adaptable;
		}

		@GET
		public Response doGet() throws IOException {
			try {
				Resource resource = adaptable.adaptTo(Resource.class);
				final String requestExt = resource.getRequestExt();
				final String[] selectors = resource.getRequestSelectors();
				final Content content = adaptTo(Content.class);
				if ( content == null ) {
					return ResponseUtils.getResponse(HttpServletResponse.SC_NOT_FOUND, "Not Found");
				}
				if (requestExt == null || requestExt.isEmpty()) {
					Session session = adaptTo(Session.class);
					final ContentManager contentManager = session
							.getContentManager();
					final InputStream in = contentManager
							.getInputStream(content.getPath());

					return Response
							.ok(new StreamingOutput() {
								@Override
								public void write(OutputStream output)
										throws IOException,
										WebApplicationException {
									IOUtils.copy(in, output);
									in.close();

								}
							}).type(adaptTo(MediaType.class))
							.lastModified(adaptTo(Date.class)).build();
				} else if ("json".equals(requestExt)) {

					return Response.ok(new StreamingOutput() {
						@Override
						public void write(OutputStream output) throws IOException,
								WebApplicationException {
							ResponseUtils.writeTree(content,selectors, output);
						}
					})
							.type(MediaType.APPLICATION_JSON_TYPE)
							.lastModified(adaptTo(Date.class)).build();
				} else if ("xml".equals(requestExt)) {
					return Response.ok(content.getProperties())
							.type(MediaType.APPLICATION_XML_TYPE)
							.lastModified(adaptTo(Date.class)).build();
				}
				return  ResponseUtils.getResponse(HttpServletResponse.SC_BAD_REQUEST, "format " + requestExt + " not recognised");
			} catch (final StorageClientException e) {
				return ResponseUtils.getResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

			} catch (final AccessDeniedException e) {
				return ResponseUtils.getResponse(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
			}
		}

		public <T> T adaptTo(Class<T> type) {
			return adaptable.adaptTo(type);
		}
	}

