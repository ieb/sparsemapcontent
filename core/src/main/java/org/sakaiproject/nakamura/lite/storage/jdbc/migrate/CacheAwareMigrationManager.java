package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.CacheHolder;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.lite.CachingManagerImpl;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows cached updates, keeping the supplied shared cache in step with the updates.
 * @author ieb
 *
 */
public class CacheAwareMigrationManager extends CachingManagerImpl {

    public CacheAwareMigrationManager(StorageClient client, Map<String, CacheHolder> sharedCache) {
        super(client, sharedCache);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheAwareMigrationManager.class);

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
    
    public void insert(String keySpace, String columnFamily, String key, Map<String, Object> encodedProperties, boolean probablyNew) throws StorageClientException {
        putCached(keySpace, columnFamily, key, encodedProperties, probablyNew);
    }

}
