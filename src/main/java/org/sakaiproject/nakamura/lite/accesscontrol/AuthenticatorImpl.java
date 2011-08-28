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

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Authenticator;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.authorizable.UserInternal;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AuthenticatorImpl implements Authenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatorImpl.class);
    private StorageClient client;
    private String keySpace;
    private String authorizableColumnFamily;

    public AuthenticatorImpl(StorageClient client, Configuration configuration) {
        this.client = client;
        this.keySpace = configuration.getKeySpace();
        this.authorizableColumnFamily = configuration.getAuthorizableColumnFamily();
    }

    public User authenticate(String userid, String password) {
        try {
            Map<String, Object> userAuthMap = client
                    .get(keySpace, authorizableColumnFamily, userid);
            if (userAuthMap == null) {
                LOGGER.debug("User was not found {}", userid);
                return null;
            }
            String passwordHash = StorageClientUtils.secureHash(password);

            String storedPassword = (String) userAuthMap
                    .get(User.PASSWORD_FIELD);
            if (passwordHash.equals(storedPassword)) {
                return new UserInternal(userAuthMap, null, false);
            }
            LOGGER.debug("Failed to authentication, passwords did not match");
        } catch (StorageClientException e) {
            LOGGER.debug("Failed To authenticate " + e.getMessage(), e);
        } catch (AccessDeniedException e) {
            LOGGER.debug("Failed To system authenticate user " + e.getMessage(), e);
        }
        return null;

    }

    public User systemAuthenticate(String userid) {
        try {
            Map<String, Object> userAuthMap = client
                    .get(keySpace, authorizableColumnFamily, userid);
            if (userAuthMap == null || userAuthMap.size() == 0) {
                LOGGER.debug("User was not found {}", userid);
                return null;
            }
            return new UserInternal(userAuthMap, null, false);
        } catch (StorageClientException e) {
            LOGGER.debug("Failed To system authenticate user " + e.getMessage(), e);
        } catch (AccessDeniedException e) {
            LOGGER.debug("Failed To system authenticate user " + e.getMessage(), e);
        }
        return null;
    }

}
