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
package org.sakaiproject.nakamura.api.lite;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Authenticator;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.lite.lock.LockManager;

/**
 * A lightweight container bound to the user that will maintain state associated
 * with the user for as long as the session exists. A session provides access to
 * Content, ACLs and Authorizables. Session implementations are not assumed to
 * be thread safe and should not be shared between threads without explicit
 * synchronzation, similarly Session implementations should not be bound to
 * threads
 */
public interface Session {

    /**
     * Logout of the session releasing all resources
     * 
     * @throws ClientPoolException
     */
    void logout() throws ClientPoolException;

    /**
     * @return the access control manager for this session.
     * @throws StorageClientException
     */
    AccessControlManager getAccessControlManager() throws StorageClientException;

    /**
     * @return the authorizable manager for this session.
     * @throws StorageClientException
     */
    AuthorizableManager getAuthorizableManager() throws StorageClientException;

    /**
     * @return the content manager for this session.
     * @throws StorageClientException
     */
    ContentManager getContentManager() throws StorageClientException;
    
    
    LockManager getLockManager() throws StorageClientException;

    /**
     * @return the userID that this session is bound to.
     */
    String getUserId();

    Authenticator getAuthenticator() throws StorageClientException;

    Repository getRepository();

    /**
     * Perform a commit on any pending operations.
     */
    void commit();

    /**
     * Add a commit handler for a certain key. Will replace any other commit handler of the same key.
     * @param key
     * @param commitHandler
     */
    void addCommitHandler(String key, CommitHandler commitHandler);

}
