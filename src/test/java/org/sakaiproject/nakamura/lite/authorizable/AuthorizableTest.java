package org.sakaiproject.nakamura.lite.authorizable;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.User;

public class AuthorizableTest {

	@Test
	public void testHasProperty(){
		Authorizable a = new User(new HashMap<String, Object>());
		Assert.assertFalse(a.hasProperty("anykey"));

		a.setProperty("anykey", "where's the any key?");
		Assert.assertTrue(a.hasProperty("anykey"));
	}

	@Test
	public void testReset(){
		Authorizable a = new User(new HashMap<String, Object>());
		a.setProperty("anykey", "where's the any key?");

		Map<String, Object> newProps = new HashMap<String, Object>();
		newProps.put("tab", "No time for that the computer's starting!");
		a.reset(newProps);

		Assert.assertFalse(a.hasProperty("anykey"));
		Assert.assertTrue(a.hasProperty("tab"));
	}

	@Test
	public void testResetEmpty(){
		Authorizable a = new User(new HashMap<String, Object>());
		a.setProperty("anykey", "where's the any key?");
		a.reset(new HashMap<String, Object>());
		Assert.assertFalse(a.hasProperty("anykey"));
	}
}
