package org.sakaiproject.nakamura.api.lite.authorizable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

public class AuthorizableTest {
	
	protected Authorizable u;
	
	@Before
	public void setup() throws StorageClientException, AccessDeniedException{
		// u is an empty, non-anonymous, user at the beginning of each test.
		u = new User(new HashMap<String, Object>()); 
	}

	// --- Init

	@Test 
	public void testInitEmpty(){
		assertEquals(1, u.getPrincipals().length);
		// A non-anonymous user has the EVERYONE principal.
		assertEquals(Group.EVERYONE, u.getPrincipals()[0]);
	}

	@Test 
	public void testInitAnonymous() throws StorageClientException, AccessDeniedException {
		Map<String,Object> props = new HashMap<String, Object>();
		props.put(Authorizable.ID_FIELD, User.ANON_USER);
		Authorizable a = new User(props);
		// The anonymous user has no principals.
		assertEquals(0, a.getPrincipals().length);
	}

	@Test 
	public void testInitPrincipals() throws StorageClientException, AccessDeniedException {
		Map<String,Object> props = new HashMap<String, Object>();
		props.put(Authorizable.PRINCIPALS_FIELD, "principal1-managers;principal2");
		Authorizable a = new User(props);
		// principal1-managers, principal2, Group.EVERYONE
		assertEquals(3, a.getPrincipals().length);
	}

	// --- Properties, get, set, has, remove
	
	@Test
	public void testGetProperty(){
		assertNull(u.getProperty("somekey"));
		u.setProperty("somekey", "found");
		assertEquals("found", u.getProperty("somekey"));
	}

	@Test
	public void testGetPrivateProperty(){
		u.setProperty(Authorizable.PASSWORD_FIELD, "testpass");
		assertNull(u.getProperty(Authorizable.PASSWORD_FIELD));
	}

	@Test
	public void testHasProperty(){
		assertFalse(u.hasProperty("anykey"));
		u.setProperty("anykey", "where's the any key?");
		assertTrue(u.hasProperty("anykey"));
	}

	@Test
	public void testRemoveProperty(){
		u.setProperty("anykey", "where's the any key?");
		assertTrue(u.hasProperty("anykey"));
		u.removeProperty("anykey");
		assertFalse(u.hasProperty("anykey"));
		assertNull(u.getProperty("anykey"));
	}

	@Test
	public void testSetOverrideProperty(){
		u.setProperty("anykey", "value1");
		u.setProperty("anykey", "value2");
		assertEquals("value2", u.getProperty("anykey"));
	}

	// --- Modified

	@Test
	public void isModified(){
		u.setProperty("anykey", "value1");
		assertTrue(u.isModified());
		assertTrue(u.modifiedMap.size() > 0);

		u.reset((Map<String,Object>)new HashMap<String, Object>());
		assertFalse(u.isModified());

		u.addPrincipal("first");
		assertTrue(u.isModified());
	}

	// --- Principals

	@Test
	public void testAddPrincipal(){
		assertEquals(1, u.principals.size());
		u.addPrincipal("first");
		u.addPrincipal("second");
		assertEquals(3, u.principals.size());
		assertTrue(u.principalsModified);
	}

	@Test
	public void testRemovePrincipal(){
		assertEquals(1, u.principals.size());
		u.removePrincipal(Group.EVERYONE);
		assertEquals(0, u.principals.size());
		assertTrue(u.principalsModified);
	}

	// --- Reset

	@Test
	public void testReset(){
		u.setProperty("anykey", "where's the any key?");

		Map<String, Object> newProps = new HashMap<String, Object>();
		newProps.put("newkey", "No time for that the computer's starting!");
		// Reset the properties with new ones.
		u.reset(newProps); 

		// Old properties should no longer be present.
		assertFalse(u.hasProperty("anykey"));
		assertTrue(u.hasProperty("newkey"));
		assertEquals(0, u.modifiedMap.size());
	}

	@Test
	public void testResetEmpty(){
		u.setProperty("anykey", "where's the any key?");
		// Reset the properties with an empty Map.
		u.reset(new HashMap<String, Object>());
		assertFalse(u.hasProperty("anykey"));
		// No properties should be present.
		assertEquals(0, u.modifiedMap.size());
	}
}
