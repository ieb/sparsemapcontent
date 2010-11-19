package org.sakaiproject.nakamura.lite.storage;

import java.util.Map;

import org.sakaiproject.nakamura.lite.accesscontrol.CacheHolder;

public interface ConnectionPool {

    /**
     * @return the connection bound to this thread, or one that was just opened.
     * @throws ConnectionPoolException
     */
    StorageClient openConnection() throws ConnectionPoolException;

    /**
     * Closes the connection bound to this thread.
     * 
     * @throws ConnectionPoolException
     */
    void closeConnection() throws ConnectionPoolException;

    Map<String, CacheHolder> getSharedCache();

}
