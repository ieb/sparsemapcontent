package org.sakaiproject.nakamura.api.lite.authorizable;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;

import com.google.common.collect.Sets;

public class Group extends Authorizable {

	
	private Set<String> members;

	public Group(Map<String, Object> groupMap) {
		super(groupMap);
		this.members = Sets.newHashSet(StringUtils.split(StorageClientUtils.toString((byte[]) authorizableMap.get(PRINCIPALS_FIELD)), ';'));
	}
	
	@Override
	public boolean isGroup() {
		return true;
	}
	
	public String[] getMembers() {
		return members.toArray(new String[members.size()]);
	}
	
}
