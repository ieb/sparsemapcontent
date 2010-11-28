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

    void changePassword(Authorizable authorizable, String password, String oldPassword)
            throws StorageClientException, AccessDeniedException;

    Iterator<Authorizable> findAuthorizable(String propertyName, String value,
            Class<? extends Authorizable> authorizableType) throws StorageClientException;

}
