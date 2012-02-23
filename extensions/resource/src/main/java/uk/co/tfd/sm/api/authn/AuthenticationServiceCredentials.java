package uk.co.tfd.sm.api.authn;

import java.util.Map;

public interface AuthenticationServiceCredentials {

	String getUserName();

	String getPassword();
	
	Map<String, Object> getProperties();
	
	boolean allowCreate();

}
