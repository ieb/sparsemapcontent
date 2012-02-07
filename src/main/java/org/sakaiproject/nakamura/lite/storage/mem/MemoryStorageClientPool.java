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
package org.sakaiproject.nakamura.lite.storage.mem;

import java.util.Map;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.StorageCacheManager;
import org.sakaiproject.nakamura.lite.storage.spi.AbstractClientConnectionPool;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;

import com.google.common.collect.Maps;

@Component(enabled = false, metatype = true, inherit = true)
@Service(value = StorageClientPool.class)
public class MemoryStorageClientPool extends AbstractClientConnectionPool {

    public static class ClientConnectionPoolFactory extends BasePoolableObjectFactory {

        private Map<String, Object> store;
        private Map<String, Object> properties;
        private MemoryStorageClientPool pool;

        public ClientConnectionPoolFactory(MemoryStorageClientPool pool,
                Map<String, Object> store, Map<String, Object> properties) {
            this.store = store;
            this.pool = pool;
            this.properties = properties;
        }

        @Override
        public Object makeObject() throws Exception {
            MemoryStorageClient client = new MemoryStorageClient(pool, store, properties);
            return client;
        }

        @Override
        public void passivateObject(Object obj) throws Exception {
            super.passivateObject(obj);
        }

        @Override
        public void activateObject(Object obj) throws Exception {
            super.activateObject(obj);
        }

        @Override
        public void destroyObject(Object obj) throws Exception {
            MemoryStorageClient client = (MemoryStorageClient) obj;
            client.destroy();
        }

        @Override
        public boolean validateObject(Object obj) {
            return super.validateObject(obj);
        }

    }

    private Map<String, Object> store;
    private Map<String, Object> properties;
    
    public MemoryStorageClientPool() {
    }

    @Activate
    public void activate(Map<String, Object> properties) throws ClassNotFoundException {
        this.properties = properties;
        store = Maps.newConcurrentMap();
        super.activate(properties);
    }

    @Deactivate
    public void deactivate(Map<String, Object> properties) {
        super.deactivate(properties);
        store = null;
    }

    @Override
    protected PoolableObjectFactory getConnectionPoolFactory() {
        return new ClientConnectionPoolFactory(this, store, properties);
    }

    
    public StorageCacheManager getStorageCacheManager() {
        return null;
    }


}