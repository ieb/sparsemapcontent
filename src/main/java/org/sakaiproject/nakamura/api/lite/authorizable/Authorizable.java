package org.sakaiproject.nakamura.api.lite.authorizable;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.util.Iterables;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class Authorizable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Authorizable.class);
	
	public static final String PASSWORD_FIELD = "pwd";

	public static final String PRINCIPALS_FIELD = "principals";

	public static final String MEMBERS_FIELD = "members";

	public static final String ID_FIELD = "id";

	public static final String NAME_FIELD = "name";

	public static final String GROUP_FIELD = "group";

	public static final String GROUP_VALUE = "y";

	public static final String ADMINISTRATORS_GROUP = "administrators";

	private static final Set<String> FILTER_PROPERTIES = ImmutableSet.of(PASSWORD_FIELD,
			ID_FIELD);

	private static final Set<String> PRIVATE_PROPERTIES = ImmutableSet
			.of(PASSWORD_FIELD);

	public static final String NO_PASSWORD = "--none--";


	protected Map<String, Object> authorizableMap;
	protected Set<String> principals;

	protected String id;

	protected boolean modified;

	public Authorizable(Map<String, Object> autorizableMap) {
		this.authorizableMap = autorizableMap;
		Object principalsB = authorizableMap.get(PRINCIPALS_FIELD);
		if (principalsB == null) {
			this.principals = Sets.newLinkedHashSet();
		} else {
			this.principals = Sets.newLinkedHashSet(Iterables.of(StringUtils.split(
					StorageClientUtils.toString(principalsB), ';')));
		}
		this.id = StorageClientUtils.toString(authorizableMap
				.get(ID_FIELD));
		modified = false;
	}

	public String[] getPrincipals() {
		return principals.toArray(new String[principals.size()]);
	}

	public String getId() {
		return id;
	}

	public boolean isGroup() {
		return false;
	}

	public Map<String, Object> getSafeProperties() {
		authorizableMap.put(PRINCIPALS_FIELD, StringUtils.join(principals,';'));
		return StorageClientUtils.getFilterMap(authorizableMap,
				FILTER_PROPERTIES);
	}

	public static boolean isAGroup(Map<String, Object> authProperties) {
		LOGGER.info("Group Values is {} {} ",authProperties
				.get(GROUP_FIELD),StorageClientUtils.toString(authProperties
				.get(GROUP_FIELD)));
		return GROUP_VALUE.equals(StorageClientUtils.toString(authProperties
				.get(GROUP_FIELD)));
	}

	public void setProperty(String key, Object value) {
		if (!FILTER_PROPERTIES.contains(key)) {
			Object cv = authorizableMap.get(key);
			if ( !value.equals(cv) ) {
				authorizableMap.put(key, value);
				modified = true;
			}
		
		}
	}
	
	public Object getProperty(String key) {
		if ( !PRIVATE_PROPERTIES.contains(key)) {
			return authorizableMap.get(key);
		}
		return null;
	}
	

	public void removeProperty(String key) {
		if ( authorizableMap.containsKey(key)) {
			authorizableMap.remove(key);
			modified = true;
		}
	}



	public void addPrincipal(String principal) {
		if (!principals.contains(principal)) {
			principals.add(principal);
			modified = true;
		}
	}

	public void removePrincipal(String principal) {
		if ( principals.contains(principal) ) {
			principals.remove(principal);
			modified = true;
		}
	}

	public Map<String, Object> getPropertiesForUpdate() {
		authorizableMap.put(PRINCIPALS_FIELD, StringUtils.join(principals,';'));
		return StorageClientUtils.getFilterMap(authorizableMap,
				FILTER_PROPERTIES);
	}

	public void reset() {
		modified = false;
	}

	public boolean isModified() {
		return modified;
	}

}
