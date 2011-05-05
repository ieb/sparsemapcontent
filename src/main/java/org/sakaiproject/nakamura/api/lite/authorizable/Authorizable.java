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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.RemoveProperty;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.util.Iterables;
import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base Authorizable object.
 */
public class Authorizable {

    public static final String PASSWORD_FIELD = "pwd";

    public static final String PRINCIPALS_FIELD = "principals";

    public static final String MEMBERS_FIELD = "members";

    public static final String ID_FIELD = "id";

    public static final String NAME_FIELD = "name";

    public static final String AUTHORIZABLE_TYPE_FIELD = "type";

    public static final String GROUP_VALUE = "g";
    public static final Object USER_VALUE = "u";

    public static final String ADMINISTRATORS_GROUP = "administrators";

    public static final String LASTMODIFIED_FIELD = "lastModified";
    public static final String LASTMODIFIED_BY_FIELD = "lastModifiedBy";
    public static final String CREATED_FIELD = "created";
    public static final String CREATED_BY_FIELD = "createdBy";

    private static final Set<String> FILTER_PROPERTIES = ImmutableSet.of(PASSWORD_FIELD, ID_FIELD);

    private static final Set<String> PRIVATE_PROPERTIES = ImmutableSet.of(PASSWORD_FIELD);

    public static final String NO_PASSWORD = "--none--";

    protected static final Logger LOGGER = LoggerFactory.getLogger(Authorizable.class);

    /**
     * A read only copy of the map, protected by an Immutable Wrapper
     */
    protected ImmutableMap<String, Object> authorizableMap;
    protected Set<String> principals;

    protected String id;

    /**
     * Modifications to the map.
     */
    protected Map<String,Object> modifiedMap;
    protected boolean principalsModified;

    private boolean isObjectNew = true;

    protected boolean readOnly;

    public Authorizable(Map<String, Object> autorizableMap) {
        principalsModified = false;
        modifiedMap = Maps.newHashMap();
        init(autorizableMap);
    }
    
    private void init(Map<String, Object> newMap) {
        this.authorizableMap = ImmutableMap.copyOf(newMap);
        Object principalsB = authorizableMap.get(PRINCIPALS_FIELD);
        if (principalsB == null) {
            this.principals = Sets.newLinkedHashSet();
        } else {
            this.principals = Sets.newLinkedHashSet(Iterables.of(StringUtils.split(
                    (String) principalsB, ';')));
        }
        this.id = (String) authorizableMap.get(ID_FIELD);
        if (!User.ANON_USER.equals(this.id)) {
          this.principals.add(Group.EVERYONE);
        }
    }

    public void reset(Map<String, Object> newMap) {
        if ( !readOnly ) {
            principalsModified = false;
            modifiedMap.clear();
            init(newMap);
            
            LOGGER.debug("After Update to Authorizable {} ", authorizableMap);
        }
    }


    public String[] getPrincipals() {
        return principals.toArray(new String[principals.size()]);
    }

    public String getId() {
        return id;
    }

    // TODO: Unit test
    public Map<String, Object> getSafeProperties() {
        if (!readOnly && principalsModified) {
            modifiedMap.put(PRINCIPALS_FIELD, StringUtils.join(principals, ';'));
        }
        return StorageClientUtils.getFilterMap(authorizableMap, modifiedMap, null, FILTER_PROPERTIES);
    }
    
    public boolean isGroup() {
        return false;
    }

    public Map<String, Object> getOriginalProperties() {
        return StorageClientUtils.getFilterMap(authorizableMap, null, null, FILTER_PROPERTIES);
    }

    public void setProperty(String key, Object value) {
        if (!readOnly && !FILTER_PROPERTIES.contains(key)) {
            Object cv = authorizableMap.get(key);
            if (!value.equals(cv)) {
                modifiedMap.put(key,value);
            } else if ( modifiedMap.containsKey(key) && !value.equals(modifiedMap.get(key))) {
                modifiedMap.put(key, value);
            }

        }
    }

    public Object getProperty(String key) {
        if (!PRIVATE_PROPERTIES.contains(key)) {
            if ( modifiedMap.containsKey(key)) {
                Object o = modifiedMap.get(key);
                if ( o instanceof RemoveProperty ) {
                    return null;
                } else {
                    return o;
                }
            }
            return authorizableMap.get(key);
        }
        return null;
    }

    public void removeProperty(String key) {
        if (!readOnly && authorizableMap.containsKey(key)) {
            modifiedMap.put(key, new RemoveProperty());
        }
    }

    public void addPrincipal(String principal) {
        if (!readOnly && !principals.contains(principal)) {
            principals.add(principal);
            principalsModified = true;
        }
    }

    public void removePrincipal(String principal) {
        if (!readOnly && principals.contains(principal)) {
            principals.remove(principal);
            principalsModified = true;
        }
    }

    public Map<String, Object> getPropertiesForUpdate() {
        if (!readOnly && principalsModified) {
            principals.remove(Group.EVERYONE);
            modifiedMap.put(PRINCIPALS_FIELD, StringUtils.join(principals, ';'));
            principals.add(Group.EVERYONE);
        }
        return StorageClientUtils.getFilterMap(authorizableMap, modifiedMap, null,
                FILTER_PROPERTIES);
    }



    public boolean isModified() {
        return !readOnly && (principalsModified || (modifiedMap.size() > 0));
    }

    public boolean hasProperty(String name) {
        if ( modifiedMap.get(name) instanceof RemoveProperty ) {
            return false;
        }
        return authorizableMap.containsKey(name);
    }

    public Iterator<Group> memberOf(final AuthorizableManager authorizableManager) {
        final List<String> memberIds = new ArrayList<String>();
        Collections.addAll(memberIds, getPrincipals());
        return new PreemptiveIterator<Group>() {

            private int p;
            private Group group;

            @Override
            protected boolean internalHasNext() {
                while (p < memberIds.size()) {
                    String id = memberIds.get(p);
                    p++;
                    try {
                        Authorizable a = authorizableManager.findAuthorizable(id);
                        if (a instanceof Group) {
                            group = (Group) a;
                            for (String pid : a.getPrincipals()) {
                                if (!memberIds.contains(pid)) {
                                    memberIds.add(pid);
                                }
                            }
                            return true;
                        }
                    } catch (AccessDeniedException e) {
                        LOGGER.debug(e.getMessage(), e);
                    } catch (StorageClientException e) {
                        LOGGER.debug(e.getMessage(), e);
                    }
                }
                return false;
            }

            @Override
            protected Group internalNext() {
                return group;
            }

        };
    }
    
    protected void setObjectNew(boolean isObjectNew) {
        this.isObjectNew = isObjectNew;
    }

    public boolean isNew() {
        return isObjectNew;
    }

    protected void setReadOnly(boolean readOnly) {
        if ( !this.readOnly ) {
            this.readOnly = readOnly;
        }
    }
    
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof Authorizable ) {
            Authorizable a = (Authorizable) obj;
            return id.equals(a.getId());
        }
        return super.equals(obj);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return id;
    }
}
