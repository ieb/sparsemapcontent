package uk.co.tfd.sm.authn.identity;

import javax.servlet.http.HttpServletRequest;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;
import uk.co.tfd.sm.api.authn.AuthenticationServiceHandler;
import uk.co.tfd.sm.api.http.IdentityRedirectService;
import uk.co.tfd.sm.authn.TransferCredentials;

@Component(immediate=true, metatype=true)
@Service(value=AuthenticationServiceHandler.class)
public class IdentityAuthenticationHandler implements AuthenticationServiceHandler {

	@Reference
	IdentityRedirectService identityRedirectService;
	
	@Override
	public AuthenticationServiceCredentials getCredentials(
			HttpServletRequest request) {
		String userId = identityRedirectService.getIdentity(request);
		if ( userId != null ) {
			return new TransferCredentials(userId);
		}
		return null;
	}

}
