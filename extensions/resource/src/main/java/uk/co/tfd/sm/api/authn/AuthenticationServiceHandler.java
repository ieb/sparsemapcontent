package uk.co.tfd.sm.api.authn;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationServiceHandler extends Comparable<AuthenticationServiceHandler> {

	AuthenticationServiceCredentials getCredentials(HttpServletRequest request);
	
	int getPriority();

}
