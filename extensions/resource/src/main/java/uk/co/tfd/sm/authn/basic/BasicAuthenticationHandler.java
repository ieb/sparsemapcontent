package uk.co.tfd.sm.authn.basic;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;
import uk.co.tfd.sm.api.authn.AuthenticationServiceHandler;

@Component(immediate=true, metatype=true)
@Service(value=AuthenticationServiceHandler.class)
public class BasicAuthenticationHandler implements AuthenticationServiceHandler {

	@Override
	public AuthenticationServiceCredentials getCredentials(
			HttpServletRequest request) {
		String auth = request.getHeader("Authorization");
		if (auth != null && auth.startsWith("Basic ")) {
			try {
				final String[] userNamePW = StringUtils.split(
						new String(Base64.decodeBase64(auth.substring("Basic "
								.length())), "UTF-8"), ":", 2);
				if (userNamePW != null && userNamePW.length == 2) {
					return new AuthenticationServiceCredentials() {

						@Override
						public String getUserName() {
							return userNamePW[0];
						}

						@Override
						public String getPassword() {
							return userNamePW[1];
						}

						@Override
						public Map<String, Object> getProperties() {
							return null;
						}
						@Override
						public boolean allowCreate() {
							return false;
						}
					};
				}
			} catch (UnsupportedEncodingException e) {
				// ignore, wont ever happen.
			}
		}
		return null;
	}

	@Override
	public int compareTo(AuthenticationServiceHandler o) {
		return o.getPriority()-getPriority();
	}

	@Override
	public int getPriority() {
		return -10;
	}

}
