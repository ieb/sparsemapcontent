package org.sakaiproject.nakamura.lite.authorizable;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.ConfigurationImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.Authenticator;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.mem.MemoryStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class AuthorizableManagerImplTest {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizableManagerImplTest.class);
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
		AuthorizableActivator authorizableActivator = new AuthorizableActivator(client, configuration);
		authorizableActivator.setup();
		LOGGER.info("Setup Complete");
	}

	@Test
	public void testAuthorizableManager() throws StorageClientException, AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("admin", "admin");
		
		AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client, currentUser, configuration);
		
		AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser, client, configuration, accessControlManagerImpl);
		
		Assert.assertNotNull(authorizableManager.findAuthorizable(User.ADMIN_USER));
		Assert.assertNotNull(authorizableManager.findAuthorizable(User.ANON_USER));
	}
	
	
	@Test
	public void testAuthorizableManagerCheckUser() throws StorageClientException, AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("admin", "admin");
		
		AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client, currentUser, configuration);
		
		AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser, client, configuration, accessControlManagerImpl);
		
		Authorizable a = authorizableManager.findAuthorizable(User.ADMIN_USER);
		Authorizable an = authorizableManager.findAuthorizable(User.ANON_USER);
		Assert.assertNotNull(a);
		Assert.assertNotNull(an);
		Assert.assertFalse(a.isGroup());
		Assert.assertFalse(an.isGroup());
		User user = (User) a;
		String[] principals = user.getPrincipals();
		Assert.assertNotNull(principals);
		Assert.assertEquals(0, principals.length);
		Assert.assertTrue(user.isAdmin());

		User anon = (User) an;
		principals = anon.getPrincipals();
		Assert.assertNotNull(principals);
		Assert.assertEquals(0, principals.length);
		Assert.assertFalse(anon.isAdmin());

		
	}

	@Test
	public void testAuthorizableManagerCreateUser() throws StorageClientException, AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("admin", "admin");
		
		AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client, currentUser, configuration);
		
		AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser, client, configuration, accessControlManagerImpl);
		
		Assert.assertTrue(authorizableManager.createAuthorizable("testuser", "Test User", "test", ImmutableMap.of("testkey",(Object)"testvalue", "principals", "administrators;testers")));
		
		Authorizable a = authorizableManager.findAuthorizable("testuser");
		Assert.assertNotNull(a);
		Assert.assertFalse(a.isGroup());
		User user = (User) a;
		String[] principals = user.getPrincipals();
		Assert.assertNotNull(principals);
		LOGGER.info("Principals {} ", Arrays.toString(principals));
		Assert.assertEquals(2, principals.length);
		Assert.assertTrue(user.isAdmin());


		
	}

}
