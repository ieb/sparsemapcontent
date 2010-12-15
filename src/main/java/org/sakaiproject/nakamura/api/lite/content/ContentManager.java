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
package org.sakaiproject.nakamura.api.lite.content;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

import java.io.IOException;
import java.io.InputStream;

public interface ContentManager {

    Content get(String path) throws StorageClientException, AccessDeniedException;

    void saveVersion(String path) throws StorageClientException, AccessDeniedException;

    void update(Content content) throws AccessDeniedException, StorageClientException;

    void delete(String path) throws AccessDeniedException, StorageClientException;

    long writeBody(String path, InputStream in) throws StorageClientException,
            AccessDeniedException, IOException;

    InputStream getInputStream(String path) throws StorageClientException, AccessDeniedException,
            IOException;

    long writeBody(String path, InputStream in, String streamId) throws StorageClientException,
            AccessDeniedException, IOException;

    InputStream getInputStream(String path, String streamId) throws StorageClientException,
            AccessDeniedException, IOException;

    String getAltField(String field, String streamId);

}
