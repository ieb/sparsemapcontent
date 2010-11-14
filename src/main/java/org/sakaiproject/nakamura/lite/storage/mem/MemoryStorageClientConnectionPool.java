package org.sakaiproject.nakamura.lite.storage.mem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.lite.storage.AbstractClientConnectionPool;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;

@Component(immediate = true, metatype = true)
@Service(value = ConnectionPool.class)
public class MemoryStorageClientConnectionPool extends AbstractClientConnectionPool {

    public static class ClientConnectionPoolFactory extends BasePoolableObjectFactory {


        private Map<String, Map<String, Object>> store;
        private Map<String, Object> properties;

        public ClientConnectionPoolFactory(Map<String, Map<String, Object>> store, Map<String, Object> properties) {
           this.store = store;
           this.properties = properties;
        }

        @Override
        public Object makeObject() throws Exception {
            MemoryStorageClient client = new MemoryStorageClient(store, properties);
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

    private Map<String, Map<String, Object>> store;
    private Map<String, Object> properties;

    public MemoryStorageClientConnectionPool() {
    }

    @Activate
    public void activate(Map<String, Object> properties) {
        this.properties = properties;
        store = new ConcurrentHashMap<String, Map<String,Object>>();
        super.activate(properties);
    }

    @Deactivate
    public void deactivate(Map<String, Object> properties) {
        super.deactivate(properties);
        store = null;
    }

    @Override
    protected PoolableObjectFactory getConnectionPoolFactory() {
        return new ClientConnectionPoolFactory(store, properties);
    }

}