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
package org.sakaiproject.nakamura.lite.accesscontrol;

import com.google.common.collect.Maps;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AclModification;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permission;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.CachingManager;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccessControlManagerImpl extends CachingManager implements AccessControlManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlManagerImpl.class);
    private User user;
    private String keySpace;
    private String aclColumnFamily;
    private Map<String, int[]> cache = new ConcurrentHashMap<String, int[]>();
    private boolean closed;

    public AccessControlManagerImpl(StorageClient client, User currentUser, Configuration config,
            Map<String, CacheHolder> sharedCache) {
        super(client, sharedCache);
        this.user = currentUser;
        this.aclColumnFamily = config.getAclColumnFamily();
        this.keySpace = config.getKeySpace();
        closed = false;
    }

    public Map<String, Object> getAcl(String objectType, String objectPath)
            throws StorageClientException, AccessDeniedException {
        checkOpen();
        check(objectType, objectPath, Permissions.CAN_READ_ACL);

        String key = this.getAclKey(objectType, objectPath);
        return getCached(keySpace, aclColumnFamily, key);
    }

    public void setAcl(String objectType, String objectPath, AclModification[] aclModifications)
            throws StorageClientException, AccessDeniedException {
        checkOpen();
        check(objectType, objectPath, Permissions.CAN_WRITE_ACL);
        String key = this.getAclKey(objectType, objectPath);
        Map<String, Object> currentAcl = getAcl(objectType, objectPath);
        Map<String, Object> modifications = Maps.newLinkedHashMap();
        for (AclModification m : aclModifications) {
            String name = m.getAceKey();
            if (m.isRemove()) {
                modifications.put(name, null);
            } else {

                int bitmap = StorageClientUtils.toInt(currentAcl.get(name));
                bitmap = m.modify(bitmap);
                modifications.put(name, StorageClientUtils.toStore(bitmap));
            }
        }
        LOGGER.debug("Updating ACL {} {} ", key, modifications);
        putCached(keySpace, aclColumnFamily, key, modifications);
    }

    public void check(String objectType, String objectPath, Permission permission)
            throws AccessDeniedException, StorageClientException {
        if (user.isAdmin()) {
            return;
        }
        // users can always operate on their own user object.
        if (Security.ZONE_AUTHORIZABLES.equals(objectType) && user.getId().equals(objectPath)) {
            return;
        }
        int[] privileges = compilePermission(user, objectType, objectPath, 0);
        if (!((permission.getPermission() & privileges[0]) == permission.getPermission())) {
            throw new AccessDeniedException(objectType, objectPath, permission.getDescription(),
                    user.getId());
        }
    }

    private String getAclKey(String objectType, String objectPath) {
        if (objectPath.startsWith("/")) {
            return objectType + objectPath;
        }
        return objectType + "/" + objectPath;
    }

    private int[] compilePermission(Authorizable authorizable, String objectType,
            String objectPath, int recursion) throws StorageClientException {
        String key = getAclKey(objectType, objectPath);
        if (user.getId().equals(authorizable.getId()) && cache.containsKey(key)) {
            return cache.get(key);
        } else {
            LOGGER.debug("Cache Miss {} [{}] ", cache, key);
        }

        Map<String, Object> acl = getCached(keySpace, aclColumnFamily, key);
        LOGGER.debug("ACL on {} is {} ", key, acl);

        int grants = 0;
        int denies = 0;
        if (acl != null) {

            {
                String principal = authorizable.getId();
                int tg = StorageClientUtils.toInt(acl.get(principal
                        + AclModification.GRANTED_MARKER));
                int td = StorageClientUtils.toInt(acl
                        .get(principal + AclModification.DENIED_MARKER));
                grants = grants | tg;
                denies = denies | td;
                // LOGGER.info("Added Permissions for {} {}   result {} {}",new
                // Object[]{tg,td,grants,denies});

            }
            for (String principal : authorizable.getPrincipals()) {
                int tg = StorageClientUtils.toInt(acl.get(principal
                        + AclModification.GRANTED_MARKER));
                int td = StorageClientUtils.toInt(acl
                        .get(principal + AclModification.DENIED_MARKER));
                grants = grants | tg;
                denies = denies | td;
                // LOGGER.info("Added Permissions for {} {}   result {} {}",new
                // Object[]{tg,td,grants,denies});
            }
            if (!User.ANON_USER.equals(authorizable.getId())) {
                // all users except anon are in the group everyone, by default
                // but only if not already denied or granted by a more specific
                // permission.
                int tg = (StorageClientUtils.toInt(acl.get(Group.EVERYONE
                        + AclModification.GRANTED_MARKER)) & ~denies);
                int td = (StorageClientUtils.toInt(acl.get(Group.EVERYONE
                        + AclModification.DENIED_MARKER)) & ~grants);
                // LOGGER.info("Adding Permissions for Everyone {} {} ",tg,td);
                grants = grants | tg;
                denies = denies | td;

            }
            /*
             * grants contains the granted permissions in a bitmap denies
             * contains the denied permissions in a bitmap
             */
            int granted = grants;
            int denied = denies;

            /*
             * Only look to parent objects if this is not the root object and
             * everything is not granted and denied
             */
            if (recursion < 20 && !StorageClientUtils.isRoot(objectPath)
                    && (granted != 0xffff || denied != 0xffff)) {
                recursion++;
                int[] parentPriv = compilePermission(authorizable, objectType,
                        StorageClientUtils.getParentObjectPath(objectPath), recursion);
                if (parentPriv != null) {
                    /*
                     * Grant permission not denied at this level parentPriv[0]
                     * is permissions granted by the parent ~denies is
                     * permissions not denied here parentPriv[0] & ~denies is
                     * permissions granted by the parent that have not been
                     * denied here. we need to add those to things granted here.
                     * ie |
                     */
                    granted = grants | (parentPriv[0] & ~denies);
                    /*
                     * Deny permissions not granted at this level
                     */
                    denied = denies | (parentPriv[1] & ~grants);
                }
            }
            // If not denied all users and groups can read other users and
            // groups and all content can be read
            if (((denied & Permissions.CAN_READ.getPermission()) == 0)
                    && (Security.ZONE_AUTHORIZABLES.equals(objectType) || Security.ZONE_CONTENT
                            .equals(objectType))) {
                granted = granted | Permissions.CAN_READ.getPermission();
                // LOGGER.info("Default Read Permission set {} {} ",key,denied);
            } else {
                // LOGGER.info("Default Read has been denied {} {} ",key,
                // denied);
            }
            // LOGGER.info("Permissions on {} for {} is {} {} ",new
            // Object[]{key,user.getId(),granted,denied});
            /*
             * Keep a cached copy
             */
            if (user.getId().equals(authorizable.getId())) {
                cache.put(key, new int[] { granted, denied });
            }
            return new int[] { granted, denied };

        }
        if (Security.ZONE_AUTHORIZABLES.equals(objectType)
                || Security.ZONE_CONTENT.equals(objectType)) {
            // unless explicitly denied all users can read other users.
            return new int[] { Permissions.CAN_READ.getPermission(), 0 };
        }
        return new int[] { 0, 0 };
    }

    public String getCurrentUserId() {
        return user.getId();
    }

    public void close() {
        closed = true;
    }

    private void checkOpen() throws StorageClientException {
        if (closed) {
            throw new StorageClientException("Access Control Manager is closed");
        }
    }

    public boolean can(Authorizable authorizable, String objectType, String objectPath,
            Permission permission) {
        if (authorizable instanceof User && ((User) authorizable).isAdmin()) {
            return true;
        }
        // users can always operate on their own user object.
        if (Security.ZONE_AUTHORIZABLES.equals(objectType)
                && authorizable.getId().equals(objectPath)) {
            return true;
        }
        try {
            int[] privileges = compilePermission(authorizable, objectType, objectPath, 0);
            if (!((permission.getPermission() & privileges[0]) == permission.getPermission())) {
                return false;
            }
        } catch (StorageClientException e) {
            LOGGER.warn(e.getMessage(), e);
            return false;
        }
        return true;
    }

}
