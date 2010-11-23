package org.sakaiproject.nakamura.api.lite;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;

public interface Session {

    void logout() throws ConnectionPoolException;

    AccessControlManager getAccessControlManager() throws StorageClientException;

    AuthorizableManager getAuthorizableManager() throws StorageClientException;

    ContentManager getContentManager() throws StorageClientException;

    String getUserId();

}
