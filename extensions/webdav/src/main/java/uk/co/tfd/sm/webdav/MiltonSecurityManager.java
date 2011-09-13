package uk.co.tfd.sm.webdav;

import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.http11.auth.DigestResponse;

public class MiltonSecurityManager implements SecurityManager {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MiltonSecurityManager.class);
	private String realm;
	private Repository reposiotry;

	public MiltonSecurityManager(Repository repository) {
		this.reposiotry = repository;
	}

	public Object authenticate(DigestResponse digestRequest) {
		return null;
	}

	public Object authenticate(String user, String password) {
		try {
			LOGGER.info("Authenticating {} ",user);
			if (user == null || User.ANON_USER.equals(user)) {
				return reposiotry.login();
			}
			return reposiotry.login(user, password);
		} catch (StorageClientException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (AccessDeniedException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	public boolean authorise(Request request, Method method, Auth auth,
			Resource resource) {
		return true;
	}

	public String getRealm(String host) {
		return realm;
	}

	public boolean isDigestAllowed() {
		return false;
	}

}
