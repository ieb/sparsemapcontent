package uk.co.tfd.sm.authn.openid;

import javax.servlet.http.HttpServletRequest;

public interface OpenIdService {

	/**
	 * Get the identity from the request.
	 * @param request
	 * @return
	 */
	String getIdentity(HttpServletRequest request);

	/**
	 * Get the redirect to send the user to authenticate against.
	 * @param userSuppliedString
	 * @param returnToUrl
	 * @return
	 */
	String getAuthRedirectUrl(String userSuppliedString, String returnToUrl);

}
