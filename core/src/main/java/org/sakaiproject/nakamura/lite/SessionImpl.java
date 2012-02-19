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

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.CacheHolder;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.CommitHandler;
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
import org.sakaiproject.nakamura.lite.lock.LockManagerImpl;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class SessionImpl implements Session {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionImpl.class);
    private AccessControlManagerImpl accessControlManager;
    private ContentManagerImpl contentManager;
    private AuthorizableManagerImpl authorizableManager;
    private LockManagerImpl lockManager;
    private User currentUser;
    private Repository repository;
    private Exception closedAt;
    private StorageClient client;
    private Authenticator authenticator;
    private StoreListener storeListener;
    private Map<String, CommitHandler> commitHandlers = Maps.newLinkedHashMap();
    private StorageCacheManager storageCacheManager;
    private Configuration configuration;
    private static long nagclient;

    public SessionImpl(Repository repository, User currentUser, StorageClient client,
            Configuration configuration, StorageCacheManager storageCacheManager,
            StoreListener storeListener, PrincipalValidatorResolver principalValidatorResolver)
            throws ClientPoolException, StorageClientException, AccessDeniedException {
        this.currentUser = currentUser;
        this.repository = repository;
        this.client = client;
        this.storageCacheManager = storageCacheManager;
        this.storeListener = storeListener;
        this.configuration = configuration;
        
        if ( this.storageCacheManager == null ) {
            if ( (nagclient % 1000) == 0 ) {
                LOGGER.warn("No Cache Manager, All Caching disabled, please provide an Implementation of NamedCacheManager. This message will appear every 1000th time a session is created. ");
            }
            nagclient++;
        }
        accessControlManager = new AccessControlManagerImpl(client, currentUser, configuration,
                getCache(configuration.getAclColumnFamily()), storeListener,
                principalValidatorResolver);
        Map<String, CacheHolder> authorizableCache = getCache(configuration
                .getAuthorizableColumnFamily());
        authorizableManager = new AuthorizableManagerImpl(currentUser, this, client, configuration,
                accessControlManager, authorizableCache, storeListener);

        contentManager = new ContentManagerImpl(client, accessControlManager, configuration,
                getCache(configuration.getContentColumnFamily()), storeListener);

        lockManager = new LockManagerImpl(client, configuration, currentUser,
                getCache(configuration.getLockColumnFamily()));

        authenticator = new AuthenticatorImpl(client, configuration, authorizableCache);

        storeListener.onLogin(currentUser.getId(), this.toString());
    }

    public void logout() throws ClientPoolException {
        if (closedAt == null) {
            commit();
            accessControlManager.close();
            authorizableManager.close();
            contentManager.close();
            lockManager.close();
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

    public LockManagerImpl getLockManager() throws StorageClientException {
        check();
        return lockManager;
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

    public void addCommitHandler(String key, CommitHandler commitHandler) {
        synchronized (commitHandlers) {
            commitHandlers.put(key, commitHandler);
        }
    }

    public void commit() {
        synchronized (commitHandlers) {
            for (CommitHandler commitHandler : commitHandlers.values()) {
                commitHandler.commit();
            }
            commitHandlers.clear();
        }
    }

    public Map<String, CacheHolder> getCache(String columnFamily) {
        if (storageCacheManager != null) {
            if (configuration.getAuthorizableColumnFamily().equals(columnFamily)) {
                return storageCacheManager.getAuthorizableCache();
            }
            if (configuration.getAclColumnFamily().equals(columnFamily)) {
                return storageCacheManager.getAccessControlCache();
            }
            if (configuration.getContentColumnFamily().equals(columnFamily)) {
                return storageCacheManager.getContentCache();
            }
            return storageCacheManager.getCache(columnFamily);
        }
        return null;
    }

}
