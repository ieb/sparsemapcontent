package org.sakaiproject.nakamura.lite.content;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.ConfigurationImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImplTest;
import org.sakaiproject.nakamura.lite.accesscontrol.Authenticator;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;
import org.sakaiproject.nakamura.lite.storage.mem.MemoryStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class ContentManagerTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AccessControlManagerImplTest.class);
	private StorageClient client;
	private ConfigurationImpl configuration;

	@Before
	public void before() throws StorageClientException, AccessDeniedException {
		client = new MemoryStorageClient();
		configuration = new ConfigurationImpl();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("keyspace", "n");
		properties.put("acl-column-family", "ac");
		properties.put("authorizable-column-family", "au");
		configuration.activate(properties);
		AuthorizableActivator authorizableActivator = new AuthorizableActivator(
				client, configuration);
		authorizableActivator.setup();
		LOGGER.info("Setup Complete");
	}

	@Test
	public void test() throws StorageClientException, AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("admin", "admin");

		AccessControlManagerImpl accessControlManager = new AccessControlManagerImpl(
				client, currentUser, configuration);
		
		ContentManager contentManager = new ContentManager(client, accessControlManager);
		contentManager.update(new Content("/", ImmutableMap.of("prop1", (Object)"value1")));
		contentManager.update(new Content("/test", ImmutableMap.of("prop1", (Object)"value2")));
		contentManager.update(new Content("/test/ing", ImmutableMap.of("prop1", (Object)"value3")));
		
		listContent(contentManager.get("/"));
		

		
	}

	private void listContent(Content content) {
		System.err.println(content.getPath());
		String indent = StringUtils.leftPad("", content.getPath().length(),'.');
		for ( Entry<String, Object> e : content.getProperties().entrySet() ) {
			System.err.println(indent+" Key["+e.getKey()+"] Value["+e.getValue()+"]");
		}
		for ( String child : content.listChildPaths()) {
            System.err.println(indent+" Child["+child+"]");
		}
		for ( Content child : content.listChildren()) {
		    listContent(child);
		}
	}

	@SuppressWarnings("unchecked")
	private void listMap(Map<String, Object> map, String spacing) {
		
		for ( Entry<String, Object> e : map.entrySet() ) {
			Object o = e.getValue();
			if ( o instanceof Map ) {
				System.err.println(spacing+" Key["+e.getKey()+"] Value[");
				listMap((Map<String, Object>) o,spacing+"  ");
				System.err.println(spacing+"]");
			} else {
				System.err.println(spacing+" Key["+e.getKey()+"] Value["+StorageClientUtils.toString(o)+"]");
			}
		}
	}
}
