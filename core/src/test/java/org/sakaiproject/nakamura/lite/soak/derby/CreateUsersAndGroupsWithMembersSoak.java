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
package org.sakaiproject.nakamura.lite.soak.derby;

import java.io.IOException;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.ConfigurationImpl;
import org.sakaiproject.nakamura.lite.jdbc.derby.DerbySetup;
import org.sakaiproject.nakamura.lite.soak.AbstractSoakController;
import org.sakaiproject.nakamura.lite.soak.authorizable.CreateUsersAndGroupsWithMembersClient;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;

import com.google.common.collect.Maps;

public class CreateUsersAndGroupsWithMembersSoak extends AbstractSoakController {

    private int totalUsers;
    private StorageClientPool connectionPool;
    private int totalGroups;
    private Configuration configuration;

    public CreateUsersAndGroupsWithMembersSoak(int totalUsers, int totalGroups,
            StorageClientPool connectionPool, Configuration configuration) {
        super(totalUsers);
        this.configuration = configuration;
        this.connectionPool = connectionPool;
        this.totalUsers = totalUsers;
        this.totalGroups = totalGroups;
    }

    protected Runnable getRunnable(int nthreads) throws ClientPoolException,
            StorageClientException, AccessDeniedException {
        int usersPerThread = totalUsers / nthreads;
        int groupsPerThread = totalGroups / nthreads;
        return new CreateUsersAndGroupsWithMembersClient(usersPerThread, groupsPerThread,
                connectionPool, configuration);
    }

    public static void main(String[] argv) throws ClientPoolException, StorageClientException,
            AccessDeniedException, ClassNotFoundException, IOException {

        int totalUsers = 1000;
        int totalGroups = 1000;
        int nthreads = 10;

        if (argv.length > 0) {
            nthreads = StorageClientUtils.getSetting(Integer.valueOf(argv[0]), nthreads);
        }
        if (argv.length > 1) {
            totalUsers = StorageClientUtils.getSetting(Integer.valueOf(argv[1]), totalUsers);
        }
        if (argv.length > 2) {
            totalGroups = StorageClientUtils.getSetting(Integer.valueOf(argv[2]), totalUsers);
        }
        ConfigurationImpl configuration = new ConfigurationImpl();
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("keyspace", "n");
        properties.put("acl-column-family", "ac");
        properties.put("authorizable-column-family", "au");
        properties.put("content-column-family", "cn");
        configuration.activate(properties);

        CreateUsersAndGroupsWithMembersSoak createUsersAndGroupsSoak = new CreateUsersAndGroupsWithMembersSoak(
                totalUsers, totalGroups, DerbySetup.getClientPool(configuration,"jdbc:derby:target/soak/db;create=true"), configuration);
        createUsersAndGroupsSoak.launchSoak(nthreads);
    }

}
