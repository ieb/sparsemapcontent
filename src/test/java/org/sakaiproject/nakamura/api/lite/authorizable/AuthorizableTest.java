package org.sakaiproject.nakamura.api.lite.authorizable;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AuthorizableTest {
	
	protected Authorizable u;
	
	@Before
	public void setup(){
		u = new User(new HashMap<String, Object>()); 
	}

	/**
	 * A non-anonymous user has the EVERYONE principal.
	 */
	@Test 
	public void testInitEmpty(){
		Assert.assertEquals(1, u.getPrincipals().length);
		Assert.assertEquals(Group.EVERYONE, u.getPrincipals()[0]);
	}
	
	/**
	 * The anonymous user has no principals.
	 */
	@Test 
	public void testInitAnonymous(){
		Map<String,Object> props = new HashMap<String, Object>();
		props.put(Authorizable.ID_FIELD, User.ANON_USER);
		Authorizable a = new User(props);
		Assert.assertEquals(0, a.getPrincipals().length);
	}
	
	@Test 
	public void testInitPrincipals(){
		Map<String,Object> props = new HashMap<String, Object>();
		props.put(Authorizable.PRINCIPALS_FIELD, "principal1-managers;principal2");
		Authorizable a = new User(props);
		// principal1-managers, principal2, Group.EVERYONE
		Assert.assertEquals(3, a.getPrincipals().length);
	}
	
	@Test
	public void testHasProperty(){
		Assert.assertFalse(u.hasProperty("anykey"));
		u.setProperty("anykey", "where's the any key?");
		Assert.assertTrue(u.hasProperty("anykey"));
	}

	/**
	 * Reset the {@link Authorizable} properties with new ones.
	 * Old properties should no longer be present.
	 */
	@Test
	public void testReset(){
		u.setProperty("anykey", "where's the any key?");

		Map<String, Object> newProps = new HashMap<String, Object>();
		newProps.put("tab", "No time for that the computer's starting!");
		u.reset(newProps);

		Assert.assertFalse(u.hasProperty("anykey"));
		Assert.assertTrue(u.hasProperty("tab"));
		Assert.assertEquals(0, u.modifiedMap.size());
	}

	/**
	 * Reset the {@link Authorizable} properties with an empty {@link HashMap}.
	 * No properties should be present.
	 */
	@Test
	public void testResetEmpty(){
		u.setProperty("anykey", "where's the any key?");
		u.reset(new HashMap<String, Object>());
		Assert.assertFalse(u.hasProperty("anykey"));
		Assert.assertEquals(0, u.modifiedMap.size());
	}
	
	@Test
	public void testRemoveProperty(){
		Authorizable u = new User(new HashMap<String, Object>());
		u.setProperty("anykey", "where's the any key?");
		Assert.assertTrue(u.hasProperty("anykey"));
		u.removeProperty("anykey");
		Assert.assertFalse(u.hasProperty("anykey"));
	}
}
