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

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

public interface Repository {

    Session login(String username, String password) throws ClientPoolException,
            StorageClientException, AccessDeniedException;

    Session login() throws ClientPoolException, StorageClientException, AccessDeniedException;

    Session loginAdministrative() throws ClientPoolException, StorageClientException,
            AccessDeniedException;

    Session loginAdministrative(String username) throws ClientPoolException,
            StorageClientException, AccessDeniedException;

}
