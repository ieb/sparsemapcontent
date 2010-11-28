package org.sakaiproject.nakamura.api.lite;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

public interface Repository {

    Session login(String username, String password) throws ClientPoolException,
            StorageClientException, AccessDeniedException;

    Session login() throws ClientPoolException, StorageClientException, AccessDeniedException;

    Session loginAdministrative() throws ClientPoolException, StorageClientException,
            AccessDeniedException;

    Session loginAdministrative(String username) throws ClientPoolException,
            StorageClientException, AccessDeniedException;

}
