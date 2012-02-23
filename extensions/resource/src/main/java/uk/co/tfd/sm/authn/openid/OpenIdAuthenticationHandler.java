package uk.co.tfd.sm.authn.openid;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;
import uk.co.tfd.sm.api.authn.AuthenticationServiceHandler;

@Component(immediate = true, metatype = true)
@Service(value = AuthenticationServiceHandler.class)
public class OpenIdAuthenticationHandler implements AuthenticationServiceHandler {

    @Reference
    OpenIdService openIdService;

    @Override
    public AuthenticationServiceCredentials getCredentials(HttpServletRequest request) {
        Map<String, Object> attributes = openIdService.getIdentity(request);
        if (attributes != null) {
            // TrustedCredentials cause a Token to be set for subsequent
            // requests.
            return new OpenIdCredentials(attributes);
        }
        return null;
    }

    @Override
    public int compareTo(AuthenticationServiceHandler o) {
        return o.getPriority() - getPriority();
    }

    @Override
    public int getPriority() {
        return 20;
    }

}
