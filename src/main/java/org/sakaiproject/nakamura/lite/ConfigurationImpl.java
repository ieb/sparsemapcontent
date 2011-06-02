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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;

import java.util.Map;

@Component(immediate = true, metatype = true)
@Service(value = Configuration.class)
public class ConfigurationImpl implements Configuration {

    @Property(value = "ac")
    private static final String ACL_COLUMN_FAMILY = "acl-column-family";
    @Property(value = "n")
    private static final String KEYSPACE = "keyspace";
    @Property(value = "au")
    private static final String AUTHORIZABLE_COLUMN_FAMILY = "authorizable-column-family";
    @Property(value = "cn")
    private static final String CONTENT_COLUMN_FAMILY = "content-column-family";

    private String aclColumnFamily;
    private String keySpace;
    private String authorizableColumnFamily;
    private String contentColumnFamily;

    @Activate
    public void activate(Map<String, Object> properties) {
        aclColumnFamily = StorageClientUtils.getSetting(properties.get(ACL_COLUMN_FAMILY), "ac");
        keySpace = StorageClientUtils.getSetting(properties.get(KEYSPACE),"n");
        authorizableColumnFamily = StorageClientUtils.getSetting(properties.get(AUTHORIZABLE_COLUMN_FAMILY),"au");
        contentColumnFamily = StorageClientUtils.getSetting(properties.get(CONTENT_COLUMN_FAMILY),"cn");
    }

    public String getAclColumnFamily() {
        return aclColumnFamily;
    }

    public String getKeySpace() {
        return keySpace;
    }

    public String getAuthorizableColumnFamily() {
        return authorizableColumnFamily;
    }

    public String getContentColumnFamily() {
        return contentColumnFamily;
    }
}
