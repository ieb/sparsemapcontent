package org.sakaiproject.nakamura.lite.storage.cassandra;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.lite.accesscontrol.CacheHolder;
import org.sakaiproject.nakamura.lite.storage.AbstractClientConnectionPool;
import org.sakaiproject.nakamura.lite.storage.ConcurrentLRUMap;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(enabled=false, metatype = true, inherit=true)
@Service(value = StorageClientPool.class)
public class CassandraClientPool extends AbstractClientConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraClientPool.class);
    @Property(value = { "localhost:9610" })
    private static final String CONNECTION_POOL = "conection-pool";

    public static class ClientConnectionPoolFactory extends BasePoolableObjectFactory {

        private String[] hosts;
        private int[] ports;
        private int savedLastHost = 0;
        private Map<String, Object> properties;
        private CassandraClientPool pool;

        public ClientConnectionPoolFactory(CassandraClientPool pool, String[] connections, Map<String, Object> properties) {
            this.properties = properties;
            this.pool = pool;
            hosts = new String[connections.length];
            ports = new int[connections.length];
            int i = 0;
            for (String connection : connections) {
                String[] spec = StringUtils.split(connection, ':');
                hosts[i] = spec[0];
                ports[i] = Integer.parseInt(spec[1]);
                i++;
            }
        }

        @Override
        public Object makeObject() throws Exception {
            TSocket tSocket = null;
            int lastHost = savedLastHost;
            int startHost = lastHost;

            for (int i = lastHost + 1; i < hosts.length; i++) {
                try {
                    tSocket = new TSocket(hosts[i], ports[i]);
                    tSocket.open();
                    LOGGER.debug("Opened connction to {} {} ",hosts[i], ports[i]);
                    lastHost = i;
                    break;
                } catch (Exception ex) {
                    LOGGER.warn("Failed to open connection to host "+hosts[i]+" on port "+ports[i]+" cause:"+ex.getMessage());
                    tSocket = null;
                }
            }
            if (startHost == lastHost) {
                for (int i = 0; i <= startHost; i++) {
                    try {
                        tSocket = new TSocket(hosts[i], ports[i]);
                        tSocket.open();
                        LOGGER.debug("Opened connction to {} {} ",hosts[i], ports[i]);
                        lastHost = i;
                        break;
                    } catch (Exception ex) {
                        LOGGER.warn("Failed to open connection to host "+hosts[i]+" on port "+ports[i]+" cause:"+ex.getMessage());
                        tSocket = null;
                    }
                }
            }
            if ( tSocket == null ) {
                LOGGER.error("Unable to connect to any Cassandra Hosts");
                throw new StorageClientException("Unable to connect to any Cassandra Clients, tried all known locations");
            }
            savedLastHost = lastHost;
            TProtocol tProtocol = new TBinaryProtocol(tSocket);
            LOGGER.debug("Opened Connection {} isOpen {} Host {} Port {}",tSocket, tSocket.isOpen());
            CassandraClient clientConnection = new CassandraClient( pool, tProtocol, tSocket, properties);
            return clientConnection;
        }

        @Override
        public void passivateObject(Object obj) throws Exception {
            CassandraClient clientConnection = (CassandraClient) obj;
            clientConnection.passivate();
            super.passivateObject(obj);
        }

        @Override
        public void activateObject(Object obj) throws Exception {
            CassandraClient clientConnection = (CassandraClient) obj;
            clientConnection.activate();
            super.activateObject(obj);
        }

        @Override
        public void destroyObject(Object obj) throws Exception {
            CassandraClient clientConnection = (CassandraClient) obj;
            clientConnection.destroy();
        }

        @Override
        public boolean validateObject(Object obj) {
            CassandraClient clientConnection = (CassandraClient) obj;
            try {
                clientConnection.validate();
            } catch (TException e) {
                LOGGER.error("Failed to validate connection " + e.getMessage(), e);
                return false;
            }
            return super.validateObject(obj);
        }

    }

    private String[] connections;
    private Map<String, Object> properties;
    private Map<String, CacheHolder> sharedCache;

    public CassandraClientPool() {
    }

    @Activate
    public void activate(Map<String, Object> properties) throws ClassNotFoundException {
        connections = StorageClientUtils.getSetting(properties.get(CONNECTION_POOL), new String[] { "localhost:9160" });
        this.properties = properties;
        super.activate(properties);
        // this should come from the memory service ultimately.
        sharedCache = new ConcurrentLRUMap<String, CacheHolder>(10000);


    }

    @Deactivate
    public void deactivate(Map<String, Object> properties) {
        super.deactivate(properties);
    }



    @Override
    protected PoolableObjectFactory getConnectionPoolFactory() {
        return new ClientConnectionPoolFactory(this, connections, properties);
    }

    @Override
    public Map<String, CacheHolder> getSharedCache() {
        return sharedCache;
    }
}