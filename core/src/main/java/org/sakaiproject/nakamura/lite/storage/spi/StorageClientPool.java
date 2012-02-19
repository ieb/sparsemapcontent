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
package org.sakaiproject.nakamura.lite.storage.spi;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.StorageCacheManager;

/**
 * This is the main SPI of the Storage SPI. This should be implemented as an OSGi service.
 * @author ieb
 *
 */
public interface StorageClientPool {

    /**
     * @return the connection bound to this thread, or one that was just opened.
     * @throws ClientPoolException
     */
    StorageClient getClient() throws ClientPoolException;

    /**
     * @return the object cache for this datastore.
     */
    StorageCacheManager getStorageCacheManager();

}
