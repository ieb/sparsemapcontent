package uk.co.tfd.sm.authn.token;

import javax.servlet.http.HttpServletRequest;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;
import uk.co.tfd.sm.api.authn.AuthenticationServiceHandler;

@Component(immediate=true, metatype=true)
@Service(value=AuthenticationServiceHandler.class)
public class TokenAuthenticationHandler implements AuthenticationServiceHandler {

	@Reference
	TokenAuthenticationService tokenAuthenticationService;
	
	@Override
	public AuthenticationServiceCredentials getCredentials(
			HttpServletRequest request) {
		return tokenAuthenticationService.getCredentials(request);
	}

}
