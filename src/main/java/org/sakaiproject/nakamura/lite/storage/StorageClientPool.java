package org.sakaiproject.nakamura.lite.storage;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.lite.accesscontrol.CacheHolder;

import java.util.Map;

public interface StorageClientPool {

    /**
     * @return the connection bound to this thread, or one that was just opened.
     * @throws ClientPoolException
     */
    StorageClient getClient() throws ClientPoolException;

    Map<String, CacheHolder> getSharedCache();

}
