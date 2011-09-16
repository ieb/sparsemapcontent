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

package org.sakaiproject.nakamura.api.lite;

import java.util.Map;

/**
 * An Interface to define configuration for the sparse content store.
 */
public interface Configuration {

    /**
     * @return name of the column family used for Acl Storage, in an RDBMS
     *         environment might be used to form the basis for tables storing
     *         ACls.
     */
    String getAclColumnFamily();

    /**
     * @return name of the Keyspace used by the sparse content store. In an
     *         RDBMS environment could be used as the tablespace name.
     */
    String getKeySpace();

    /**
     * @return name of the column family used for Authorizable Storage, in an
     *         RDBMS environment might be used to form the basis for tables
     *         storing Authorizables.
     */
    String getAuthorizableColumnFamily();

    /**
     * @return name of the column family used for Content Storage, in an
     *         RDBMS environment might be used to form the basis for tables
     *         storing Content objects.
     */
    String getContentColumnFamily();

    /**
     * @return name of the lock column family.
     */
    String getLockColumnFamily();
    
   /**
     * @return the config, shared by all drivers.
     */
    Map<String, String> getSharedConfig();

    /**
     * @return an array of properties names that should be indexed.
     */
    String[] getIndexColumnNames();

    /**
     * 
     * @return an array of index column types
     */
    String[] getIndexColumnTypes();

}
