package uk.co.tfd.sm.api.http;

import javax.servlet.http.HttpServletRequest;

/**
 * This service provides Cross Site Request Forgery protection tokens. Those tokens
 * may be checked by a filter or something else to ensure that posts are being performed
 * by genuine clients
 * @author ieb
 *
 */
public interface CSRFProtectionService {

	/**
	 * Get a Token based on the request, should take into account at least the 
	 * host where the request is being made. The token should also be relatively 
	 * secure and not leaked unnecessarily. Ideally the token will timeout.
	 * @param request
	 * @return
	 */
	String getCSRFToken(HttpServletRequest request);

}
