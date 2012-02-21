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
 * This uses a header in the request to transfer the userid from a front end
 * proxy. The proxy must ensure that the header is protected and no client
 * application could set the header. By default this Handler is not enabled. Set
 * the value of trusted-header-name to none to disable or to the header name to
 * enable.
 * 
 * @author ieb
 * 
 */
@Component(immediate = true, metatype = true)
@Service(value = AuthenticationServiceHandler.class)
public class TrustedHeaderAuthenticationHandler implements
		AuthenticationServiceHandler {

	@Property(value = "none")
	private static final String TRUSTED_HEADER_NAME = "trusted-header-name";
	private boolean disabled;
	private String trustedHeader;

	@Activate
	protected void activate(Map<String, Object> properties) {
		modified(properties);
	}

	@Modified
	protected void modified(Map<String, Object> properties) {
		trustedHeader = (String) properties.get(TRUSTED_HEADER_NAME);
		disabled = (trustedHeader == null || trustedHeader.length() == 0 || "none"
				.equals(trustedHeader));
	}

	@Override
	public AuthenticationServiceCredentials getCredentials(
			HttpServletRequest request) {
		if (!disabled) {
			String header = request.getHeader(trustedHeader);
			if (header != null && header.trim().length() > 0) {
				return new TrustedCredentials(header);
			}
		}
		return null;
	}

}
