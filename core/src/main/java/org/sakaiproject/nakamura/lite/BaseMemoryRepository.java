package org.sakaiproject.nakamura.lite;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.storage.mem.MemoryStorageClientPool;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClient;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;
import org.sakaiproject.nakamura.lite.storage.spi.content.BlockContentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Utility class to create an entirely in memory Sparse Repository, useful for
 * testing or bulk internal modifications.
 */
public class BaseMemoryRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMemoryRepository.class);
    private StorageClientPool clientPool;
    private StorageClient client;
    private ConfigurationImpl configuration;
    private RepositoryImpl repository;

    public BaseMemoryRepository() throws StorageClientException, AccessDeniedException,
            ClientPoolException, ClassNotFoundException, IOException {
        configuration = new ConfigurationImpl();
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("keyspace", "n");
        properties.put("acl-column-family", "ac");
        properties.put("authorizable-column-family", "au");
        properties.put("content-column-family", "cn");
        configuration.activate(properties);
        clientPool = getClientPool(configuration);
        client = clientPool.getClient();
        AuthorizableActivator authorizableActivator = new AuthorizableActivator(client,
                configuration);
        authorizableActivator.setup();
        LOGGER.info("Setup Complete");
        repository = new RepositoryImpl();
        repository.configuration = configuration;
        repository.clientPool = clientPool;
        repository.storeListener = new LoggingStorageListener();
        Map<String, Object> repoProperties = ImmutableMap.of("t", (Object) "x");
        repository.activate(repoProperties);

    }

    public void close() {
        client.close();
    }

    protected StorageClientPool getClientPool(Configuration configuration) throws ClassNotFoundException {
        MemoryStorageClientPool cp = new MemoryStorageClientPool();
        cp.activate(ImmutableMap.of("test", (Object) "test",
                BlockContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK, 9,
                Configuration.class.getName(), configuration));
        return cp;
    }

    public RepositoryImpl getRepository() {
        return repository;
    }

}
