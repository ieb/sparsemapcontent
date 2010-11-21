package org.sakaiproject.nakamura.api.lite.authorizable;

import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

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

    void changePassword(Authorizable authorizable, String password) throws StorageClientException, AccessDeniedException;

    Iterator<Authorizable> findAuthorizable(String propertyName, String value,
            Class<? extends Authorizable> authorizableType) throws StorageClientException;
    
    

}
