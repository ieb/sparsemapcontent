package org.sakaiproject.nakamura.lite;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.ConnectionPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Authenticator;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.CacheHolder;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableManagerImpl;
import org.sakaiproject.nakamura.lite.content.ContentManagerImpl;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.StorageClient;

public class SessionImpl implements Session {

    private AccessControlManagerImpl accessControlManager;
    private ContentManagerImpl contentManager;
    private AuthorizableManagerImpl authorizableManager;
    private ConnectionPool connectionPool;
    private User currentUser;
    private Repository repository;
    private Exception closedAt;
    private StorageClient client;
    private Authenticator authenticator;

    public SessionImpl(Repository repository, User currentUser, ConnectionPool connectionPool,
            Configuration configuration, Map<String, CacheHolder> sharedCache)
            throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        this.connectionPool = connectionPool;
        this.currentUser = currentUser;
        this.repository = repository;
        client = connectionPool.openConnection();
        
        accessControlManager = new AccessControlManagerImpl(client, currentUser, configuration,
                sharedCache);
        authorizableManager = new AuthorizableManagerImpl(currentUser, client, configuration,
                accessControlManager);

        contentManager = new ContentManagerImpl(client, accessControlManager, configuration);

        authenticator = new AuthenticatorImpl(client, configuration);
    }

    @Override
    public void logout() throws ConnectionPoolException {
        if (closedAt == null) {
            accessControlManager.close();
            authorizableManager.close();
            contentManager.close();
            connectionPool.closeConnection(client);
            accessControlManager = null;
            authorizableManager = null;
            contentManager = null;
            connectionPool = null;
            authenticator = null;
            closedAt = new Exception("This session was closed at:");
        }
    }

    @Override
    public AccessControlManagerImpl getAccessControlManager() throws StorageClientException {
        check();
        return accessControlManager;
    }

    @Override
    public AuthorizableManagerImpl getAuthorizableManager() throws StorageClientException {
        check();
        return authorizableManager;
    }

    @Override
    public ContentManagerImpl getContentManager() throws StorageClientException {
        check();
        return contentManager;
    }

    @Override
    public Authenticator getAuthenticator() throws StorageClientException {
        check();
        return authenticator;
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

    @Override
    public String getUserId() {
        return currentUser.getId();
    }

    private void check() throws StorageClientException {
        if (closedAt != null) {
            throw new StorageClientException(
                    "Session has been closed, see cause to see where this happend ", closedAt);
        }
    }

}
