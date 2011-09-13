package uk.co.tfd.sm.webdav;

import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

public class MiltonContentResourceFactory implements ResourceFactory {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MiltonContentResource.class);
	private String basePath;

	public MiltonContentResourceFactory(String basePath) {
		this.basePath = basePath;
	}

	public Resource getResource(String host, String path) {
		Session session = (Session) HttpManager.request().getAuthorization()
				.getTag();
		if ( path == null ) {
			path = "/";
		} else if ( path != null && path.startsWith(basePath) ) {
			path = path.substring(basePath.length());
		}
		try {
			Content content = session.getContentManager().get(path);
			if (content != null) {
				return new MiltonContentResource(path, session, content);
			}
			LOGGER.info("found {} ", path);
			if ("/".equals(path) || "".equals(path) || path == null) {
				return new MiltonContentResource(path, session, new Content(
						"/", null));
			}
		} catch (StorageClientException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (AccessDeniedException e) {
			LOGGER.info(e.getMessage());
		}
		return null;
	}

}
