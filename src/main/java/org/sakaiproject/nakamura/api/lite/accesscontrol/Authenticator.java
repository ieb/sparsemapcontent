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

import org.sakaiproject.nakamura.api.lite.authorizable.User;

/**
 * Authenticates a user
 * @since 1.0
 */
public interface Authenticator {

    /**
     * Gets a User object if the userid and password are valid.
     * 
     * @param userid
     *            the userid
     * @param password
     *            password for the user
     * @return the user object for the user or null if the authentication
     *         attempt is not valid.
     * @since 1.0
     */
    User authenticate(String userid, String password);

    /**
     * perform a system authentication, trusting the userId.
     * @param userid
     * @return the User object if the userID exists.
     * @since 1.0
     */
    User systemAuthenticate(String userid);

    /**
     * perform a system authentication bypassing enable login checks
     * @param userid
     * @return
     * @since 1.4
     */
    User systemAuthenticateBypassEnable(String userid);

}
