package org.sakaiproject.nakamura.lite.storage.cassandra;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.sakaiproject.nakamura.lite.storage.AbstractClientConnectionPool;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, metatype = true)
@Service(value = ConnectionPool.class)
public class ClientConnectionPool extends AbstractClientConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectionPool.class);
    @Property(value = { "localhost:9610" })
    private static final String CONNECTION_POOL = "conection-pool";

    public static class ClientConnectionPoolFactory extends BasePoolableObjectFactory {

        private String[] hosts;
        private int[] ports;
        private int savedLastHost = 0;

        public ClientConnectionPoolFactory(String[] connections) {
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
                    lastHost = i;
                    break;
                } catch (Exception ex) {

                }
            }
            if (startHost == lastHost) {
                for (int i = 0; i <= startHost; i++) {
                    try {
                        tSocket = new TSocket(hosts[i], ports[i]);
                        tSocket.open();
                        lastHost = i;
                        break;
                    } catch (Exception ex) {

                    }
                }
            }
            savedLastHost = lastHost;
            TProtocol tProtocol = new TBinaryProtocol(tSocket);
            ClientConnection clientConnection = new ClientConnection(tProtocol, tSocket);
            return clientConnection;
        }

        @Override
        public void passivateObject(Object obj) throws Exception {
            ClientConnection clientConnection = (ClientConnection) obj;
            clientConnection.passivate();
            super.passivateObject(obj);
        }

        @Override
        public void activateObject(Object obj) throws Exception {
            ClientConnection clientConnection = (ClientConnection) obj;
            clientConnection.activate();
            super.activateObject(obj);
        }

        @Override
        public void destroyObject(Object obj) throws Exception {
            ClientConnection clientConnection = (ClientConnection) obj;
            clientConnection.destroy();
        }

        @Override
        public boolean validateObject(Object obj) {
            ClientConnection clientConnection = (ClientConnection) obj;
            try {
                clientConnection.validate();
            } catch (TException e) {
                LOGGER.debug("Failed to validate connection " + e.getMessage(), e);
                return false;
            }
            return super.validateObject(obj);
        }

    }

    private String[] connections;

    public ClientConnectionPool() {
    }

    @Activate
    public void activate(Map<String, Object> properties) {
        connections = (String[]) properties.get(CONNECTION_POOL);
        super.activate(properties);

    }



    @Override
    protected PoolableObjectFactory getConnectionPoolFactory() {
        return new ClientConnectionPoolFactory(connections);
    }
}