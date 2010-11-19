package org.sakaiproject.nakamura.lite.authorizable;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class AuthorizableActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizableActivator.class);
    private StorageClient client;
    private String keySpace;
    private String authorizableColumnFamily;

    public AuthorizableActivator(StorageClient client, Configuration configuration)
            throws StorageClientException, AccessDeniedException {
        this.client = client;
        this.authorizableColumnFamily = configuration.getAuthorizableColumnFamily();
        this.keySpace = configuration.getKeySpace();
    }

    public synchronized void setup() throws StorageClientException {
        createAdminUser();
        createAnonUser();
        createSystemUser();
        createAdministratorsGroup();
    }

    private void createAdministratorsGroup() throws StorageClientException {
        Map<String, Object> authorizableMap = client.get(keySpace, authorizableColumnFamily,
                Authorizable.ADMINISTRATORS_GROUP);
        if (authorizableMap == null || authorizableMap.size() == 0) {
            Map<String, Object> group = ImmutableMap.of(Authorizable.ID_FIELD,
                    StorageClientUtils.toStore(Authorizable.ADMINISTRATORS_GROUP), Authorizable.NAME_FIELD,
                    StorageClientUtils.toStore(Authorizable.ADMINISTRATORS_GROUP));
            LOGGER.info("Creating System User user as {} with {} ", Authorizable.ADMINISTRATORS_GROUP, group);
            client.insert(keySpace, authorizableColumnFamily, Authorizable.ADMINISTRATORS_GROUP, group);
        } else {
            LOGGER.info("System User user exists as {} with {} ", Authorizable.ADMINISTRATORS_GROUP, authorizableMap);
            
        }
    }

    private void createSystemUser() throws StorageClientException {
        Map<String, Object> authorizableMap = client.get(keySpace, authorizableColumnFamily,
                User.SYSTEM_USER);
        if (authorizableMap == null || authorizableMap.size() == 0) {
            Map<String, Object> user = ImmutableMap.of(Authorizable.ID_FIELD,
                    StorageClientUtils.toStore(User.SYSTEM_USER), Authorizable.NAME_FIELD,
                    StorageClientUtils.toStore(User.SYSTEM_USER), Authorizable.PASSWORD_FIELD,
                    StorageClientUtils.toStore("--no-password--"));
            LOGGER.info("Creating System User user as {} with {} ", User.SYSTEM_USER, user);
            client.insert(keySpace, authorizableColumnFamily, User.SYSTEM_USER, user);
        } else {
            LOGGER.info("System User user exists as {} with {} ", User.SYSTEM_USER, authorizableMap);
            
        }
    }

    private void createAdminUser() throws StorageClientException {
        Map<String, Object> authorizableMap = client.get(keySpace, authorizableColumnFamily,
                User.ADMIN_USER);
        if (authorizableMap == null || authorizableMap.size() == 0) {
            Map<String, Object> user = ImmutableMap.of(Authorizable.ID_FIELD,
                    StorageClientUtils.toStore(User.ADMIN_USER), Authorizable.NAME_FIELD,
                    StorageClientUtils.toStore(User.ADMIN_USER), Authorizable.PASSWORD_FIELD,
                    StorageClientUtils.toStore(StorageClientUtils.secureHash("admin")));
            LOGGER.info("Creating Admin User user as {} with {} ", User.ADMIN_USER, user);
            client.insert(keySpace, authorizableColumnFamily, User.ADMIN_USER, user);
        } else {
            LOGGER.info("Admin User user exists as {} with {} ", User.ADMIN_USER, authorizableMap);   
        }
    }

    private void createAnonUser() throws StorageClientException {
        Map<String, Object> authorizableMap = client.get(keySpace, authorizableColumnFamily,
                User.ANON_USER);
        if (authorizableMap == null || authorizableMap.size() == 0) {
            Map<String, Object> user = ImmutableMap.of(Authorizable.ID_FIELD,
                    StorageClientUtils.toStore(User.ANON_USER), Authorizable.NAME_FIELD,
                    StorageClientUtils.toStore(User.ANON_USER), Authorizable.PASSWORD_FIELD,
                    StorageClientUtils.toStore(Authorizable.NO_PASSWORD));
            LOGGER.info("Creating Anon user as {} with {} ", User.ANON_USER, user);
            client.insert(keySpace, authorizableColumnFamily, User.ANON_USER, user);
        } else {
            LOGGER.info("Anon User user exists as {} with {} ", User.ANON_USER, authorizableMap);   
        }
    }

}
