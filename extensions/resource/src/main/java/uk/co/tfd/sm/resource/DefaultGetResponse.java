package uk.co.tfd.sm.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.commons.io.IOUtils;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
				final Content content = adaptTo(Content.class);
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
							Gson gson = new GsonBuilder().create();
							output.write(gson.toJson(content.getProperties()).getBytes("UTF-8"));
						}
					})
							.type(MediaType.APPLICATION_JSON_TYPE)
							.lastModified(adaptTo(Date.class)).build();
				} else if ("xml".equals(requestExt)) {
					return Response.ok(content.getProperties())
							.type(MediaType.APPLICATION_XML_TYPE)
							.lastModified(adaptTo(Date.class)).build();
				}
				return Response.status(new StatusType() {

					public int getStatusCode() {
						return Status.BAD_REQUEST.getStatusCode();
					}

					public String getReasonPhrase() {
						return "format " + requestExt + " not recognised";
					}

					public Family getFamily() {
						return Family.CLIENT_ERROR;
					}
				}).build();

			} catch (final StorageClientException e) {
				return Response.status(new StatusType() {

					public int getStatusCode() {
						return Status.INTERNAL_SERVER_ERROR.getStatusCode();
					}

					public String getReasonPhrase() {
						return e.getMessage();
					}

					public Family getFamily() {
						return Family.SERVER_ERROR;
					}
				}).build();

			} catch (final AccessDeniedException e) {
				return Response.status(new StatusType() {

					public int getStatusCode() {
						return Status.FORBIDDEN.getStatusCode();
					}

					public String getReasonPhrase() {
						return e.getMessage();
					}

					public Family getFamily() {
						return Family.CLIENT_ERROR;
					}
				}).build();
			}
		}

		public <T> T adaptTo(Class<T> type) {
			return adaptable.adaptTo(type);
		}
	}

