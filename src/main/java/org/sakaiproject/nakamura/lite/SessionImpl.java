package org.sakaiproject.nakamura.lite;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableManagerImpl;
import org.sakaiproject.nakamura.lite.content.ContentManagerImpl;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;

public class SessionImpl implements Session {

    private AccessControlManagerImpl accessControlManager;
    private ContentManagerImpl contentManager;
    private AuthorizableManagerImpl authorizableManager;
    private ConnectionPool connectionPool;
    private User currentUser;

    public SessionImpl(User currentUser, ConnectionPool connectionPool, Configuration configuration)
            throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        this.connectionPool = connectionPool;
        this.currentUser = currentUser;
        StorageClient client = connectionPool.openConnection();
        accessControlManager = new AccessControlManagerImpl(client, currentUser, configuration);
        authorizableManager = new AuthorizableManagerImpl(currentUser, client, configuration,
                accessControlManager);

        contentManager = new ContentManagerImpl(client, accessControlManager, configuration);
    }

    public void logout() throws ConnectionPoolException {
        accessControlManager.close();
        authorizableManager.close();
        contentManager.close();
        connectionPool.closeConnection();
        accessControlManager = null;
        authorizableManager = null;
        contentManager = null;
        connectionPool = null;
    }

    public AccessControlManagerImpl getAccessControlManager() {
        return accessControlManager;
    }

    public AuthorizableManagerImpl getAuthorizableManager() {
        return authorizableManager;
    }

    public ContentManagerImpl getContentManager() {
        return contentManager;
    }

    @Override
    public String getUserId() {
        return currentUser.getId();
    }

}
