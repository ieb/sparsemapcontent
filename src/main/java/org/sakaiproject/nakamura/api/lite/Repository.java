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

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

/**
 * Repository container that provides a mechanism to login to the sparse content
 * store.
 * @since 1.0
 */
public interface Repository {

    /**
     * The prefix on all system properties in the repository. Anything prefixed
     * with this is a system proper anything not prefixed with this is not a
     * system property.
     */
    public static final String SYSTEM_PROP_PREFIX = "_";

    /**
     * Login with a user name and password
     * 
     * @param username
     *            the username
     * @param password
     *            the password
     * @return a session for the username if the login was valid.
     * @throws ClientPoolException
     *             If there was a problem getting resources from the pool.
     * @throws StorageClientException
     *             If there was a problem with the storage pool.
     * @throws AccessDeniedException
     *             If the user was denied access.
     * @since 1.0
     */
    Session login(String username, String password) throws ClientPoolException,
            StorageClientException, AccessDeniedException;

    /**
     * Perform an anon login
     * 
     * @return an anon session.
     * @throws ClientPoolException
     *             If there was a problem getting resources from the pool.
     * @throws StorageClientException
     *             If there was a problem with the storage pool.
     * @throws AccessDeniedException
     *             If the anon was denied access.
     * @since 1.0
     */
    Session login() throws ClientPoolException, StorageClientException, AccessDeniedException;

    /**
     * Perform an administrative login as the super user (admin)
     * 
     * @return an admin session.
     * @throws ClientPoolException
     *             If there was a problem getting resources from the pool.
     * @throws StorageClientException
     *             If there was a problem with the storage pool.
     * @throws AccessDeniedException
     *             If admin was denied access.
     * @since 1.0
     */
    Session loginAdministrative() throws ClientPoolException, StorageClientException,
            AccessDeniedException;

    /**
     * Perform an administrative login as the identified user
     * 
     * @param username
     *            the user to login as
     * @return a session bound to the user.
     * @throws ClientPoolException
     *             If there was a problem getting resources from the pool.
     * @throws StorageClientException
     *             If there was a problem with the storage pool.
     * @throws AccessDeniedException
     *             If the user was denied access.
     * @since 1.0
     */
    Session loginAdministrative(String username) throws ClientPoolException,
            StorageClientException, AccessDeniedException;

    /**
     * Perform an administrative login bypassing login enabled checks. Only
     * internal system operations should use this. Anything related to a login
     * should never use use.
     * 
     * @param username
     * @return
     * @throws StorageClientException
     * @throws ClientPoolException
     * @throws AccessDeniedException
     * @since 1.4
     */
    Session loginAdministrativeBypassEnable(String username) throws StorageClientException,
            ClientPoolException, AccessDeniedException;

}
