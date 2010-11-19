package org.sakaiproject.nakamura.lite;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.ConnectionPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Authenticator;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.CacheHolder;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.StorageClient;

@Component(immediate=true, metatype=true)
@Service(value=Repository.class)
public class RepositoryImpl implements Repository {
    @Reference
    protected Configuration configuration;
    
    @Reference
    protected ConnectionPool connectionPool;

    private Map<String, CacheHolder> sharedCache;
    
    public RepositoryImpl() {
    }
    
    @Activate
    public void activate(Map<String, Object> properties) throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        StorageClient client = connectionPool.openConnection();
        AuthorizableActivator authorizableActivator = new AuthorizableActivator(
                client, configuration);
        authorizableActivator.setup();
        connectionPool.closeConnection();
        sharedCache = connectionPool.getSharedCache();
    }
    
    
    
    
    public Session login(String username, String password) throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        StorageClient client = connectionPool.openConnection();
        AuthenticatorImpl authenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticatorImpl.systemAuthenticate(username);
        if ( currentUser == null ) {
            throw new StorageClientException("User "+username+" does not exist, cant login as this user");
        }
        return new SessionImpl(currentUser, connectionPool, configuration, sharedCache);
    }

    public Session login() throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        StorageClient client = connectionPool.openConnection();
        AuthenticatorImpl authenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticatorImpl.systemAuthenticate(User.ANON_USER);
        if ( currentUser == null ) {
            throw new StorageClientException("User "+User.ANON_USER+" does not exist, cant login as this user");
        }
        return new SessionImpl(currentUser, connectionPool, configuration, sharedCache);
    }

    public Session loginAdministrative() throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        StorageClient client = connectionPool.openConnection();
        AuthenticatorImpl authenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticatorImpl.systemAuthenticate(User.ADMIN_USER);
        if ( currentUser == null ) {
            throw new StorageClientException("User "+User.ADMIN_USER+" does not exist, cant login asministratively as this user");
        }
        return new SessionImpl(currentUser, connectionPool, configuration, sharedCache);
    }
    
    public Session loginAdministrative(String username) throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        StorageClient client = connectionPool.openConnection();
        AuthenticatorImpl authenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticatorImpl.systemAuthenticate(username);
        if ( currentUser == null ) {
            throw new StorageClientException("User "+username+" does not exist, cant login asministratively as this user");
        }
        return new SessionImpl(currentUser, connectionPool, configuration, sharedCache);
    }

    
    public Authenticator getAuthenticator() throws ConnectionPoolException {
        StorageClient client = connectionPool.openConnection();
        return new AuthenticatorImpl(client, configuration);
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
    
    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }
   
    
}
