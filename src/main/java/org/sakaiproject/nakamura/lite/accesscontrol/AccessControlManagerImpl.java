package org.sakaiproject.nakamura.lite.accesscontrol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AclModification;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permission;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;

import com.google.common.collect.Maps;

public class AccessControlManagerImpl implements AccessControlManager {

    private static final String GRANTED_MARKER = "@g";
    private static final String DENIED_MARKER = "@d";
    private StorageClient client;
    private User user;
    private String keySpace;
    private String aclColumnFamily;
    private Map<String, int[]> cache = new ConcurrentHashMap<String, int[]>();
    private boolean closed;

    public AccessControlManagerImpl(StorageClient client, User currentUser, Configuration config) {
        this.user = currentUser;
        this.client = client;
        this.aclColumnFamily = config.getAclColumnFamily();
        this.keySpace = config.getKeySpace();
        closed = false;
    }

    public int toBitmap(Object value) {
        if (value != null) {
            return Integer.valueOf(StorageClientUtils.toString(value), 16);
        }
        return 0x00;
    }

    public Map<String, Object> getAcl(String objectType, String objectPath)
            throws StorageClientException, AccessDeniedException {
        checkOpen();
        check(objectType, objectPath, Permissions.CAN_READ_ACL);
        String key = this.getAclKey(objectType, objectPath);
        return client.get(keySpace, aclColumnFamily, key);
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

                int bitmap = toBitmap(currentAcl.get(name));
                bitmap = m.modify(bitmap);
                modifications.put(name, StorageClientUtils.toStore(bitmap));
            }
        }
        client.insert(keySpace, aclColumnFamily, key, modifications);
    }

    public void check(String objectType, String objectPath, Permission permission)
            throws AccessDeniedException {
        if (user.isAdmin()) {
            return;
        }
        int[] privileges = compilePermission(objectType, objectPath, 0);
        if (!((permission.getPermission() & privileges[0]) == permission.getPermission())) {
            throw new AccessDeniedException(objectType, objectPath, permission.getDescription());
        }
    }

    private String getAclKey(String objectType, String objectPath) {
        if (objectPath.startsWith("/")) {
            return objectType + objectPath;
        }
        return objectType + "/" + objectPath;
    }

    private int[] compilePermission(String objectType, String objectPath, int recursion) {
        String key = getAclKey(objectType, objectPath);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Map<String, Object> acl = null;
        try {
            acl = client.get(keySpace, aclColumnFamily, key);
        } catch (StorageClientException e) {

        }

        int grants = 0;
        int denies = 0;
        if (acl != null) {
            for (String principal : user.getPrincipals()) {
                grants = grants | toBitmap(acl.get(principal + GRANTED_MARKER));
                denies = denies | toBitmap(acl.get(principal + DENIED_MARKER));
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
                int[] parentPriv = compilePermission(objectType,
                        StorageClientUtils.getParentObjectPath(objectPath), recursion);
                if (parentPriv != null) {
                    /*
                     * Grant things not denied at this level
                     */
                    granted = grants & (parentPriv[0] & ~denies);
                    /*
                     * Deny things not granted at this level
                     */
                    denied = denies & (parentPriv[1] & ~grants);
                    /*
                     * Keep a cached copy
                     */
                    cache.put(key, new int[] { granted, denied });
                    return new int[] { granted, denied };
                }
            }
        }
        return new int[] { 0, 0 };
    }

    @Override
    public String getCurrentUserId() {
        return user.getId();
    }

    public void close() {
        closed = true;
    }
    
    private void checkOpen() throws StorageClientException {
        if ( closed ) {
            throw new StorageClientException("Access Control Manager is closed");
        }
    }

}
