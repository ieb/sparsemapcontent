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
package org.sakaiproject.nakamura.lite.authorizable;

import com.google.common.collect.ImmutableMap;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AuthorizableActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizableActivator.class);
    private StorageClient client;
    private String keySpace;
    private String authorizableColumnFamily;

    public AuthorizableActivator(StorageClient client, Configuration configuration)
            throws StorageClientException, AccessDeniedException {
        this.client = client;
        this.authorizableColumnFamily = configuration.getAuthorizableColumnFamily();
        this.keySpace = configuration.getKeySpace();
    }

    public synchronized void setup() throws StorageClientException {
        createAdminUser();
        createAnonUser();
        createSystemUser();
        createAdministratorsGroup();
    }

    private void createAdministratorsGroup() throws StorageClientException {
        Map<String, Object> authorizableMap = client.get(keySpace, authorizableColumnFamily,
                Authorizable.ADMINISTRATORS_GROUP);
        if (authorizableMap == null || authorizableMap.size() == 0) {
            Map<String, Object> group = ImmutableMap.of(Authorizable.ID_FIELD,
                    (Object)Authorizable.ADMINISTRATORS_GROUP,
                    Authorizable.NAME_FIELD,
                    Authorizable.ADMINISTRATORS_GROUP,
                    Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.GROUP_VALUE);
            LOGGER.debug("Creating System User user as {} with {} ",
                    Authorizable.ADMINISTRATORS_GROUP, group);
            client.insert(keySpace, authorizableColumnFamily, Authorizable.ADMINISTRATORS_GROUP,
                    group, true);
        } else {
            LOGGER.debug("System User user exists as {} with {} ",
                    Authorizable.ADMINISTRATORS_GROUP, authorizableMap);

        }
    }

    private void createSystemUser() throws StorageClientException {
        Map<String, Object> authorizableMap = client.get(keySpace, authorizableColumnFamily,
                User.SYSTEM_USER);
        if (authorizableMap == null || authorizableMap.size() == 0) {
            Map<String, Object> user = ImmutableMap.of(Authorizable.ID_FIELD,
                    (Object)User.SYSTEM_USER, Authorizable.NAME_FIELD,
                    User.SYSTEM_USER, Authorizable.PASSWORD_FIELD,
                    "--no-password--",
                    Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.USER_VALUE);
            LOGGER.info("Creating System User user as {} with {} ", User.SYSTEM_USER, user);
            client.insert(keySpace, authorizableColumnFamily, User.SYSTEM_USER, user, true);
        } else {
            LOGGER.info("System User user exists as {} with {} ", User.SYSTEM_USER, authorizableMap);

        }
    }

    private void createAdminUser() throws StorageClientException {
        Map<String, Object> authorizableMap = client.get(keySpace, authorizableColumnFamily,
                User.ADMIN_USER);
        if (authorizableMap == null || authorizableMap.size() == 0) {
            Map<String, Object> user = ImmutableMap.of(Authorizable.ID_FIELD,
                    (Object)User.ADMIN_USER, Authorizable.NAME_FIELD,
                    User.ADMIN_USER, Authorizable.PASSWORD_FIELD,
                    StorageClientUtils.secureHash("admin"),
                    Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.USER_VALUE);
            LOGGER.info("Creating Admin User user as {} with {} ", User.ADMIN_USER, user);
            client.insert(keySpace, authorizableColumnFamily, User.ADMIN_USER, user, true);
        } else {
            LOGGER.info("Admin User user exists as {} with {} ", User.ADMIN_USER, authorizableMap);
        }
    }

    private void createAnonUser() throws StorageClientException {
        Map<String, Object> authorizableMap = client.get(keySpace, authorizableColumnFamily,
                User.ANON_USER);
        if (authorizableMap == null || authorizableMap.size() == 0) {
            Map<String, Object> user = ImmutableMap.of(Authorizable.ID_FIELD,
                    (Object)User.ANON_USER, Authorizable.NAME_FIELD,
                    User.ANON_USER, Authorizable.PASSWORD_FIELD,
                    Authorizable.NO_PASSWORD,
                    Authorizable.AUTHORIZABLE_TYPE_FIELD, Authorizable.USER_VALUE);
            LOGGER.info("Creating Anon user as {} with {} ", User.ANON_USER, user);
            client.insert(keySpace, authorizableColumnFamily, User.ANON_USER, user, true);
        } else {
            LOGGER.info("Anon User user exists as {} with {} ", User.ANON_USER, authorizableMap);
        }
    }

}
