package org.sakaiproject.nakamura.lite.authorizable;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.ConfigurationImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.Authenticator;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.mem.MemoryStorageClient;

public class AuthorizableManagerImplTest {

	
	private StorageClient client;
	private ConfigurationImpl configuration;
	
	@Before
	public void before() throws StorageClientException, AccessDeniedException {
		client = new MemoryStorageClient();
		configuration = new ConfigurationImpl();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		configuration.activate(properties);
		AuthorizableActivator authorizableActivator = new AuthorizableActivator(client, configuration);
		authorizableActivator.setup();
	}

	@Test
	public void testAuthorizableManager() throws StorageClientException, AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("admin", "admin");
		
		AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client, currentUser, configuration);
		
		AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser, client, accessControlManagerImpl);
		
		Assert.assertNotNull(authorizableManager.findAuthorizable(User.ADMIN_USER));
		Assert.assertNotNull(authorizableManager.findAuthorizable(User.ANON_USER));
	}
}
