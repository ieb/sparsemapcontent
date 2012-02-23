package uk.co.tfd.sm.authn.openid;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface OpenIdService {

    /**
     * Get the identity from the request.
     * 
     * @param request
     * @return
     */
    Map<String, Object> getIdentity(HttpServletRequest request);

    /**
     * Get the redirect to send the user to authenticate against.
     * 
     * @param userSuppliedString
     * @param returnToUrl
     * @return
     */
    String getAuthRedirectUrl(String userSuppliedString, String returnToUrl);

}
