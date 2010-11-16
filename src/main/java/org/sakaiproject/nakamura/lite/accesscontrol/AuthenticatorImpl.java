package org.sakaiproject.nakamura.lite.accesscontrol;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Authenticator;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatorImpl implements Authenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatorImpl.class);
    private StorageClient client;
    private String keySpace;
    private String authorizableColumnFamily;

    public AuthenticatorImpl(StorageClient client, Configuration configuration) {
        this.client = client;
        this.keySpace = configuration.getKeySpace();
        this.authorizableColumnFamily = configuration.getAuthorizableColumnFamily();
    }

    public User authenticate(String userid, String password) {
        try {
            Map<String, Object> userAuthMap = client
                    .get(keySpace, authorizableColumnFamily, userid);
            if (userAuthMap == null) {
                LOGGER.info("User was not found {}", userid);
                return null;
            }
            String passwordHash = StorageClientUtils.secureHash(password);

            String storedPassword = StorageClientUtils.toString(userAuthMap
                    .get(User.PASSWORD_FIELD));
            if (passwordHash.equals(storedPassword)) {
                return new User(userAuthMap);
            }
            LOGGER.debug("Failed to authentication, passwords did not match");
        } catch (StorageClientException e) {
            LOGGER.debug("Failed To authenticate " + e.getMessage(), e);
        }
        return null;

    }

    public User systemAuthenticate(String userid) {
        try {
            Map<String, Object> userAuthMap = client.get(keySpace, authorizableColumnFamily, userid);
            if (userAuthMap == null) {
                LOGGER.info("User was not found {}", userid);
                return null;
            }
            return new User(userAuthMap);
        } catch (StorageClientException e) {
            LOGGER.debug("Failed To system authenticate user " + e.getMessage(), e);
        }
        return null;
    }
}
