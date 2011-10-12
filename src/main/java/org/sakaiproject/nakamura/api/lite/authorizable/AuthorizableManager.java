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

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

import java.util.Iterator;
import java.util.Map;

/**
 * Service to manage authorizables.
 */
public interface AuthorizableManager {

    /**
     * Find an Authorizable by ID.
     *
     * @param authorizableId
     *          ID of the authorizable to find.
     * @return the authorizable object. null if none found
     * @throws AccessDeniedException
     * @throws StorageClientException
     */
    Authorizable findAuthorizable(String authorizableId) throws AccessDeniedException,
            StorageClientException;

    /**
     * Update an authorizable
     * @param authorizable the authorzable
     * @throws AccessDeniedException
     * @throws StorageClientException
     */
    void updateAuthorizable(Authorizable authorizable) throws AccessDeniedException,
            StorageClientException;

    /**
     * Update an authorizable with the option to not touch the user last modified information.
     * @param authorizable the authorizable.
     * @param withTouch if false the last modified information will not be changed, but only admin users can perform this.
     * @throws AccessDeniedException
     * @throws StorageClientException
     */
    void updateAuthorizable(Authorizable authorizable, boolean withTouch)
        throws AccessDeniedException, StorageClientException;

    /**
     * Create a group
     * @param authorizableId the group ID
     * @param authorizableName the groupName
     * @param properties initial properties of the group.
     * @return true if created.
     * @throws AccessDeniedException
     * @throws StorageClientException
     */
    boolean createGroup(String groupId, String groupName,
            Map<String, Object> properties) throws AccessDeniedException, StorageClientException;

    /**
     * Create a user
     *
     * @param userId the user ID
     * @param userName the user name
     * @param password the password (unencoded, null if no password)
     * @param properties initial properties of the user.
     * @return true if created. false otherwise.
     * @throws AccessDeniedException
     * @throws StorageClientException
     */
    boolean createUser(String userId, String userName, String password,
            Map<String, Object> properties) throws AccessDeniedException, StorageClientException;

    /**
     * Delete an authorizable.
     * @param authorizableId
     * @throws AccessDeniedException
     * @throws StorageClientException
     */
    void delete(String authorizableId) throws AccessDeniedException, StorageClientException;

    /**
     * Change the password of an authorizable (User only)
     * @param authorizable
     * @param password
     * @param oldPassword
     * @throws StorageClientException
     * @throws AccessDeniedException
     */
    void changePassword(Authorizable authorizable, String password, String oldPassword)
            throws StorageClientException, AccessDeniedException;

    
    /**
     * Administratively disable a password for the supplied user. Only admin can do this.
     * @param authorizable
     * @throws StorageClientException
     * @throws AccessDeniedException
     */
    void disablePassword(Authorizable authorizable)
        throws StorageClientException, AccessDeniedException;

    /**
     * Find authorizables by exact property matches
     * @param propertyName the name of the property
     * @param value the value of the property
     * @param authorizableType the type of the authorizable
     * @return a list of Authorizables that match the criteria.
     * @throws StorageClientException
     */
    Iterator<Authorizable> findAuthorizable(String propertyName, String value,
            Class<? extends Authorizable> authorizableType) throws StorageClientException;

    /**
     * @return the user bound to this authorizable manager.
     */
    User getUser();
    
    
    /**
     * @param path cause an event to be emitted for the path that will cause a refresh.
     * @throws AccessDeniedException 
     * @throws StorageClientException 
     */
    void triggerRefresh(String path) throws StorageClientException, AccessDeniedException;
    
    
    /**
     * Cause an event to be emitted for all items.
     * @throws StorageClientException 
     */
    void triggerRefreshAll() throws StorageClientException;


}
