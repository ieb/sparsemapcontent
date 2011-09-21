package uk.co.tfd.sm.milton;

import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Auth.Scheme;
import com.bradmcevoy.http.AuthenticationHandler;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;

public class AnonAuthHandler implements AuthenticationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnonAuthHandler.class);
	private SecurityManager securityManager;

	public AnonAuthHandler(SecurityManager securityManager) {
		this.securityManager = securityManager;
	}

	public boolean supports(Resource r, Request request) {
        Auth auth = request.getAuthorization();
        if (auth == null) {
        	request.setAuthorization(new Auth(Scheme.NEGOTIATE, User.ANON_USER, null));
            return true;
        }
        return false;
	}

	public Object authenticate(Resource resource, Request request) {
        LOGGER.debug("authenticate");
    	Object o = securityManager.authenticate(User.ANON_USER, null);
    	LOGGER.debug("result: " + o);
        return o;
	}

	public String getChallenge(Resource resource, Request request) {
		return null;
	}

	public boolean isCompatible(Resource resource) {
		return true;
	}

}
