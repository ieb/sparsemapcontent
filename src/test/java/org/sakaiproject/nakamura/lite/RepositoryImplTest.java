package org.sakaiproject.nakamura.lite;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.ConnectionPoolException;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.mem.MemoryStorageClientConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class RepositoryImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryImplTest.class);
    private ConnectionPool connectionPool;
    private StorageClient client;
    private ConfigurationImpl configuration;

    @Before
    public void before() throws StorageClientException, AccessDeniedException,
            ConnectionPoolException, ClassNotFoundException {
        connectionPool = getConnectionPool();
        client = connectionPool.openConnection();
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

    @Test
    public void testStart() throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        RepositoryImpl repository = new RepositoryImpl();
        repository.configuration = configuration;
        repository.connectionPool = connectionPool;
        repository.activate(ImmutableMap.of("t",(Object)"x"));
        
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
    
    protected ConnectionPool getConnectionPool() throws ClassNotFoundException {
        MemoryStorageClientConnectionPool cp = new MemoryStorageClientConnectionPool();
        cp.activate(ImmutableMap.of("test", (Object) "test",
                BlockContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK, 9));
        return cp;
    }


}
