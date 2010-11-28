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
package org.sakaiproject.nakamura.lite.soak.mysql;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.jdbc.mysql.MysqlSetup;
import org.sakaiproject.nakamura.lite.soak.AbstractSoakController;
import org.sakaiproject.nakamura.lite.soak.authorizable.CreateUsersAndGroupsClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;

public class CreateUsersAndGroupsSoak extends AbstractSoakController {

    private int totalUsers;
    private StorageClientPool connectionPool;

    public CreateUsersAndGroupsSoak(int totalUsers, StorageClientPool connectionPool) {
        super(totalUsers);
        this.connectionPool = connectionPool;
        this.totalUsers = totalUsers;
    }

    protected Runnable getRunnable(int nthreads) throws ClientPoolException,
            StorageClientException, AccessDeniedException {
        int usersPerThread = totalUsers / nthreads;
        return new CreateUsersAndGroupsClient(usersPerThread, connectionPool);
    }

    public static void main(String[] argv) throws ClientPoolException, StorageClientException,
            AccessDeniedException, ClassNotFoundException {

        int totalUsers = 1000;
        int nthreads = 10;

        if (argv.length > 0) {
            nthreads = StorageClientUtils.getSetting(Integer.valueOf(argv[0]), nthreads);
        }
        if (argv.length > 1) {
            totalUsers = StorageClientUtils.getSetting(Integer.valueOf(argv[1]), totalUsers);
        }

        CreateUsersAndGroupsSoak createUsersAndGroupsSoak = new CreateUsersAndGroupsSoak(
                totalUsers, MysqlSetup.getClientPool());
        createUsersAndGroupsSoak.launchSoak(nthreads);
    }

}
