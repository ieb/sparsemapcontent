package org.sakaiproject.nakamura.lite.authorizable;

import java.util.Map;
import java.util.Set;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.Security;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * An Authourizable Manager bound to a user, on creation the user ID specified by the caller is trusted.
 * @author ieb
 *
 */
public class AuthorizableManagerImpl implements AuthorizableManager {

	private static final Set<String> FILTER_ON_UPDATE = ImmutableSet.of( Authorizable.ID_FIELD, Authorizable.PASSWORD_FIELD);
	private static final Set<String> FILTER_ON_CREATE = ImmutableSet.of( Authorizable.ID_FIELD, Authorizable.PASSWORD_FIELD);
	private String currentUserId;
	private StorageClient client;
	private AccessControlManager accessControlManager;
	private String keySpace;
	private String authorizableColumnFamily;
	private User thisUser;
	
	

	public AuthorizableManagerImpl(User currentUser, StorageClient client, AccessControlManager accessControlManager) throws StorageClientException, AccessDeniedException {
		this.currentUserId  = currentUser.getId();
		this.thisUser = currentUser;
		this.client = client;
		this.accessControlManager = accessControlManager;
	}
	
	
	public User getUser() {
		return thisUser;
	}
	
	public Authorizable findAuthorizable(String authorizableId) throws AccessDeniedException, StorageClientException {
		if (!this.currentUserId.equals(authorizableId) ) {
			accessControlManager.check(Security.ZONE_AUTHORIZABLES, authorizableId, Permissions.CAN_READ);
		}
		Map<String, Object> authorizableMap = client.get(keySpace, authorizableColumnFamily, authorizableId);
		if ( authorizableMap ==  null ) {
			return null;
		}
		if (Authorizable.isAGroup(authorizableMap) ) {
			return new Group(authorizableMap);
		} else {
			return new User(authorizableMap);
		}
	}
	
	public void updateAuthorizable(Authorizable authorizable) throws AccessDeniedException, StorageClientException {
		String id = authorizable.getId();
		accessControlManager.check(Security.ZONE_AUTHORIZABLES, id, Permissions.CAN_WRITE);
		Map<String, Object> encodedProperties = StorageClientUtils.getFilteredAndEcodedMap(authorizable.getSafeProperties(), FILTER_ON_UPDATE);
		client.insert(keySpace, authorizableColumnFamily, id, encodedProperties);
	}
	
	public boolean createAuthorizable(String authorizableId, String authorizableName, String password, Map<String, Object> properties) throws AccessDeniedException, StorageClientException {
		if ( Authorizable.isAGroup(properties) ) {
			accessControlManager.check(Security.ZONE_ADMIN, Security.ADMIN_GROUPS, Permissions.CAN_WRITE);			
		} else {
			accessControlManager.check(Security.ZONE_ADMIN, Security.ADMIN_USERS, Permissions.CAN_WRITE);						
		}
		Authorizable a = findAuthorizable(authorizableId);
		if ( a != null ) {
			return false;
		}
		Map<String, Object> encodedProperties = StorageClientUtils.getFilteredAndEcodedMap(properties, FILTER_ON_CREATE);
		encodedProperties.put(Authorizable.ID_FIELD, StorageClientUtils.toBytes(authorizableId));
		encodedProperties.put(Authorizable.NAME_FIELD, StorageClientUtils.toBytes(authorizableName));
		if ( password != null ) {
			encodedProperties.put(Authorizable.PASSWORD_FIELD, StorageClientUtils.toBytes(StorageClientUtils.secureHash(password)));
		} else {
			encodedProperties.put(Authorizable.PASSWORD_FIELD, StorageClientUtils.toBytes(Authorizable.NO_PASSWORD));
		}
		client.insert(keySpace, authorizableColumnFamily, authorizableId, encodedProperties);
		return true;
	}
	
	
	
}
