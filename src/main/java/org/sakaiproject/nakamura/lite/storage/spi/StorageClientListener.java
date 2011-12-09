package org.sakaiproject.nakamura.lite.storage.spi;

import java.util.Map;

/**
 * Things that implement this listen to the StorageClient for changes made to
 * objects. There is normally only one listener registered at a time to the
 * StorageClient instance, and the storage client is not normally shared between
 * threads. The StorageClient listener may need to maintain state between calls
 * and should ensure that it does not generate a memory leak or throw exceptions
 * to any of its method that might get in the way of normal processing.
 * 
 * @author ieb
 * 
 */
public interface StorageClientListener {

    /**
     * Notification the key was staged for deletion.
     * 
     * @param keySpace
     * @param columnFamily
     * @param key
     */
    void delete(String keySpace, String columnFamily, String key);

    /**
     * Notification of the state of the map after it has been updated.
     * 
     * @param keySpace
     * @param columnFamily
     * @param key
     * @param mapAfter
     */
    void after(String keySpace, String columnFamily, String key, Map<String, Object> mapAfter);

    /**
     * Notification of the state of the map before being updated.
     * 
     * @param keySpace
     * @param columnFamily
     * @param key
     * @param mapBefore
     */
    void before(String keySpace, String columnFamily, String key, Map<String, Object> mapBefore);

    /**
     * The whole transaction since the last begin() has been committed.
     */
    void commit();

    /**
     * A new transaction has been started.
     */
    void begin();

    /**
     * The whole transaction has been rolled back, almost certainly due to a
     * problem with the data.
     */
    void rollback();

}
