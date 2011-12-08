package uk.co.tfd.sm.util.http;

import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class ContentHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentHelper.class);
	private Map<String, Content> toSave;
	private ContentManager contentManager;
	private boolean debug;
	
	public ContentHelper(ContentManager contentManager) {
		this.contentManager = contentManager;
		toSave = Maps.newHashMap();
		this.debug = LOGGER.isDebugEnabled();
	}

	public void applyProperties(Content content, ModificationRequest modificationRequest) {
		for (Entry<String, Object> e : modificationRequest.getParameterSet(ParameterType.REMOVE).entrySet()) {
			content.removeProperty(e.getKey());
		}

		for (Entry<String, Object> e : modificationRequest.getParameterSet(ParameterType.ADD).entrySet()) {
			content.setProperty(e.getKey(), e.getValue());
		}
		modificationRequest.resetProperties();
		// add to the save list, just in case the content didnt come from here
		toSave.put(content.getPath(), content);
	}
	
	public Content getOrCreateContent(String contentPath)
			throws StorageClientException, AccessDeniedException {
		Content content = toSave.get(contentPath);
		if (content == null) {
			content = contentManager.get(contentPath);
			if (content == null) {
				if (debug) {
					LOGGER.debug("Created A New Unsaved Content object {} ",
							contentPath);
				}
				content = new Content(contentPath, null);
			} else if (debug) {
				LOGGER.debug("Content Existed at {} ", content);
			}
			toSave.put(contentPath, content);
		}
		return content;
	}
	
	public void save() throws AccessDeniedException, StorageClientException {
		for (Content c : toSave.values()) {
			contentManager.update(c);
			if (debug) {
				LOGGER.debug("Updated {} ", c);
			}
		}
	}

}
