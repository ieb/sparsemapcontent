package uk.co.tfd.sm.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;

import com.google.common.collect.Lists;

public class ContentRequestStreamProcessor implements
		RequestStreamProcessor<Content> {

	private Content content;
	private ContentManager contentManager;
	private ContentHelper contentHelper;

	public ContentRequestStreamProcessor(Content content,
			ContentManager contentManager, ContentHelper contentHelper) {
		this.content = content;
		this.contentManager = contentManager;
		this.contentHelper = contentHelper;
	}

	@Override
	public List<String> processStream(String streamName, String fileName, String contentType, InputStream stream,
			ModificationRequest modificationRequest)
			throws StorageClientException, AccessDeniedException, IOException {
		String alternativeStreamName = RequestUtils.getStreamName(streamName);
		if ( fileName == null || fileName.length() == 0 ) {
			fileName = RequestUtils.getFileName(streamName);
		}
		List<String> feedback = Lists.newArrayList();
		String path = content.getPath();
		if (fileName != null) {
			path = StorageClientUtils.newPath(path, fileName);
			Content childContent = contentHelper.getOrCreateContent(path);
			childContent.setProperty(StorageClientUtils.getAltField(Content.MIMETYPE_FIELD, alternativeStreamName), contentType);
			contentHelper.applyProperties(childContent, modificationRequest);
		} else {
			// all properties to this point in a stream get
			// saved to the upload object.
			contentHelper.applyProperties(content, modificationRequest);
		}
		if (alternativeStreamName == null) {
			contentManager.writeBody(path, stream);
			feedback.add("Saved Stream " + fileName);
		} else {
			contentManager.writeBody(path, stream, alternativeStreamName);
			feedback.add("Saved Stream " + fileName + ":" + alternativeStreamName);
		}
		return feedback;
	}

}
