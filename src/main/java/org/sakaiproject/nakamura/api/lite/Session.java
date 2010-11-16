package org.sakaiproject.nakamura.api.lite;

import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableManagerImpl;
import org.sakaiproject.nakamura.lite.content.ContentManagerImpl;
import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;

public interface Session {

    void logout() throws ConnectionPoolException;

    AccessControlManagerImpl getAccessControlManager();

    AuthorizableManagerImpl getAuthorizableManager();

    ContentManagerImpl getContentManager();

    String getUserId();

}
