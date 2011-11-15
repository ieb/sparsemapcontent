package uk.co.tfd.sm.api.authn;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationServiceHandler {

	AuthenticationServiceCredentials getCredentials(HttpServletRequest request);

}
