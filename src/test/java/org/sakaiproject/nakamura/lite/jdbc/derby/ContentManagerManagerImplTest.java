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
package org.sakaiproject.nakamura.lite.jdbc.derby;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.lite.LoggingStorageListener;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
import org.sakaiproject.nakamura.lite.content.AbstractContentManagerTest;
import org.sakaiproject.nakamura.lite.content.ContentManagerImpl;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;

public class ContentManagerManagerImplTest extends AbstractContentManagerTest {

    @Override
    protected StorageClientPool getClientPool() throws ClassNotFoundException {
        return DerbySetup.getClientPool();
    }

    @Test
    public void findVsGet() throws Exception {
        AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = AuthenticatorImpl.authenticate("admin", "admin");

        AccessControlManagerImpl accessControlManager = new AccessControlManagerImpl(client,
                currentUser, configuration, null, new LoggingStorageListener(), principalValidatorResolver);

        ContentManagerImpl contentManager = new ContentManagerImpl(client, accessControlManager,
                configuration, null, new LoggingStorageListener());
        contentManager.update(new Content("/test", ImmutableMap.of("prop1", (Object) "val1")));

        Content viaGet = contentManager.get("/test");
        Assert.assertEquals("/test", viaGet.getPath());

        Iterable<Content> results = contentManager.find(ImmutableMap.of("prop1", (Object) "val1"));
        Content viaFind = results.iterator().next();
        Assert.assertEquals("/test", viaFind.getPath());
        Assert.assertEquals("val1", viaFind.getProperty("prop1"));

        viaFind.setProperty("prop1", "newval");
        contentManager.update(viaFind);

        Content viaGetAfterUpdate = contentManager.get("/test");
        Assert.assertEquals("newval", viaGetAfterUpdate.getProperty("prop1"));

        Iterable<Content> resultsAfterUpdate = contentManager.find(ImmutableMap.of("prop1", (Object) "newval"));
        Content viaFindAfterUpdate = resultsAfterUpdate.iterator().next();
        Assert.assertEquals("newval", viaFindAfterUpdate.getProperty("prop1"));

    }
}
