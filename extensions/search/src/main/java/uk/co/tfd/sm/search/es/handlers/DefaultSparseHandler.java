package uk.co.tfd.sm.search.es.handlers;

import java.util.Collection;
import java.util.List;

import org.osgi.service.event.Event;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.search.IndexingHandler;
import uk.co.tfd.sm.api.search.InputDocument;
import uk.co.tfd.sm.api.search.RepositorySession;

import com.google.common.collect.Lists;

/**
 * Indexes content using the content type to lookup the indexing configuration
 * in the content system.
 */
public class DefaultSparseHandler implements IndexingHandler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultSparseHandler.class);
	private String indexName;

	public DefaultSparseHandler(String indexName) {
		this.indexName = indexName;
	}

	public Collection<InputDocument> getDocuments(
			RepositorySession repositorySession, Event event) {
		LOGGER.debug("GetDocuments for {} ", event);
		String path = (String) event.getProperty(FIELD_PATH);
		boolean deleted = Boolean.parseBoolean(String.valueOf(event
				.getProperty(FIELD_DELETE)));
		List<InputDocument> documents = Lists.newArrayList();
		if (path != null) {
			try {
				Session session = repositorySession.adaptTo(Session.class);
				ContentManager contentManager = session.getContentManager();
				if (deleted) {
					String resourceType = (String) event
							.getProperty(FIELD_RESOURCE_TYPE);
					Content indexingConfig = contentManager
							.get("/var/search/indexing/" + resourceType);
					if (indexingConfig != null) {
						documents.add(new DeleteInputDocument(indexName, path));
					}
				} else {
					Content content = contentManager.get(path);
					if (content != null) {
						if (Boolean.parseBoolean(String.valueOf(content
								.getProperty("sling:excludeSearch")))) {
							return documents;
						}
						String contentType = (String) content
								.getProperty("sling:resourceType");
						Content indexingConfig = contentManager
								.get("/var/search/indexing/" + contentType);
						if (indexingConfig != null) {
							documents.add(new PropertiesInputDocument(
									indexName, path, content.getProperties(),
									indexingConfig.getProperties()));
						}
					}
				}
			} catch (StorageClientException e) {
				LOGGER.warn(e.getMessage(), e);
			} catch (AccessDeniedException e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
		LOGGER.debug("Got documents {} ", documents);
		return documents;
	}


}
