package uk.co.tfd.sm.authn.openid;

import javax.servlet.http.HttpServletRequest;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;
import uk.co.tfd.sm.api.authn.AuthenticationServiceHandler;
import uk.co.tfd.sm.authn.TrustedCredentials;

@Component(immediate=true, metatype=true)
@Service(value=AuthenticationServiceHandler.class)
public class OpenIdAuthenticationHandler implements AuthenticationServiceHandler {

	@Reference
	OpenIdService openIdService;
	
	@Override
	public AuthenticationServiceCredentials getCredentials(
			HttpServletRequest request) {
		String userId = openIdService.getIdentity(request);
		if ( userId != null ) {
			// TrustedCredentials cause a Token to be set for subsequent requests.
			return new TrustedCredentials(userId);
		}
		return null;
	}

}
