package uk.co.tfd.sm.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.Resource;

public class DefaultResponse implements Adaptable {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResponse.class);
	private Adaptable adaptable;

	public DefaultResponse(Adaptable adaptable) {
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
						}).type(MediaType.APPLICATION_JSON_TYPE)
						.lastModified(adaptTo(Date.class)).build();
			} else if ("xml".equals(requestExt)) {
				return Response.ok(content.getProperties())
						.type(MediaType.APPLICATION_XML_TYPE)
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
		LOGGER.debug("Executing POST ");
		Resource resource = adaptable.adaptTo(Resource.class);
		HttpServletRequest request = adaptable
				.adaptTo(HttpServletRequest.class);
		Session session = adaptTo(Session.class);
		try {
			ContentManager contentManager = session.getContentManager();
			String contentPath = resource.getToCreatePath();
			Content content = contentManager.get(contentPath);
			if (content == null) {
				LOGGER.info("Created New Content {} ",contentPath);
				content = new Content(contentPath, null);
			} else {
				LOGGER.info("Content Existed at {} ",content);
			}
			Set<String> toRemove = Sets.newHashSet();
			Map<String, Object> toAdd = Maps.newHashMap();
			final List<String> feedback = Lists.newArrayList();
			if (ServletFileUpload.isMultipartContent(request)) {
				LOGGER.info("Multipart POST ");
				feedback.add("Multipart Upload");
				ServletFileUpload upload = new ServletFileUpload();
				FileItemIterator iterator = upload.getItemIterator(request);
				while (iterator.hasNext()) {
					FileItemStream item = iterator.next();
					String name = item.getFieldName();
					InputStream stream = item.openStream();
					if (item.isFormField()) {
						String propertyName = RequestUtils.propertyName(name);
						if (RequestUtils.isDelete(name)) {
							toRemove.add(propertyName);
							feedback.add("Removed " + propertyName);
						} else {
							accumulate(
									toAdd,
									propertyName,
									RequestUtils.toValue(name,
											Streams.asString(stream)));
							feedback.add("Added " + propertyName);
						}
					} else {
						String alternativeStreamName = RequestUtils
								.getStreamName(name);
						if (alternativeStreamName == null) {
							contentManager.writeBody(content.getPath(), stream);
							feedback.add("Saved Stream " + name);
						} else {
							contentManager.writeBody(content.getPath(), stream,
									alternativeStreamName);
							feedback.add("Saved Stream " + name);
						}
					}
				}

			} else {
				LOGGER.info("Traditional POST ");

				// use traditional unstreamed operations.
				@SuppressWarnings("unchecked")
				Map<String, String[]> parameters = request
				.getParameterMap();
				LOGGER.info("Traditional POST {} ",parameters);
				Set<Entry<String, String[]>> entries = parameters.entrySet();

				for (Entry<String, String[]> param : entries) {
					String name = (String) param.getKey();
					String propertyName = RequestUtils.propertyName(name);
					if (RequestUtils.isDelete(name)) {
						toRemove.add(propertyName);
						feedback.add("Removed " + propertyName);
					} else {
						accumulate(toAdd, propertyName,
								RequestUtils.toValue(name, param.getValue()));
						feedback.add("Added " + propertyName);
					}

				}
			}

			for (String k : toRemove) {
				content.removeProperty(k);
			}

			for (Entry<String, Object> e : toAdd.entrySet()) {
				content.setProperty(e.getKey(), e.getValue());
			}

			LOGGER.info("Updating {} ",content);
			contentManager.update(content);
			return Response
					.ok(new StreamingOutput() {
						@Override
						public void write(OutputStream output)
								throws IOException, WebApplicationException {
							ResponseUtils.writeFeedback(feedback, output);
						}
					}).type(MediaType.APPLICATION_JSON_TYPE)
					.lastModified(adaptTo(Date.class)).build();
		} catch (StorageClientException e) {
			LOGGER.debug(e.getMessage(),e);
			return ResponseUtils.getResponse(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					e.getMessage());

		} catch (AccessDeniedException e) {
			LOGGER.debug(e.getMessage(),e);
			return ResponseUtils.getResponse(HttpServletResponse.SC_FORBIDDEN,
					e.getMessage());
		} catch (FileUploadException e) {
			LOGGER.debug(e.getMessage(),e);
			throw new IOException(e);
		}

	}

	private void accumulate(Map<String, Object> toAdd, String propertyName,
			Object value) {
		Object o = toAdd.get(propertyName);
		if ( o == null ) {
			toAdd.put(propertyName, value);
			LOGGER.info("Saved {} {}",propertyName,value);
		} else {
			int sl = 1;
			try { 
				sl = Array.getLength(o);
			} catch ( IllegalArgumentException e) {
				Object[] newO = (Object[]) Array.newInstance(o.getClass(), 1);
				newO[0] = o;
				o = newO;
			}
			int vl = 1;
			try { 
				vl = Array.getLength(value);
			} catch ( IllegalArgumentException e) {
				Object[] newO = (Object[]) Array.newInstance(value.getClass(), 1);
				newO[0] = value;
				value = newO;
			}
			Object type = Array.get(o, 0);
			Object[] newArray = (Object[]) Array.newInstance(type.getClass(), sl+vl);
			System.arraycopy(o, 0, newArray, 0, sl);
			System.arraycopy(value, 0, newArray, sl, vl);
			toAdd.put(propertyName, newArray);
			LOGGER.info("Appended {} {} {}",new Object[]{propertyName, value, newArray});
		}
	}

	public <T> T adaptTo(Class<T> type) {
		return adaptable.adaptTo(type);
	}
}
