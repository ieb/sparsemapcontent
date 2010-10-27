package org.sakaiproject.nakamura.api.lite.authorizable;

import java.util.Map;



public class User extends Authorizable {

	public static final String ADMIN_USER = "admin";
	public static final String ANON_USER = "anonymous";

	public User( Map<String, Object> userMap) {
		super(userMap);
	}
	
	public boolean isAdmin() {
		return ADMIN_USER.equals(id) || principals.contains(ADMINISTRATORS_GROUP);
	}
	

}
