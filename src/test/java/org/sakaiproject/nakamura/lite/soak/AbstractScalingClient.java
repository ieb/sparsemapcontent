package org.sakaiproject.nakamura.lite.soak;

import com.google.common.collect.Maps;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.ConfigurationImpl;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;

import java.util.Map;

/**
 * Base class for multithreads soak tests, this class is the thing that is
 * runnable by a thread in a soak test.
 * 
 * @author ieb
 * 
 */
public abstract class AbstractScalingClient implements Runnable {

    protected StorageClientPool clientPool;
    protected StorageClient client;
    protected ConfigurationImpl configuration;

    public AbstractScalingClient(StorageClientPool clientPool) throws ClientPoolException,
            StorageClientException, AccessDeniedException {
        this.clientPool = clientPool;
    }

    public void setup() throws ClientPoolException, StorageClientException, AccessDeniedException {
        client = clientPool.getClient();
        configuration = new ConfigurationImpl();
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("keyspace", "n");
        properties.put("acl-column-family", "ac");
        properties.put("authorizable-column-family", "au");
        configuration.activate(properties);
        AuthorizableActivator authorizableActivator = new AuthorizableActivator(client,
                configuration);
        authorizableActivator.setup();
    }

}
