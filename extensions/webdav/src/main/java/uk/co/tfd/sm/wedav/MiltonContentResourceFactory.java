package uk.co.tfd.sm.wedav;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(MiltonContentResource.class);
	
	public MiltonContentResourceFactory() {
	}

	public Resource getResource(String host, String path) {
		Session session = (Session) HttpManager.request().getAuthorization().getTag();
		try {
		Content content = session.getContentManager().get(path);
		if ( content != null ) {
			return new MiltonContentResource(path, session, content);
		}
		} catch ( StorageClientException e ) {
			LOGGER.error(e.getMessage(),e);
		} catch (AccessDeniedException e) {
			LOGGER.info(e.getMessage());
		}
		return null;
	}

}
