package uk.co.tfd.sm.search.es;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.search.RepositorySession;

public class RepositorySessionImpl implements RepositorySession {

	private static final Logger LOGGER = LoggerFactory.getLogger(RepositorySessionImpl.class);
	private Session session;
	private boolean logout;

	public RepositorySessionImpl(Session session, Repository repository) throws ClientPoolException, StorageClientException, AccessDeniedException {
		if ( session == null ) {
			this.session = repository.loginAdministrative();
			logout = true;
		} else {
			this.session = session;
			logout = false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T adaptTo(Class<T> c) {
		if ( Session.class.isAssignableFrom(c) ) {
			return (T) session;
		}
		return null;
	}

	@Override
	public void logout() {
		if ( logout ) {
			try {
				session.logout();
			} catch (ClientPoolException e) {
				LOGGER.error(e.getMessage(),e);
			}
			session = null;
		}
	}

}
