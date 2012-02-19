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
package org.sakaiproject.nakamura.api.lite.accesscontrol;

/**
 * Defines Security Zone Names or Object Types.
 */
public class Security {

    /**
     * Authorizables permissions.
     */
    public static final String ZONE_AUTHORIZABLES = "AU";
    /**
     * Administrative permissions.
     */
    public static final String ZONE_ADMIN = "AD";
    /**
     * Special object path to Admin groups
     */
    public static final String ADMIN_GROUPS = "GR";
    /**
     * Special object path to Admin users
     */
    public static final String ADMIN_USERS = "US";
    /**
     * Content permissions
     */
    public static final String ZONE_CONTENT = "CO";
    /**
     * Special object path to admin all authorizables.
     */
    public static final String ADMIN_AUTHORIZABLES = "AA";

}
