package uk.co.tfd.sm.authn.openid;

import java.util.Map;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;

public class OpenIdCredentials implements AuthenticationServiceCredentials {

	private Map<String, Object> attributes;

	public OpenIdCredentials(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getUserName() {
		return (String) attributes.get("username");
	}

	@Override
	public String getPassword() {
		return null;
	}
	
	public Map<String, Object> getProperties() {
		return attributes;
	}
	@Override
	public boolean allowCreate() {
		return true;
	}

}
