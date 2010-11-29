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

import com.google.common.collect.Maps;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AclModification;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.ConfigurationImpl;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractAccessControlManagerImplTest {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractAccessControlManagerImplTest.class);
    private StorageClient client;
    private ConfigurationImpl configuration;
    private StorageClientPool clientPool;

    @Before
    public void before() throws StorageClientException, AccessDeniedException, ClientPoolException,
            ClassNotFoundException {
        clientPool = getClientPool();
        client = clientPool.getClient();
        configuration = new ConfigurationImpl();
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("keyspace", "n");
        properties.put("acl-column-family", "ac");
        properties.put("authorizable-column-family", "au");
        configuration.activate(properties);
        AuthorizableActivator authorizableActivator = new AuthorizableActivator(client,
                configuration);
        authorizableActivator.setup();
        LOGGER.info("Setup Complete");
    }

    protected abstract StorageClientPool getClientPool() throws ClassNotFoundException;

    @After
    public void after() throws ClientPoolException {
        client.close();
    }

    @Test
    public void test() throws StorageClientException, AccessDeniedException {
        AuthenticatorImpl authenticator = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticator.authenticate("admin", "admin");

        AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(client,
                currentUser, configuration, null);
        AclModification user1 = new AclModification("user1", Permissions.CAN_ANYTHING.combine(
                Permissions.CAN_ANYTHING_ACL).getPermission(), AclModification.Operation.OP_REPLACE);
        AclModification user2 = new AclModification("user2", Permissions.CAN_READ
                .combine(Permissions.CAN_WRITE).combine(Permissions.CAN_DELETE).getPermission(),
                AclModification.Operation.OP_REPLACE);
        AclModification user3 = new AclModification("user3", Permissions.CAN_READ.getPermission(),
                AclModification.Operation.OP_REPLACE);

        accessControlManagerImpl.setAcl(Security.ZONE_AUTHORIZABLES, "testpath",
                new AclModification[] { user1, user2, user3 });

        Map<String, Object> acl = accessControlManagerImpl.getAcl(Security.ZONE_AUTHORIZABLES,
                "testpath");
        Assert.assertEquals(Integer.toHexString(Permissions.CAN_ANYTHING.combine(
                Permissions.CAN_ANYTHING_ACL).getPermission()), Integer
                .toHexString(StorageClientUtils.toInt(acl.get("user1"))));
        Assert.assertEquals(
                Permissions.CAN_READ.combine(Permissions.CAN_WRITE).combine(Permissions.CAN_DELETE)
                        .getPermission(), StorageClientUtils.toInt(acl.get("user2")));
        Assert.assertEquals(Permissions.CAN_READ.getPermission(),
                StorageClientUtils.toInt(acl.get("user3")));
        for (Entry<String, Object> e : acl.entrySet()) {
            LOGGER.info(" ACE {} : {} ", e.getKey(), StorageClientUtils.toInt(e.getValue()));
        }
        LOGGER.info("Got ACL {}", acl);

    }

}
