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
package org.sakaiproject.nakamura.lite.storage;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

public interface StorageClient {

    Map<String, Object> get(String keySpace, String columnFamily, String key)
            throws StorageClientException;

    void insert(String keySpace, String columnFamily, String key, Map<String, Object> values, boolean probablyNew)
            throws StorageClientException;

    void remove(String keySpace, String columnFamily, String key) throws StorageClientException;

    InputStream streamBodyOut(String keySpace, String columnFamily, String contentId,
            String contentBlockId, String streamId, Map<String, Object> content) throws StorageClientException,
            AccessDeniedException, IOException;

    Map<String, Object> streamBodyIn(String keySpace, String columnFamily, String contentId,
            String contentBlockId, String streamId, Map<String, Object> content, InputStream in)
            throws StorageClientException, AccessDeniedException, IOException;

    Iterator<Map<String, Object>> find(String keySpace, String authorizableColumnFamily,
            Map<String, Object> properties) throws StorageClientException;

    void close();

    DisposableIterator<Map<String, Object>> listChildren(String keySpace, String columnFamily,
            String key) throws StorageClientException;

    boolean hasBody( Map<String, Object> content, String streamId);

}
