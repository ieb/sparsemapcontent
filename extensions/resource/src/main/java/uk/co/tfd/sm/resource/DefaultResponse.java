package uk.co.tfd.sm.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.Resource;
import uk.co.tfd.sm.util.http.ContentHelper;
import uk.co.tfd.sm.util.http.ContentRequestStreamProcessor;
import uk.co.tfd.sm.util.http.ModificationRequest;
import uk.co.tfd.sm.util.http.ResponseUtils;

public class DefaultResponse implements Adaptable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultResponse.class);
	private Adaptable adaptable;
	private boolean debug;

	public DefaultResponse(Adaptable adaptable) {
		debug = LOGGER.isDebugEnabled();
		this.adaptable = adaptable;
	}

	@GET
	public Response doGet() throws IOException {
		try {
			Resource resource = adaptable.adaptTo(Resource.class);
			final String requestExt = resource.getRequestExt();
			final String[] selectors = resource.getRequestSelectors();
			final Content content = adaptTo(Content.class);
			if (content == null) {
				return ResponseUtils.getResponse(
						HttpServletResponse.SC_NOT_FOUND, "Not Found");
			}
			if (!content.getPath().equals(resource.getToCreatePath())) {
				// ie the Content item does not exist.
				return ResponseUtils.getResponse(
						HttpServletResponse.SC_NOT_FOUND,
						"Not Found " + content.getPath() + " is not "
								+ resource.getToCreatePath());
			}
			if (debug) {
				LOGGER.debug("Get found Resource:[{}] Content:[{}]", resource,
						content);
			}
			if (requestExt == null || requestExt.isEmpty()) {
				Session session = adaptTo(Session.class);
				final ContentManager contentManager = session
						.getContentManager();
				final InputStream in = contentManager.getInputStream(content
						.getPath());

				return Response
						.ok(new StreamingOutput() {
							@Override
							public void write(OutputStream output)
									throws IOException, WebApplicationException {
								IOUtils.copy(in, output);
								in.close();

							}
						}).type(adaptTo(MediaType.class))
						.lastModified(adaptTo(Date.class)).build();
			} else if ("json".equals(requestExt)) {

				return Response
						.ok(new StreamingOutput() {
							@Override
							public void write(OutputStream output)
									throws IOException, WebApplicationException {
								ResponseUtils.writeTree(content, selectors,
										output);
							}
						})
						.type(MediaType.APPLICATION_JSON_TYPE.toString()
								+ "; charset=utf-8")
						.lastModified(adaptTo(Date.class)).build();
			} else if ("xml".equals(requestExt)) {
				return Response
						.ok(content.getProperties())
						.type(MediaType.APPLICATION_XML_TYPE.toString()
								+ "; charset=utf-8")
						.lastModified(adaptTo(Date.class)).build();
			}
			return ResponseUtils.getResponse(
					HttpServletResponse.SC_BAD_REQUEST, "format " + requestExt
							+ " not recognised");
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
	public Response doPost() throws IOException {
		if (debug) {
			LOGGER.debug("Executing POST ");
		}
		Resource resource = adaptable.adaptTo(Resource.class);
		HttpServletRequest request = adaptable
				.adaptTo(HttpServletRequest.class);
		Session session = adaptTo(Session.class);
		try {
			ContentManager contentManager = session.getContentManager();
			String contentPath = resource.getToCreatePath();
			
			ContentHelper contentHelper = new ContentHelper(contentManager);
			Content content = contentHelper.getOrCreateContent(contentPath);
			ContentRequestStreamProcessor contentRequestStreamProcessor = new ContentRequestStreamProcessor(content, contentManager, contentHelper);
			ModificationRequest modificationRequest = new ModificationRequest(contentRequestStreamProcessor);
			modificationRequest.processRequest(request);
			contentHelper.applyProperties(content, modificationRequest);
			contentHelper.save();
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
							+ "; charset=utf-8")
					.lastModified(new Date()).build();
		} catch (StorageClientException e) {
			if (debug) {
				LOGGER.debug(e.getMessage(), e);
			}
			return ResponseUtils.getResponse(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					e.getMessage());

		} catch (AccessDeniedException e) {
			if (debug) {
				LOGGER.debug(e.getMessage(), e);
			}
			return ResponseUtils.getResponse(HttpServletResponse.SC_FORBIDDEN,
					e.getMessage());
		} catch (FileUploadException e) {
			if (debug) {
				LOGGER.debug(e.getMessage(), e);
			}
			throw new IOException(e);
		}

	}




	public <T> T adaptTo(Class<T> type) {
		return adaptable.adaptTo(type);
	}
}
