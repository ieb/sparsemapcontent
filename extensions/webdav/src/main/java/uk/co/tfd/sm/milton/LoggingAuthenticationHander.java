package uk.co.tfd.sm.milton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.AuthenticationHandler;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;

public class LoggingAuthenticationHander implements AuthenticationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAuthenticationHander.class);
	private SecurityManager securityManager;

	public LoggingAuthenticationHander(SecurityManager securityManager) {
		this.securityManager = securityManager;
	}

	public boolean supports(Resource r, Request request) {
		Auth a = request.getAuthorization();
		LOGGER.info("Supports {} {} {} ",new Object[]{r,request,a});
		return false;
	}

	public Object authenticate(Resource resource, Request request) {
		LOGGER.info("Authenticate {} {} ",new Object[]{resource,request});
		return null;
	}

	public String getChallenge(Resource resource, Request request) {
		LOGGER.info("Get Callenge {} {}  ",new Object[]{resource,request});
		return null;
	}

	public boolean isCompatible(Resource resource) {
		LOGGER.info("Is COmpatable {} ",new Object[]{resource});
		return false;
	}

}
