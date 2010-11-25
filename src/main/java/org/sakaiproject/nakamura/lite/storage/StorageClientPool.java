package org.sakaiproject.nakamura.lite.storage;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.lite.accesscontrol.CacheHolder;

public interface StorageClientPool {

    /**
     * @return the connection bound to this thread, or one that was just opened.
     * @throws ClientPoolException
     */
    StorageClient getClient() throws ClientPoolException;


    Map<String, CacheHolder> getSharedCache();

}
