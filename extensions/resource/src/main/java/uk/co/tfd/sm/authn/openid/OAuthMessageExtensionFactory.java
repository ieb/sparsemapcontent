package uk.co.tfd.sm.authn.openid;

import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.MessageExtensionFactory;
import org.openid4java.message.ParameterList;

public class OAuthMessageExtensionFactory implements MessageExtensionFactory {
	public static final String OAUTH_EXTENSION_NS = "http://specs.openid.net/extensions/oauth/1.0";

	@Override
	public String getTypeUri() {
		return OAUTH_EXTENSION_NS;
	}

	@Override
	public MessageExtension getExtension(ParameterList parameterList,
			boolean isRequest) throws MessageException {
		if (isRequest) {
			return new OAuthRequest(parameterList);
		} else {
			return new OAuthResponse(parameterList);
		}
	}

}
