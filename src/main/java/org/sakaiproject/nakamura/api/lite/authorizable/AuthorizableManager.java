package org.sakaiproject.nakamura.api.lite.authorizable;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;

public interface AuthorizableManager {

    Authorizable findAuthorizable(String authorizableId) throws AccessDeniedException,
            StorageClientException;

    void updateAuthorizable(Authorizable authorizable) throws AccessDeniedException,
            StorageClientException;

    boolean createGroup(String authorizableId, String authorizableName,
            Map<String, Object> properties) throws AccessDeniedException, StorageClientException;

    boolean createUser(String authorizableId, String authorizableName, String password,
            Map<String, Object> properties) throws AccessDeniedException, StorageClientException;

    void delete(String authorizableId) throws AccessDeniedException, StorageClientException;

}
