package uk.co.tfd.sm.authn;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;

public class TrustedCredentials implements AuthenticationServiceCredentials {

	private String userName;

	public TrustedCredentials(String userName) {
		this.userName = userName;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public String getPassword() {
		return null;
	}

}
