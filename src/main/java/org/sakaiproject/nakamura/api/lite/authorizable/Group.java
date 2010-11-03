package org.sakaiproject.nakamura.api.lite.authorizable;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.util.Iterables;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;

import com.google.common.collect.Sets;

public class Group extends Authorizable {

	private Set<String> members;

	public Group(Map<String, Object> groupMap) {
		super(groupMap);
		this.members = Sets.newLinkedHashSet(Iterables.of(StringUtils.split(
				StorageClientUtils.toString((byte[]) authorizableMap
						.get(MEMBERS_FIELD)), ';')));
	}

	@Override
	public boolean isGroup() {
		return true;
	}
	
	@Override
	public Map<String, Object> getPropertiesForUpdate() {
		authorizableMap.put(MEMBERS_FIELD, StringUtils.join(members,';'));
		return super.getPropertiesForUpdate();
	}

	public String[] getMembers() {
		return members.toArray(new String[members.size()]);
	}

	public void addMember(String member) {
		if (!members.contains(member)) {
			members.add(member);
		}
	}

	public void removeMember(String member) {
		members.remove(member);
	}

}
