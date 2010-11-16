package org.sakaiproject.nakamura.lite;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;

@Component(immediate=true, metatype=true)
@Service(value=Repository.class)
public class RepositoryImpl implements Repository {
    @Reference
    protected Configuration configuration;
    
    @Reference
    protected ConnectionPool connectionPool;
    
    public RepositoryImpl() {
    }
    
    @Activate
    public void activate(Map<String, Object> properties) throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        StorageClient client = connectionPool.openConnection();
        AuthorizableActivator authorizableActivator = new AuthorizableActivator(
                client, configuration);
        authorizableActivator.setup();
        connectionPool.closeConnection();
    }
    
    
    
    
    public Session login(String username, String password) throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        StorageClient client = connectionPool.openConnection();
        AuthenticatorImpl authenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticatorImpl.systemAuthenticate(username);
        return new SessionImpl(currentUser, connectionPool, configuration);
    }

    public Session login() throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        StorageClient client = connectionPool.openConnection();
        AuthenticatorImpl authenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticatorImpl.systemAuthenticate(User.ANON_USER);
        return new SessionImpl(currentUser, connectionPool, configuration);
    }

    public Session loginAdministrative() throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        StorageClient client = connectionPool.openConnection();
        AuthenticatorImpl authenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticatorImpl.systemAuthenticate(User.ADMIN_USER);
        return new SessionImpl(currentUser, connectionPool, configuration);
    }
    
    
        
    
   
    
}
