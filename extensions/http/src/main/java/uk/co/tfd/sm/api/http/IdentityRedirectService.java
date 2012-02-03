package uk.co.tfd.sm.api.http;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides a mechanism to transfer identity from one host to annother with a
 * short lived token.
 * 
 * @author ieb
 * 
 */
public interface IdentityRedirectService {

	/**
	 * Extract the identity from the request.
	 * 
	 * @param request
	 *            the inbound request that should have been created by
	 *            getRedirectIdentityUrl.
	 * @return the identity that was transfered or null if there was no identity
	 *         in the request.
	 */
	String getIdentity(HttpServletRequest request);

	/**
	 * Get a redirect with identity
	 * 
	 * @param request
	 *            the request that needs to be transfered. It will be a GET
	 *            request and it will be transfered unchanged to the target
	 *            host.
	 * @param identity
	 *            the identity to transfer.
	 * @return the url of the redirection or null if there is no target. The URL
	 *         will include additional parameters to perform the transfer.
	 */
	String getRedirectIdentityUrl(HttpServletRequest request, String identity);

}
