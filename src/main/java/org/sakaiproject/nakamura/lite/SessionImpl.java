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
package org.sakaiproject.nakamura.lite;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageCacheManager;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StoreListener;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Authenticator;
import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorResolver;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableManagerImpl;
import org.sakaiproject.nakamura.lite.content.ContentManagerImpl;
import org.sakaiproject.nakamura.lite.storage.StorageClient;

public class SessionImpl implements Session {

    private AccessControlManagerImpl accessControlManager;
    private ContentManagerImpl contentManager;
    private AuthorizableManagerImpl authorizableManager;
    private User currentUser;
    private Repository repository;
    private Exception closedAt;
    private StorageClient client;
    private Authenticator authenticator;
    private StoreListener storeListener;

    public SessionImpl(Repository repository, User currentUser, StorageClient client,
            Configuration configuration, StorageCacheManager storageCacheManager, StoreListener storeListener, PrincipalValidatorResolver principalValidatorResolver)
            throws ClientPoolException, StorageClientException, AccessDeniedException {
        this.currentUser = currentUser;
        this.repository = repository;
        this.client = client;
        accessControlManager = new AccessControlManagerImpl(client, currentUser, configuration,
                storageCacheManager.getAccessControlCache(), storeListener, principalValidatorResolver);
        authorizableManager = new AuthorizableManagerImpl(currentUser, client, configuration,
                accessControlManager, storageCacheManager.getAuthorizableCache(), storeListener);

        contentManager = new ContentManagerImpl(client, accessControlManager, configuration, storageCacheManager.getContentCache(), storeListener);

        authenticator = new AuthenticatorImpl(client, configuration);
        this.storeListener = storeListener;
        storeListener.onLogin(currentUser.getId(), this.toString());
    }

    public void logout() throws ClientPoolException {
        if (closedAt == null) {
            accessControlManager.close();
            authorizableManager.close();
            contentManager.close();
            client.close();
            accessControlManager = null;
            authorizableManager = null;
            contentManager = null;
            client = null;
            authenticator = null;
            closedAt = new Exception("This session was closed at:");
            storeListener.onLogout(currentUser.getId(), this.toString());
        }
    }

    public AccessControlManagerImpl getAccessControlManager() throws StorageClientException {
        check();
        return accessControlManager;
    }

    public AuthorizableManagerImpl getAuthorizableManager() throws StorageClientException {
        check();
        return authorizableManager;
    }

    public ContentManagerImpl getContentManager() throws StorageClientException {
        check();
        return contentManager;
    }

    public Authenticator getAuthenticator() throws StorageClientException {
        check();
        return authenticator;
    }

    public Repository getRepository() {
        return repository;
    }

    public String getUserId() {
        return currentUser.getId();
    }

    private void check() throws StorageClientException {
        if (closedAt != null) {
            throw new StorageClientException(
                    "Session has been closed, see cause to see where this happend ", closedAt);
        }
    }

    public StorageClient getClient() {
        return client;
    }

}
