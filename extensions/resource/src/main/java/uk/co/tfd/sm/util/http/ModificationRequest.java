package uk.co.tfd.sm.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This class deals with processing a POST request. It needs a stream processor
 * to apply any streamed bodies within the request to something in the model. If
 * there is no stream processor given, it will ignore non form parts of a
 * multipart post.
 * 
 * Loading a standard post with no streaming.
 * <pre>
 *      {@link Authorizable} authorizable = xxx;
 * 		{@link AuthorizableHelper} authorizableHelper = new {@link AuthorizableHelper}(authorizableManager);
 *		{@link ModificationRequest} modificationRequest = new {@link ModificationRequest}(null);
 *
 *      // process the request
 *		modificationRequest.processRequest(request);
 *		 
 *		// apply the properties to the authorizable
 *		authorizableHelper.applyProperties(authorizable, modificationRequest);
 *		
 *		// save everything that was modified
 *		authorizableHelper.save();
 *		
 *		// get the feedback
 *		List<String> feedback = modificationRequest.getFeedback();
 * </pre>
 * Loading a multipart post with streaming.
 * <pre>
 *      {@link Content} authorizable = xxx;
 * 		{@link ContentHelper} contentHelper = new {@link ContentHelper}(contentManager);
 *      {@link ContentRequestStreamProcessor} contentRquestStreamProcessor = new {@link ContentRequestStreamProcessor}(content, contentManager, contentHelper)
 *		ModificationRequest modificationRequest = new {@link ModificationRequest}(contentRquestStreamProcessor);
 *
 *      // process the request
 *		modificationRequest.processRequest(request);
 *		 
 *		// apply the properties to the authorizable
 *		contentHelper.applyProperties(content, modificationRequest);
 *		
 *		// save everything that was modified
 *		contentHelper.save();
 *		
 *		// get the feedback
 *		List<String> feedback = modificationRequest.getFeedback();
 * </pre>
 *
 * 
 * 
 * @author ieb
 * 
 */
public class ModificationRequest {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ModificationRequest.class);
	private List<String> feedback = Lists.newArrayList();
	private RequestStreamProcessor<?> streamProcessor;
	private Map<ParameterType, Map<String, Object>> stores;
	
	/**
	 * Create a ModificationRequest that will handle streaming of bodies
	 * @param <T> the type of the StreamProcessor.
	 * @param streamProcessor
	 */
	public <T> ModificationRequest(RequestStreamProcessor<T> streamProcessor) {
		this.streamProcessor = streamProcessor;
		Builder<ParameterType, Map<String,Object>> b = ImmutableMap.builder();
		for ( ParameterType pt : ParameterType.values() ) {
			Map<String, Object> m = Maps.newHashMap();
			b.put(pt, m);
		}
		stores = b.build();
	}

	/**
	 * Create a ModificationRequest that will ignore streamed bodies.
	 */
	public ModificationRequest() {
		this(null);
	}

	/**
	 * Process the request in a stream.
	 * 
	 * @param request
	 * @throws IOException
	 * @throws FileUploadException
	 * @throws StorageClientException
	 * @throws AccessDeniedException
	 */
	public void processRequest(HttpServletRequest request) throws IOException,
			FileUploadException, StorageClientException, AccessDeniedException {
		boolean debug = LOGGER.isDebugEnabled();
		if (ServletFileUpload.isMultipartContent(request)) {
			if (debug) {
				LOGGER.debug("Multipart POST ");
			}
			feedback.add("Multipart Upload");
			ServletFileUpload upload = new ServletFileUpload();
			FileItemIterator iterator = upload.getItemIterator(request);
			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				if (debug) {
					LOGGER.debug("Got Item {}",item);
				}
				String name = item.getFieldName();
				InputStream stream = item.openStream();
				if (item.isFormField()) {
					ParameterType pt = ParameterType.typeOfRequestParameter(name);
					String propertyName = RequestUtils.propertyName(pt.getPropertyName(name));
					RequestUtils.accumulate(stores.get(pt), propertyName, RequestUtils.toValue(name, Streams.asString(stream)));
					feedback.add(pt.feedback(propertyName));
				} else {

					if (streamProcessor != null) {
						feedback.addAll(streamProcessor.processStream(name, StorageClientUtils.getObjectName(item.getName()), item.getContentType(), stream, this));
					}

				}
			}
			if (debug) {
				LOGGER.debug("No More items ");
			}

		} else {
			if (debug) {
				LOGGER.debug("Trad Post ");
			}
			// use traditional unstreamed operations.
			@SuppressWarnings("unchecked")
			Map<String, String[]> parameters = request.getParameterMap();
			if (debug) {
				LOGGER.debug("Traditional POST {} ", parameters);
			}
			Set<Entry<String, String[]>> entries = parameters.entrySet();

			for (Entry<String, String[]> param : entries) {
				String name = (String) param.getKey();
				ParameterType pt = ParameterType.typeOfRequestParameter(name);
				String propertyName = RequestUtils.propertyName(pt.getPropertyName(name));
				RequestUtils.accumulate(stores.get(pt), propertyName, RequestUtils.toValue(name, param.getValue()));
				feedback.add(pt.feedback(propertyName));
			}
		}
	}

	/**
	 * @return A map of the properties processed so far based on the stream
	 *         recieved.
	 */
	public Map<String, Object> getParameterSet(ParameterType pt) {
		return stores.get(pt);
	}

	/**
	 * Clear the current set of properties to add and remove.
	 */
	public void resetProperties() {
		for ( Entry<ParameterType, Map<String, Object>> e : stores.entrySet()) {
			e.getValue().clear();
		}
	}

	/**
	 * @return feedback from the resquest processing.
	 */
	public List<String> getFeedback() {
		return feedback;
	}
}
