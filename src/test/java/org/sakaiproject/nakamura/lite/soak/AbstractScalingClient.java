package org.sakaiproject.nakamura.lite.soak;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.ConfigurationImpl;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;

import com.google.common.collect.Maps;

/**
 * Base class for multithreads soak tests, this class is the thing that is runnable by a thread in a soak test.
 * @author ieb
 *
 */
public abstract class AbstractScalingClient implements Runnable  {

   
    protected ConnectionPool connectionPool;
    protected StorageClient client;
    protected ConfigurationImpl configuration;

    public AbstractScalingClient(ConnectionPool connectionPool) throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        this.connectionPool = connectionPool;        
    }
    
    public void setup() throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        client = connectionPool.openConnection();
        configuration = new ConfigurationImpl();
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("keyspace", "n");
        properties.put("acl-column-family", "ac");
        properties.put("authorizable-column-family", "au");
        configuration.activate(properties);
        AuthorizableActivator authorizableActivator = new AuthorizableActivator(
                client, configuration);
        authorizableActivator.setup();
    }
    
    
      
}
