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
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.util.Iterables;
import org.sakaiproject.nakamura.lite.authorizable.GroupInternal;

import java.util.Map;
import java.util.Set;

/**
 * A group has a list of members that is maintained in the group. This is
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
    public static final Group EVERYONE_GROUP = getEveryone();
    private Set<String> members;
    private Set<String> membersAdded;
    private Set<String> membersRemoved;
    private boolean membersModified;

    public Group(Map<String, Object> groupMap) throws StorageClientException, AccessDeniedException {
        this(groupMap, null);
    }

    public Group(Map<String, Object> groupMap, Session session) throws StorageClientException, AccessDeniedException {
        super(groupMap, session);
        this.members = Sets.newLinkedHashSet(Iterables.of(StringUtils.split(
                (String) authorizableMap.get(MEMBERS_FIELD), ';')));
        this.membersAdded = Sets.newHashSet();
        this.membersRemoved = Sets.newHashSet();
        membersModified = true;
    }

    private static Group getEveryone() {
        try {
            return new GroupInternal(ImmutableMap.of("id", (Object) EVERYONE), null, false, true);
        } catch (StorageClientException e) {
            // it cant throw this since the session is null
        } catch (AccessDeniedException e) {
            // it cant throw this since the session is null
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroup() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getPropertiesForUpdate() {
        if (!readOnly && membersModified) {
            modifiedMap.put(MEMBERS_FIELD, StringUtils.join(members, ';'));
        }
        Map<String, Object> propertiesForUpdate = super.getPropertiesForUpdate();
        return propertiesForUpdate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO: Unit test
    public Map<String, Object> getSafeProperties() {
        if (!readOnly && membersModified) {
            modifiedMap.put(MEMBERS_FIELD, StringUtils.join(members, ';'));
        }
        return super.getSafeProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO: Unit test
    public boolean isModified() {
        return !readOnly && (membersModified || super.isModified());
    }

    public String[] getMembers() {
        return members.toArray(new String[members.size()]);
    }

    public void addMember(String member) {
        if (!readOnly && !members.contains(member)) {
            LOGGER.debug(" {} adding Member {} to {} ", new Object[] { this, member, members });
            members.add(member);
            membersAdded.add(member);
            membersRemoved.remove(member);
            membersModified = true;
        } else {
            LOGGER.debug("{} Member {} already present in {} ", new Object[] { this, member,
                    members });
        }
    }

    public void removeMember(String member) {
        if (!readOnly && members.contains(member)) {
            LOGGER.debug(" {} removing Member {} to {} ", new Object[] { this, member, members });
            members.remove(member);
            membersAdded.remove(member);
            membersRemoved.add(member);
            membersModified = true;
        } else {
            LOGGER.debug("{} Member {} already not present in {} ", new Object[] { this, member,
                    members });
        }
    }

    public String[] getMembersAdded() {
        return membersAdded.toArray(new String[membersAdded.size()]);
    }

    public String[] getMembersRemoved() {
        return membersRemoved.toArray(new String[membersRemoved.size()]);
    }

    public void reset(Map<String, Object> newMap) {
        if (!readOnly) {
            super.reset(newMap);
            LOGGER.debug("{} reset ", new Object[] { this });
            this.members = Sets.newLinkedHashSet(Iterables.of(StringUtils.split(
                    (String) authorizableMap.get(MEMBERS_FIELD), ';')));
            membersAdded.clear();
            membersRemoved.clear();
            membersModified = false;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); // Group and User shared the same key space.
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
