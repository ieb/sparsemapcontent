package uk.co.tfd.sm.authn;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;

/**
 * These credentials may not create a trusted token, but may be used to performa  login without password.
 * @author ieb
 *
 */
public class TransferCredentials implements AuthenticationServiceCredentials {

	private String userId;

	public TransferCredentials(String userId) {
		this.userId = userId;
	}

	@Override
	public String getUserName() {
		return userId;
	}

	@Override
	public String getPassword() {
		return null;
	}

}
