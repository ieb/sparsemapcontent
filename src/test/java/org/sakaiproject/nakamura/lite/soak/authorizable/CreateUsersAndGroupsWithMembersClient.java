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
package org.sakaiproject.nakamura.lite.soak.authorizable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.sakaiproject.nakamura.api.lite.CacheHolder;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorResolver;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.LoggingStorageListener;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.PrincipalValidatorResolverImpl;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableManagerImpl;
import org.sakaiproject.nakamura.lite.soak.AbstractScalingClient;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CreateUsersAndGroupsWithMembersClient extends AbstractScalingClient {

    private int nusers;
    private int ngroups;
    private Map<String, CacheHolder> sharedCache = new ConcurrentHashMap<String, CacheHolder>(1000);
    private PrincipalValidatorResolver principalValidatorResolver = new PrincipalValidatorResolverImpl();

    public CreateUsersAndGroupsWithMembersClient(int totalUsers, int totalGroups,
            StorageClientPool connectionPool, Configuration configuration) throws ClientPoolException, StorageClientException,
            AccessDeniedException {
        super(connectionPool, configuration);
        nusers = totalUsers;
        ngroups = totalGroups;
    }

    public void run() {
        try {
            super.setup();
            String tname = String.valueOf(Thread.currentThread().getId())
                    + String.valueOf(System.currentTimeMillis());
            AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration, null);
            User currentUser = AuthenticatorImpl.authenticate("admin", "admin");

            AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(
                    client, currentUser, configuration, sharedCache,  new LoggingStorageListener(), principalValidatorResolver);

            AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                    null, client, configuration, accessControlManagerImpl, sharedCache,  new LoggingStorageListener());

            List<String> userNames = Lists.newArrayList();
            List<String> groupNames = Lists.newArrayList();

            for (int i = 0; i < nusers; i++) {
                userNames.add("u" + tname + "_" + i);
            }
            for (int i = 0; i < ngroups; i++) {
                userNames.add("g" + tname + "_" + i);
            }
            for (String userId : userNames) {
                authorizableManager.createUser(userId, userId, "test",
                        ImmutableMap.of(userId, (Object) "testvalue"));
            }
            int mu = 0;
            for (String groupId : groupNames) {
                authorizableManager.createGroup(groupId, groupId,
                        ImmutableMap.of(groupId, (Object) "testvalue"));
                Group group = (Group) authorizableManager.findAuthorizable(groupId);
                for (int i = 0; i < 4; i++) {
                    group.addMember(userNames.get(mu));
                    mu++;
                    if (mu >= userNames.size()) {
                        mu = 0;
                    }
                }
            }

        } catch (StorageClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AccessDeniedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
