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

import java.io.IOException;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.ConfigurationImpl;
import org.sakaiproject.nakamura.lite.jdbc.mysql.MysqlSetup;
import org.sakaiproject.nakamura.lite.soak.AbstractSoakController;
import org.sakaiproject.nakamura.lite.soak.content.ContentCreateClient;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;

import com.google.common.collect.Maps;

public class ContentCreateSoak extends AbstractSoakController {

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
                totalContent, MysqlSetup.getClientPool(configuration), configuration, cm);
        contentCreateSoak.launchSoak(nthreads);
        
        
    }



}
