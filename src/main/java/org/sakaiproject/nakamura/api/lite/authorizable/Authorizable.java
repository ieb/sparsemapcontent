package org.sakaiproject.nakamura.api.lite.authorizable;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class Authorizable {

	public static final String PASSWORD_FIELD = "pwd";

	public static final String PRINCIPALS_FIELD = "principals";

	public static final String ID_FIELD = "id";

	public static final String NAME_FIELD = "name";
	
	public static final String GROUP_FIELD = "group";
	
	public static final String ADMINISTRATORS_GROUP = "administrators";
	
	


	private static final Set<String> FILTER_PROPERTIES = ImmutableSet.of("pwd", "id");

	public static final String NO_PASSWORD = "--none--";



	protected Map<String, Object> authorizableMap;
	protected Set<String> principals;

	protected String id;
	

	public Authorizable(Map<String, Object> autorizableMap) {
		this.authorizableMap = autorizableMap;
		this.principals = Sets.newHashSet(StringUtils.split(StorageClientUtils.toString((byte[]) authorizableMap.get(PRINCIPALS_FIELD)), ';'));
		this.id = StorageClientUtils.toString((byte[]) authorizableMap.get(ID_FIELD));
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

	public Map<String,Object> getSafeProperties() {
		return StorageClientUtils.getFilterMap(authorizableMap, FILTER_PROPERTIES);
	}
	
	public static boolean isAGroup(Map<String,Object> authProperties) {
		return "Y".equals(authProperties.get(GROUP_FIELD));
	}
	
}
