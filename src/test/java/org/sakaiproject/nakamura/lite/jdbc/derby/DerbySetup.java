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
import com.google.common.collect.ImmutableMap.Builder;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClientPool;

public class DerbySetup {

    private static JDBCStorageClientPool clientPool = null;

    private synchronized static JDBCStorageClientPool createClientPool(Configuration configuration, String location) {
        try {
            JDBCStorageClientPool connectionPool = new JDBCStorageClientPool();
            Builder<String, Object> configBuilder = ImmutableMap.builder();
            if ( location == null ) {
                location = "jdbc:derby:memory:MyDB;create=true";
            }
            configBuilder.put(JDBCStorageClientPool.CONNECTION_URL,
            location);
            configBuilder.put(JDBCStorageClientPool.JDBC_DRIVER, "org.apache.derby.jdbc.EmbeddedDriver");
            configBuilder.put("store-base-dir", "target/store");
            configBuilder.put(Configuration.class.getName(), configuration);
            connectionPool.activate(configBuilder.build());
            return connectionPool;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static JDBCStorageClientPool  getClientPool(Configuration configuration) {
        return getClientPool(configuration, null);
    }

    public synchronized static JDBCStorageClientPool getClientPool(Configuration configuration, String location) {
        if ( clientPool == null ) {
            clientPool = createClientPool(configuration, location);
        }
        return clientPool;
    }

}
