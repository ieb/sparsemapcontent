/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.api.lite.authorizable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.util.Iterables;
import org.sakaiproject.nakamura.lite.authorizable.GroupInternal;

import java.util.Map;
import java.util.Set;

/**
 * A group has a list of members that is maintaiend in the group. This is
 * reflected as principals in each member, managed by the AuthorizableManager,
 * only updated on save.
 * 
 * @author ieb
 * 
 */
public class Group extends Authorizable {

    /**
     * The ID of the everyone group. Includes all users except anon.
     */
    public static final String EVERYONE = "everyone";
    public static final Group EVERYONE_GROUP = new GroupInternal(ImmutableMap.of("id",StorageClientUtils.toStore(EVERYONE)), false, false);
    private Set<String> members;
    private Set<String> membersAdded;
    private Set<String> membersRemoved;
    private boolean membersModified;

    public Group(Map<String, Object> groupMap) {
        super(groupMap);
        this.members = Sets.newLinkedHashSet(Iterables.of(StringUtils.split(
                StorageClientUtils.toString(authorizableMap.get(MEMBERS_FIELD)), ';')));
        this.membersAdded = Sets.newHashSet();
        this.membersRemoved = Sets.newHashSet();
        membersModified = true;
    }

    @Override
    public Map<String, Object> getPropertiesForUpdate() {
        if ( membersModified ) {
            authorizableMap.put(MEMBERS_FIELD, StringUtils.join(members, ';'));
            propertiesModified.add(MEMBERS_FIELD);
        }
        return super.getPropertiesForUpdate();
    }
    
    @Override
    // TODO: Unit test
    public Map<String, Object> getSafeProperties() {
        if ( membersModified ) {
            authorizableMap.put(MEMBERS_FIELD, StringUtils.join(members, ';'));
            propertiesModified.add(MEMBERS_FIELD);
        }
        return super.getSafeProperties();
    }
    
    @Override
    // TODO: Unit test
    public boolean isModified() {
        return membersModified || super.isModified();
    }

    public String[] getMembers() {
        return members.toArray(new String[members.size()]);
    }

    public void addMember(String member) {
        if (!members.contains(member)) {
            members.add(member);
            membersAdded.add(member);
            membersRemoved.remove(member);
            membersModified = true;
        }
    }

    public void removeMember(String member) {
        if (members.contains(member)) {
            members.remove(member);
            membersAdded.remove(member);
            membersRemoved.add(member);
            membersModified = true;
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
        membersModified = false;
    }

}
