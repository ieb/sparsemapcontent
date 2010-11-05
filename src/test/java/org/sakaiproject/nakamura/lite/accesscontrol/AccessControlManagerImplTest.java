package org.sakaiproject.nakamura.lite.accesscontrol;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AclModification;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.ConfigurationImpl;
import org.sakaiproject.nakamura.lite.Security;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;
import org.sakaiproject.nakamura.lite.storage.mem.MemoryStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessControlManagerImplTest {
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

		AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(
				client, currentUser, configuration);
		AclModification user1 = new AclModification("user1",
				Permissions.CAN_ANYTHING.combine(Permissions.CAN_ANYTHING_ACL)
						.getPermission(), AclModification.Operation.OP_REPLACE);
		AclModification user2 = new AclModification("user2",
				Permissions.CAN_READ.combine(Permissions.CAN_WRITE)
						.combine(Permissions.CAN_DELETE).getPermission(),
				AclModification.Operation.OP_REPLACE);
		AclModification user3 = new AclModification("user3",
				Permissions.CAN_READ.getPermission(),
				AclModification.Operation.OP_REPLACE);
		
		
		accessControlManagerImpl.setAcl(Security.ZONE_AUTHORIZABLES,
				"testpath", new AclModification[] { user1, user2, user3 });

		Map<String, Object> acl = accessControlManagerImpl.getAcl(
				Security.ZONE_AUTHORIZABLES, "testpath");
		Assert.assertEquals(
				Integer.toHexString(Permissions.CAN_ANYTHING.combine(Permissions.CAN_ANYTHING_ACL)
						.getPermission()), Integer.toHexString(StorageClientUtils.toInt(acl.get("user1"))));
		Assert.assertEquals(Permissions.CAN_READ.combine(Permissions.CAN_WRITE)
				.combine(Permissions.CAN_DELETE).getPermission(),
				StorageClientUtils.toInt(acl.get("user2")));
		Assert.assertEquals(Permissions.CAN_READ.getPermission(),
				StorageClientUtils.toInt(acl.get("user3")));
		for (Entry<String, Object> e : acl.entrySet()) {
			LOGGER.info(" ACE {} : {} ", e.getKey(),
					StorageClientUtils.toInt(e.getValue()));
		}
		LOGGER.info("Got ACL {}", acl);

	}

}
