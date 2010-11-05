package org.sakaiproject.nakamura.lite.authorizable;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
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

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AuthorizableManagerImplTest.class);
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
	public void testAuthorizableManager() throws StorageClientException,
			AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("admin", "admin");

		AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(
				client, currentUser, configuration);

		AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(
				currentUser, client, configuration, accessControlManagerImpl);

		Assert.assertNotNull(authorizableManager
				.findAuthorizable(User.ADMIN_USER));
		Assert.assertNotNull(authorizableManager
				.findAuthorizable(User.ANON_USER));
		Assert.assertEquals(currentUser, authorizableManager.getUser());
	}

	@Test
	public void testAuthorizableManagerAccessDenied() throws StorageClientException,
			AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("admin", "wrong-password");
		
		Assert.assertNull(currentUser);
	}

	@Test
	public void testAuthorizableManagerUserNotFound() throws StorageClientException,
			AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("nonuser", "wrong-password");
		
		Assert.assertNull(currentUser);
	}

	@Test
	public void testAuthorizableManagerCheckUser()
			throws StorageClientException, AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("admin", "admin");

		AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(
				client, currentUser, configuration);

		AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(
				currentUser, client, configuration, accessControlManagerImpl);

		Authorizable a = authorizableManager.findAuthorizable(User.ADMIN_USER);
		Authorizable an = authorizableManager.findAuthorizable(User.ANON_USER);
		Authorizable missing = authorizableManager
				.findAuthorizable("missinguser");
		Assert.assertNull(missing);
		Assert.assertNotNull(a);
		Assert.assertNotNull(an);
		Assert.assertFalse(a instanceof Group);
		Assert.assertFalse(an instanceof Group);
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
	public void testAuthorizableManagerCreateUser()
			throws StorageClientException, AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("admin", "admin");

		AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(
				client, currentUser, configuration);

		AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(
				currentUser, client, configuration, accessControlManagerImpl);

		Assert.assertTrue(authorizableManager.createUser("testuser",
				"Test User", "test", ImmutableMap.of("testkey",
						(Object) "testvalue", "principals",
						"administrators;testers", Authorizable.GROUP_FIELD,
						Authorizable.GROUP_VALUE)));
		Assert.assertFalse(authorizableManager.createUser("testuser",
				"Test User", "test", ImmutableMap.of("testkey",
						(Object) "testvalue", "principals",
						"administrators;testers")));

		Authorizable a = authorizableManager.findAuthorizable("testuser");
		Assert.assertNotNull(a);
		Assert.assertFalse(a instanceof Group);
		User user = (User) a;
		String[] principals = user.getPrincipals();
		Assert.assertNotNull(principals);
		LOGGER.info("Principals {} ", Arrays.toString(principals));
		Assert.assertEquals(2, principals.length);
		Assert.assertTrue(user.isAdmin());

	}

	@Test
	public void testAuthorizableManagerCreateUserDenied()
			throws StorageClientException, AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("admin", "admin");

		AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(
				client, currentUser, configuration);

		AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(
				currentUser, client, configuration, accessControlManagerImpl);

		Assert.assertTrue(authorizableManager.createUser("testuser",
				"Test User", "test", ImmutableMap.of("testkey",
						(Object) "testvalue", "principals", "testers",
						Authorizable.GROUP_FIELD, Authorizable.GROUP_VALUE)));
		Assert.assertFalse(authorizableManager.createUser("testuser",
				"Test User", "test", ImmutableMap.of("testkey",
						(Object) "testvalue", "principals",
						"administrators;testers")));

		Authorizable a = authorizableManager.findAuthorizable("testuser");
		Assert.assertNotNull(a);
		Assert.assertFalse(a instanceof Group);
		User user = (User) a;
		String[] principals = user.getPrincipals();
		LOGGER.info("Principals {} ", Arrays.toString(principals));
		Assert.assertArrayEquals(new String[] { "testers" },
				principals);

		Assert.assertFalse(user.isAdmin());

		AccessControlManagerImpl userAccessControlManagerImpl = new AccessControlManagerImpl(
				client, user, configuration);
		AuthorizableManagerImpl userAuthorizableManager = new AuthorizableManagerImpl(
				user, client, configuration, userAccessControlManagerImpl);

		try {
			userAuthorizableManager
					.createUser("testuser2", "Test User", "test", ImmutableMap
							.of("testkey", (Object) "testvalue", "principals",
									"administrators;testers",
									Authorizable.GROUP_FIELD,
									Authorizable.GROUP_VALUE));
			Assert.fail();
		} catch (AccessDeniedException e) {
			LOGGER.info(" Correctly denied access {} ", e.getMessage());
		}

		try {
			userAuthorizableManager.createUser("testuser", "Test User", "test",
					ImmutableMap.of("testkey", (Object) "testvalue",
							"principals", "administrators;testers"));
			Assert.fail();
		} catch (AccessDeniedException e) {
			LOGGER.info(" Correctly denied access {} ", e.getMessage());
		}

	}

	@Test
	public void testAuthorizableManagerCreateGroup()
			throws StorageClientException, AccessDeniedException {
		Authenticator authenticator = new Authenticator(client, configuration);
		User currentUser = authenticator.authenticate("admin", "admin");

		AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(
				client, currentUser, configuration);

		AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(
				currentUser, client, configuration, accessControlManagerImpl);

		Assert.assertTrue(authorizableManager.createUser("user3", "TestUser",
				null, ImmutableMap.of("testkey", (Object) "testvalue",
						"principals", "administrators;testers", "members",
						"user1;user2")));
		Assert.assertTrue(authorizableManager.createGroup("testgroup",
				"Test Group", ImmutableMap.of("testkey", (Object) "testvalue",
						"principals", "administrators;testers", "members",
						"user1;user2")));
		Assert.assertFalse(authorizableManager.createGroup("testgroup",
				"Test Group", ImmutableMap.of("testkey", (Object) "testvalue",
						"principals", "administrators;testers", "members",
						"user1;user2", Authorizable.GROUP_FIELD,
						Authorizable.GROUP_VALUE)));

		Authorizable a = authorizableManager.findAuthorizable("testgroup");
		Assert.assertNotNull(a);
		Assert.assertTrue(a instanceof Group);
		Group g = (Group) a;
		String[] principals = g.getPrincipals();
		LOGGER.info("Principals {} ", Arrays.toString(principals));
		Assert.assertArrayEquals(new String[] { "administrators", "testers" },
				principals);
		String[] members = g.getMembers();
		LOGGER.info("Members {} ", Arrays.toString(members));
		Assert.assertArrayEquals(new String[] { "user1", "user2" }, members);

		g.setProperty("SomeValue", "AValue");
		g.setProperty(Authorizable.PASSWORD_FIELD, "badpassword");
		g.removeProperty("testkey");
		g.addPrincipal("tester2");
		g.removePrincipal("testers");
		g.addMember("user3");
		g.removeMember("user2");

		authorizableManager.updateAuthorizable(g);

		Authorizable a2 = authorizableManager.findAuthorizable("testgroup");
		Assert.assertNotNull(a2);
		Assert.assertTrue(a2 instanceof Group);
		Group g2 = (Group) a2;
		principals = g2.getPrincipals();
		LOGGER.info("Principals {} ", Arrays.toString(principals));
		Assert.assertArrayEquals(new String[] { "administrators", "tester2" },
				principals);
		members = g2.getMembers();
		LOGGER.info("Members {} ", Arrays.toString(members));
		Assert.assertArrayEquals(new String[] { "user1", "user3" }, members);
		Assert.assertNull(g2.getProperty(Authorizable.PASSWORD_FIELD));

		// Test that User3 no has testgroup as a principal.
		Authorizable a3 = authorizableManager.findAuthorizable("user3");
		Assert.assertNotNull(a3);
		Assert.assertFalse(a3 instanceof Group);
		User u3 = (User) a3;
		principals = u3.getPrincipals();
		LOGGER.info("Principals {} ", Arrays.toString(principals));
		Assert.assertArrayEquals(new String[] { "administrators", "testers",
				"testgroup" }, principals);

	}

}
