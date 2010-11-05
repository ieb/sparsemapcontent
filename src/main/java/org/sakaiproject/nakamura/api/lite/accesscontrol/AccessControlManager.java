package org.sakaiproject.nakamura.api.lite.accesscontrol;

import java.util.Map;

import org.sakaiproject.nakamura.lite.storage.StorageClientException;

public interface AccessControlManager {

    Map<String, Object> getAcl(String objectType, String objectPath) throws StorageClientException,
            AccessDeniedException;

    void setAcl(String objectType, String objectPath, AclModification[] aclModifications)
            throws StorageClientException, AccessDeniedException;

    void check(String objectType, String objectPath, Permission permission)
            throws AccessDeniedException;

}
