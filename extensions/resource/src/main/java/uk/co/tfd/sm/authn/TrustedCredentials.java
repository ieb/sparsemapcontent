package uk.co.tfd.sm.authn;

import java.util.Map;

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

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }

    @Override
    public boolean allowCreate() {
        return false;
    }

}
