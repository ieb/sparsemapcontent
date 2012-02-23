package uk.co.tfd.sm.authn.openid;

import org.apache.commons.lang.StringUtils;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;

import com.google.common.collect.ImmutableMap;

public class OAuthRequest implements MessageExtension {

    private ParameterList parameterList;

    public OAuthRequest(String realm, String[] feeds) {
        parameterList = new ParameterList(ImmutableMap.of("consumer", realm, "scope", StringUtils.join(feeds, " ")));
    }

    public OAuthRequest(ParameterList parameterList) {
        this.parameterList = parameterList;
    }

    @Override
    public String getTypeUri() {
        return OAuthMessageExtensionFactory.OAUTH_EXTENSION_NS;
    }

    @Override
    public ParameterList getParameters() {
        return parameterList;
    }

    @Override
    public void setParameters(ParameterList params) {
        this.parameterList = params;
    }

    @Override
    public boolean providesIdentifier() {
        return false;
    }

    @Override
    public boolean signRequired() {
        return false;
    }

}
