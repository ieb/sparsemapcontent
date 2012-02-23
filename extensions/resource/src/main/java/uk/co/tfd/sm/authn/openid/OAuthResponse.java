package uk.co.tfd.sm.authn.openid;

import org.apache.commons.lang.StringUtils;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;

public class OAuthResponse implements MessageExtension {

    private ParameterList parameterList;
    private String[] scope;
    private String token;

    public OAuthResponse(ParameterList parameterList) {
        setParameters(parameterList);
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
    public void setParameters(ParameterList parameterList) {
        this.parameterList = parameterList;
        if (parameterList.hasParameter("scope")) {
            scope = StringUtils.split(parameterList.getParameterValue("scope"), " ");
        } else {
            scope = null;
        }
        if (parameterList.hasParameter("request_token")) {
            token = parameterList.getParameterValue("request_token");
        } else {
            token = null;
        }
    }

    @Override
    public boolean providesIdentifier() {
        return false;
    }

    @Override
    public boolean signRequired() {
        return false;
    }

    public String[] getScope() {
        return scope;
    }

    public String getToken() {
        return token;
    }

}
