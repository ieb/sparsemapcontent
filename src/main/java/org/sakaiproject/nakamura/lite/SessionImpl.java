package org.sakaiproject.nakamura.lite;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Configuration;
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
import org.sakaiproject.nakamura.lite.storage.StorageClient;

import java.util.Map;

public class SessionImpl implements Session {

    private AccessControlManagerImpl accessControlManager;
    private ContentManagerImpl contentManager;
    private AuthorizableManagerImpl authorizableManager;
    private User currentUser;
    private Repository repository;
    private Exception closedAt;
    private StorageClient client;
    private Authenticator authenticator;

    public SessionImpl(Repository repository, User currentUser, StorageClient client,
            Configuration configuration, Map<String, CacheHolder> sharedCache)
            throws ClientPoolException, StorageClientException, AccessDeniedException {
        this.currentUser = currentUser;
        this.repository = repository;
        this.client = client;
        accessControlManager = new AccessControlManagerImpl(client, currentUser, configuration,
                sharedCache);
        authorizableManager = new AuthorizableManagerImpl(currentUser, client, configuration,
                accessControlManager, sharedCache);

        contentManager = new ContentManagerImpl(client, accessControlManager, configuration);

        authenticator = new AuthenticatorImpl(client, configuration);
    }

    public void logout() throws ClientPoolException {
        if (closedAt == null) {
            accessControlManager.close();
            authorizableManager.close();
            contentManager.close();
            client.close();
            accessControlManager = null;
            authorizableManager = null;
            contentManager = null;
            client = null;
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
