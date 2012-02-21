package uk.co.tfd.sm.authn.token;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;

public interface TokenAuthenticationService {

	AuthenticationServiceCredentials getCredentials(HttpServletRequest request);
	

	AuthenticationServiceCredentials refreshCredentials(AuthenticationServiceCredentials credentials,
			HttpServletRequest request, HttpServletResponse response);

}
