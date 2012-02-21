package uk.co.tfd.sm.authn.trusted;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;
import uk.co.tfd.sm.api.authn.AuthenticationServiceHandler;
import uk.co.tfd.sm.authn.TrustedCredentials;

/**
 * Trusts a url parameter to indicate he user. By default this authentication
 * handler is not enabled. Set trusted-url-parameter to the name of the
 * parameter to trust. It is vital that your front end LB or proxy protects this
 * parameter and only uses it to communicate a user has been logged in when they
 * are logged in, ie log the user in on a stub url, not on every request.
 * 
 * @author ieb
 * 
 */
@Component(immediate = true, metatype = true)
@Service(value = AuthenticationServiceHandler.class)
public class TrustedUrlAuthenticationHandler implements
		AuthenticationServiceHandler {

	@Property(value = "none")
	private static final String TRUSTED_PARAMETER_NAME = "trusted-url-parameter";
	private boolean disabled;
	private String trustedParameterName;

	@Activate
	protected void activate(Map<String, Object> properties) {
		modified(properties);
	}

	@Modified
	protected void modified(Map<String, Object> properties) {
		trustedParameterName = (String) properties.get(TRUSTED_PARAMETER_NAME);
		disabled = (trustedParameterName == null
				|| trustedParameterName.length() == 0 || "none"
				.equals(trustedParameterName));
	}

	@Override
	public AuthenticationServiceCredentials getCredentials(
			HttpServletRequest request) {
		if (!disabled) {
			String param = request.getParameter(trustedParameterName);
			if (param != null && param.trim().length() > 0) {
				return new TrustedCredentials(param);
			}
		}
		return null;
	}

}
