package org.sakaiproject.nakamura.api.lite;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Authenticator;

public interface Repository {

    Session login(String username, String password) throws ConnectionPoolException,
            StorageClientException, AccessDeniedException;

    Session login() throws ConnectionPoolException, StorageClientException, AccessDeniedException;

    Session loginAdministrative() throws ConnectionPoolException, StorageClientException,
            AccessDeniedException;

    Session loginAdministrative(String username) throws ConnectionPoolException, StorageClientException, AccessDeniedException;

    Authenticator getAuthenticator() throws ConnectionPoolException;

    void logout() throws ConnectionPoolException;
}
