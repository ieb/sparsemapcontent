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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.ConfigurationImpl;
import org.sakaiproject.nakamura.lite.jdbc.derby.DerbySetup;
import org.sakaiproject.nakamura.lite.soak.AbstractSoakController;
import org.sakaiproject.nakamura.lite.soak.content.ContentCreateClient;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class ContentCreateSoak extends AbstractSoakController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentCreateSoak.class);
    private int totalContent;
    private StorageClientPool connectionPool;
    private Configuration configuration;
    private Map<String, Object> contentMap;

    public ContentCreateSoak(int totalContent,
            StorageClientPool connectionPool, Configuration configuration, Map<String, Object> cm) {
        super(totalContent);
        this.configuration = configuration;
        this.connectionPool = connectionPool;
        this.totalContent = totalContent;
        this.contentMap = cm;
    }

    protected Runnable getRunnable(int nthreads) throws ClientPoolException,
            StorageClientException, AccessDeniedException {
        int contentPerThread = totalContent / nthreads;
        return new ContentCreateClient(contentPerThread,
                connectionPool, configuration, contentMap);
    }

    public static void main(String[] argv) throws ClientPoolException, StorageClientException,
            AccessDeniedException, ClassNotFoundException, IOException {

        int totalContent = 100000;
        int nthreads = 1;

        if (argv.length > 0) {
            nthreads = StorageClientUtils.getSetting(Integer.valueOf(argv[0]), nthreads);
        }
        if (argv.length > 1) {
            totalContent = StorageClientUtils.getSetting(Integer.valueOf(argv[1]), totalContent);
        }
        ConfigurationImpl configuration = new ConfigurationImpl();
        Map<String, Object> cm = Maps.newHashMap();
        cm.put("sling:resourceType","test/resourcetype");
        cm.put("sakai:pooled-content-manager",new String[]{"a","b"});
        cm.put("sakai:type","sdfsdaggdsfgsdgsd");
        cm.put("sakai:marker","marker-marker-marker");
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("keyspace", "n");
        properties.put("acl-column-family", "ac");
        properties.put("authorizable-column-family", "au");
        properties.put("content-column-family", "cn");
        configuration.activate(properties);

        ContentCreateSoak contentCreateSoak = new ContentCreateSoak(
                totalContent, DerbySetup.getClientPool(configuration,"jdbc:derby:target/soak/db;create=true"), configuration, cm);
        contentCreateSoak.launchSoak(nthreads);
        contentCreateSoak.shutdown();
        
        
    }

    private void shutdown() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:derby:target/soak/db;shutdown=true");
        } catch (SQLException e) {
            // yes really see
            // http://db.apache.org/derby/manuals/develop/develop15.html#HDRSII-DEVELOP-40464
            LOGGER.info("Sparse Map Content Derby Embedded instance shutdown sucessfully {}",
                    e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.debug(
                            "Very Odd, the getConnection should not have opened a connection (see DerbyDocs),"
                                    + " but it did, and when we tried to close it we got  "
                                    + e.getMessage(), e);
                }
            }
        }
    }


}
