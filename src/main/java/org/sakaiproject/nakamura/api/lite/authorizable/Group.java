package org.sakaiproject.nakamura.api.lite.authorizable;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.util.Iterables;

import com.google.common.collect.Sets;

/**
 * A group has a list of members that is maintaiend in the group. This is
 * reflected as principals in each member, managed by the AuthorizableManager,
 * only updated on save.
 * 
 * @author ieb
 * 
 */
public class Group extends Authorizable {

    private Set<String> members;
    private Set<String> membersAdded;
    private Set<String> membersRemoved;

    public Group(Map<String, Object> groupMap) {
        super(groupMap);
        this.members = Sets.newLinkedHashSet(Iterables.of(StringUtils.split(
                StorageClientUtils.toString(authorizableMap.get(MEMBERS_FIELD)), ';')));
        this.membersAdded = Sets.newHashSet();
        this.membersRemoved = Sets.newHashSet();
    }

    @Override
    public Map<String, Object> getPropertiesForUpdate() {
        authorizableMap.put(MEMBERS_FIELD, StringUtils.join(members, ';'));
        return super.getPropertiesForUpdate();
    }

    public String[] getMembers() {
        return members.toArray(new String[members.size()]);
    }

    public void addMember(String member) {
        if (!members.contains(member)) {
            members.add(member);
            membersAdded.add(member);
            membersRemoved.remove(member);
            modified = true;
        }
    }

    public void removeMember(String member) {
        if (members.contains(member)) {
            members.remove(member);
            membersAdded.remove(member);
            membersRemoved.add(member);
            modified = true;
        }
    }

    public String[] getMembersAdded() {
        return membersAdded.toArray(new String[membersAdded.size()]);
    }

    public String[] getMembersRemoved() {
        return membersRemoved.toArray(new String[membersRemoved.size()]);
    }

    public void reset() {
        super.reset();
        membersAdded.clear();
        membersRemoved.clear();
    }

}
