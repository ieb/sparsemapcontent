package org.sakaiproject.nakamura.lite;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.mem.MemoryStorageClientPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class RepositoryImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryImplTest.class);
    private StorageClientPool clientPool;
    private StorageClient client;
    private ConfigurationImpl configuration;

    @Before
    public void before() throws StorageClientException, AccessDeniedException,
            ClientPoolException, ClassNotFoundException {
        clientPool = getClientPool();
        client = clientPool.getClient();
        configuration = new ConfigurationImpl();
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("keyspace", "n");
        properties.put("acl-column-family", "ac");
        properties.put("authorizable-column-family", "au");
        properties.put("content-column-family", "cn");
        configuration.activate(properties);
        AuthorizableActivator authorizableActivator = new AuthorizableActivator(client,
                configuration);
        authorizableActivator.setup();
        LOGGER.info("Setup Complete");
    }
    
    @After
    public void after() {
        client.close();
    }

    @Test
    public void testStart() throws ClientPoolException, StorageClientException, AccessDeniedException {
        RepositoryImpl repository = new RepositoryImpl();
        repository.configuration = configuration;
        repository.clientPool = clientPool;
        Map<String, Object> properties = ImmutableMap.of("t",(Object)"x");
        repository.activate(properties);
        
        Session session = repository.loginAdministrative();
        Assert.assertEquals(User.ADMIN_USER, session.getUserId());
        AuthorizableManager am = session.getAuthorizableManager();
        am.delete("testuser");
        am.createUser("testuser", "Test User", "test", ImmutableMap.of("UserName", (Object)"User Name"));
        session.logout();
        
        session = repository.login("testuser", "test");
        Assert.assertEquals("testuser", session.getUserId());
        Assert.assertNotNull(session.getAccessControlManager());
        Assert.assertNotNull(session.getAuthorizableManager());
        Assert.assertNotNull(session.getContentManager());
        session.logout();
        
        
        session = repository.login();
        Assert.assertEquals(User.ANON_USER, session.getUserId());
        Assert.assertNotNull(session.getAccessControlManager());
        Assert.assertNotNull(session.getAuthorizableManager());
        Assert.assertNotNull(session.getContentManager());
        session.logout();

    }
    
    protected StorageClientPool getClientPool() throws ClassNotFoundException {
        MemoryStorageClientPool cp = new MemoryStorageClientPool();
        cp.activate(ImmutableMap.of("test", (Object) "test",
                BlockContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK, 9));
        return cp;
    }


}
