package org.sakaiproject.nakamura.lite.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.ConfigurationImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.Authenticator;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public abstract class AbstractContentManagerTest {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractContentManagerTest.class);
    private StorageClient client;
    private ConfigurationImpl configuration;
    private ConnectionPool connectionPool;

    @Before
    public void before() throws StorageClientException, AccessDeniedException, ConnectionPoolException {
        connectionPool = getConnectionPool();
        client = connectionPool.openConnection();
        configuration = new ConfigurationImpl();
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
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
    
    protected abstract ConnectionPool getConnectionPool();

    @After
    public void after() throws ConnectionPoolException {
        connectionPool.closeConnection();
    }

    @Test
    public void testCreateContent() throws StorageClientException, AccessDeniedException {
        Authenticator authenticator = new Authenticator(client, configuration);
        User currentUser = authenticator.authenticate("admin", "admin");

        AccessControlManagerImpl accessControlManager = new AccessControlManagerImpl(client,
                currentUser, configuration);

        ContentManager contentManager = new ContentManager(client, accessControlManager, configuration);
        contentManager.update(new Content("/", ImmutableMap.of("prop1", (Object) "value1")));
        contentManager.update(new Content("/test", ImmutableMap.of("prop1", (Object) "value2")));
        contentManager
                .update(new Content("/test/ing", ImmutableMap.of("prop1", (Object) "value3")));

        Content content = contentManager.get("/");
        Assert.assertEquals("/", content.getPath());
        Map<String, Object> p = content.getProperties();
        Assert.assertEquals("value1", StorageClientUtils.toString(p.get("prop1")));
        Iterator<Content> children = content.listChildren().iterator();
        Assert.assertTrue(children.hasNext());
        Content child = children.next();
        Assert.assertFalse(children.hasNext());
        Assert.assertEquals("/test", child.getPath());
        p = child.getProperties();
        Assert.assertEquals("value2", StorageClientUtils.toString(p.get("prop1")));
        children = child.listChildren().iterator();
        Assert.assertTrue(children.hasNext());
        child = children.next();
        Assert.assertFalse(children.hasNext());
        Assert.assertEquals("/test/ing", child.getPath());
        p = child.getProperties();
        Assert.assertEquals("value3", StorageClientUtils.toString(p.get("prop1")));

    }
    
    
    @Test
    public void testDeleteContent() throws StorageClientException, AccessDeniedException {
        Authenticator authenticator = new Authenticator(client, configuration);
        User currentUser = authenticator.authenticate("admin", "admin");

        AccessControlManagerImpl accessControlManager = new AccessControlManagerImpl(client,
                currentUser, configuration);

        ContentManager contentManager = new ContentManager(client, accessControlManager, configuration);
        contentManager.update(new Content("/", ImmutableMap.of("prop1", (Object) "value1")));
        contentManager.update(new Content("/test", ImmutableMap.of("prop1", (Object) "value2")));
        contentManager
                .update(new Content("/test/ing", ImmutableMap.of("prop1", (Object) "value3")));

        Content content = contentManager.get("/");
        Assert.assertEquals("/", content.getPath());
        Map<String, Object> p = content.getProperties();
        Assert.assertEquals("value1", StorageClientUtils.toString(p.get("prop1")));
        Iterator<Content> children = content.listChildren().iterator();
        Assert.assertTrue(children.hasNext());
        Content child = children.next();
        Assert.assertFalse(children.hasNext());
        Assert.assertEquals("/test", child.getPath());
        p = child.getProperties();
        Assert.assertEquals("value2", StorageClientUtils.toString(p.get("prop1")));
        children = child.listChildren().iterator();
        Assert.assertTrue(children.hasNext());
        child = children.next();
        Assert.assertFalse(children.hasNext());
        Assert.assertEquals("/test/ing", child.getPath());
        p = child.getProperties();
        Assert.assertEquals("value3", StorageClientUtils.toString(p.get("prop1")));
        
        
        
        contentManager.delete("/test/ing");
        content = contentManager.get("/test/ing");
        Assert.assertNull(content);

    }


    @Test
    public void testUpdateContent() throws StorageClientException, AccessDeniedException {
        Authenticator authenticator = new Authenticator(client, configuration);
        User currentUser = authenticator.authenticate("admin", "admin");

        AccessControlManagerImpl accessControlManager = new AccessControlManagerImpl(client,
                currentUser, configuration);

        ContentManager contentManager = new ContentManager(client, accessControlManager, configuration);
        contentManager.update(new Content("/", ImmutableMap.of("prop1", (Object) "value1")));
        contentManager.update(new Content("/test", ImmutableMap.of("prop1", (Object) "value2")));
        contentManager
                .update(new Content("/test/ing", ImmutableMap.of("prop1", (Object) "value3")));

        Content content = contentManager.get("/");
        Assert.assertEquals("/", content.getPath());
        Map<String, Object> p = content.getProperties();
        Assert.assertEquals("value1", StorageClientUtils.toString(p.get("prop1")));
        Iterator<Content> children = content.listChildren().iterator();
        Assert.assertTrue(children.hasNext());
        Content child = children.next();
        Assert.assertFalse(children.hasNext());
        Assert.assertEquals("/test", child.getPath());
        p = child.getProperties();
        Assert.assertEquals("value2", StorageClientUtils.toString(p.get("prop1")));
        children = child.listChildren().iterator();
        Assert.assertTrue(children.hasNext());
        child = children.next();
        Assert.assertFalse(children.hasNext());
        Assert.assertEquals("/test/ing", child.getPath());
        p = child.getProperties();
        Assert.assertEquals("value3", StorageClientUtils.toString(p.get("prop1")));

        p = content.getProperties();
        Assert.assertNull(StorageClientUtils.toString(p.get("prop1update")));

        content.setProperty("prop1update", "value4");
        contentManager.update(content);

        content = contentManager.get(content.getPath());
        p = content.getProperties();
        Assert.assertEquals("value4", StorageClientUtils.toString(p.get("prop1update")));

    }

    @Test
    public void testVersionContent() throws StorageClientException, AccessDeniedException {
        Authenticator authenticator = new Authenticator(client, configuration);
        User currentUser = authenticator.authenticate("admin", "admin");

        AccessControlManagerImpl accessControlManager = new AccessControlManagerImpl(client,
                currentUser, configuration);

        ContentManager contentManager = new ContentManager(client, accessControlManager, configuration);
        contentManager.update(new Content("/", ImmutableMap.of("prop1", (Object) "value1")));
        contentManager.update(new Content("/test", ImmutableMap.of("prop1", (Object) "value2")));
        contentManager
                .update(new Content("/test/ing", ImmutableMap.of("prop1", (Object) "value3")));

        Content content = contentManager.get("/");
        Assert.assertEquals("/", content.getPath());
        Map<String, Object> p = content.getProperties();
        Assert.assertEquals("value1", StorageClientUtils.toString(p.get("prop1")));
        Iterator<Content> children = content.listChildren().iterator();
        Assert.assertTrue(children.hasNext());
        Content child = children.next();
        Assert.assertFalse(children.hasNext());
        Assert.assertEquals("/test", child.getPath());
        p = child.getProperties();
        Assert.assertEquals("value2", StorageClientUtils.toString(p.get("prop1")));
        children = child.listChildren().iterator();
        Assert.assertTrue(children.hasNext());
        child = children.next();
        Assert.assertFalse(children.hasNext());
        Assert.assertEquals("/test/ing", child.getPath());
        p = child.getProperties();
        Assert.assertEquals("value3", StorageClientUtils.toString(p.get("prop1")));

        p = content.getProperties();
        Assert.assertNull(StorageClientUtils.toString(p.get("prop1update")));

        // FIXME: add some version list methods, we have no way of testing if
        // this works.
        contentManager.saveVersion("/");

        // must reload after a version save.
        content = contentManager.get("/");

        content.setProperty("prop1update", "value4");
        contentManager.update(content);

        content = contentManager.get("/");
        p = content.getProperties();
        Assert.assertEquals("value4", StorageClientUtils.toString(p.get("prop1update")));

    }

    @Test
    public void testUploadContent() throws StorageClientException, AccessDeniedException {
        Authenticator authenticator = new Authenticator(client, configuration);
        User currentUser = authenticator.authenticate("admin", "admin");

        AccessControlManagerImpl accessControlManager = new AccessControlManagerImpl(client,
                currentUser, configuration);

        ContentManager contentManager = new ContentManager(client, accessControlManager, configuration);
        contentManager.update(new Content("/", ImmutableMap.of("prop1", (Object) "value1")));
        contentManager.update(new Content("/test", ImmutableMap.of("prop1", (Object) "value2")));
        contentManager
                .update(new Content("/test/ing", ImmutableMap.of("prop1", (Object) "value3")));

        Content content = contentManager.get("/");
        Assert.assertEquals("/", content.getPath());
        Map<String, Object> p = content.getProperties();
        Assert.assertEquals("value1", StorageClientUtils.toString(p.get("prop1")));
        Iterator<Content> children = content.listChildren().iterator();
        Assert.assertTrue(children.hasNext());
        Content child = children.next();
        Assert.assertFalse(children.hasNext());
        Assert.assertEquals("/test", child.getPath());
        p = child.getProperties();
        Assert.assertEquals("value2", StorageClientUtils.toString(p.get("prop1")));
        children = child.listChildren().iterator();
        Assert.assertTrue(children.hasNext());
        child = children.next();
        Assert.assertFalse(children.hasNext());
        Assert.assertEquals("/test/ing", child.getPath());
        p = child.getProperties();
        Assert.assertEquals("value3", StorageClientUtils.toString(p.get("prop1")));

        p = content.getProperties();
        Assert.assertNull(StorageClientUtils.toString(p.get("prop1update")));

        // FIXME: add some version list methods, we have no way of testing if
        // this works.
        contentManager.saveVersion("/");
        
        content = contentManager.get("/");

        content.setProperty("prop1update", "value4");
        contentManager.update(content);

        content = contentManager.get(content.getPath());
        p = content.getProperties();
        Assert.assertEquals("value4", StorageClientUtils.toString(p.get("prop1update")));
        
        contentManager.setMaxChunksPerBlockSet(9);

        final byte[] b = new byte[20*1024*1024+1231];
        Random r = new Random();
        r.nextBytes(b);
        try {
            contentManager.update(new Content("/test/ing/testfile.txt", ImmutableMap.of(
                    "testproperty", (Object) "testvalue")));
            long su = System.currentTimeMillis();
            ByteArrayInputStream bais = new ByteArrayInputStream(b);
            contentManager.writeBody("/test/ing/testfile.txt", bais);
            bais.close();
            long eu = System.currentTimeMillis();
            
            InputStream read = contentManager.getInputStream("/test/ing/testfile.txt");
            
            int i = 0;
            int j = read.read();
            Assert.assertNotSame(-1, j);
            while ( j != -1 ) {
                // Assert.assertEquals((int)b[i] & 0xff, j);
                i++;
                j = read.read();
            }
            Assert.assertEquals(b.length,i);
            long ee = System.currentTimeMillis();
            LOGGER.info("Write rate {} MB/s  Read Rate {} MB/s ",(1000*(double)b.length/(1024*1024*(double)(eu-su))),(1000*(double)b.length/(1024*1024*(double)(ee-eu))));
            
            
            // Update content and re-read
            r.nextBytes(b);
            bais = new ByteArrayInputStream(b);
            contentManager.writeBody("/test/ing/testfile.txt", bais);
            
            
            read = contentManager.getInputStream("/test/ing/testfile.txt");
            
            i = 0;
            j = read.read();
            Assert.assertNotSame(-1, j);
            while ( j != -1 ) {
                Assert.assertEquals((int)b[i] & 0xff, j);
                i++;
                if ( (i%100==0) && (i < b.length-20) ) {
                    Assert.assertEquals(10,read.skip(10));
                    i+=10;
                }
                j = read.read();
            }
            Assert.assertEquals(b.length,i);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
