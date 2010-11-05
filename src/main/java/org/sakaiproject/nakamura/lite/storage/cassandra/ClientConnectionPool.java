package org.sakaiproject.nakamura.lite.storage.cassandra;

import java.util.Dictionary;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.osgi.service.component.ComponentContext;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, metatype = true)
@Service(value = ConnectionPool.class)
public class ClientConnectionPool implements ConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectionPool.class);
    @Property(intValue = 200)
    private static final String MAX_ACTIVE = "max-active";
    @Property(intValue = 10)
    private static final String MAX_WAIT = "max-wait";
    @Property(intValue = 5)
    private static final String MAX_IDLE = "max-idle";
    @Property(boolValue = true)
    private static final String TEST_ON_BORROW = "test-on-borrow";
    @Property(boolValue = true)
    private static final String TEST_ON_RETURN = "test-on-return";
    @Property(intValue = 60000)
    private static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "time-between-eviction-run";
    @Property(intValue = 1000)
    private static final String NUM_TESTS_PER_EVICTION_RUN = "num-tests-per-eviction-run";
    @Property(intValue = 10000)
    private static final String MIN_EVICTABLE_IDLE_TIME_MILLIS = "min-evictable-idle-time-millis";
    @Property(boolValue = false)
    private static final String TEST_WHILE_IDLE = "test-while-idle";
    @Property(value = { "localhost:9610" })
    private static final String CONNECTION_POOL = "conection-pool";
    @Property(value = "block | fail | grow ")
    private static final String WHEN_EHAUSTED = "when-exhausted-action";

    private ThreadLocal<ClientConnection> boundCLient = new ThreadLocal<ClientConnection>();

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

    private GenericObjectPool pool;

    public ClientConnectionPool() {
    }

    @Activate
    public void activate(ComponentContext componentContext) {
        @SuppressWarnings("unchecked")
        Dictionary<String, Object> properties = componentContext.getProperties();
        int maxActive = (Integer) properties.get(MAX_ACTIVE);
        byte whenExhaustedAction = GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION;
        String whenExhausted = (String) properties.get(WHEN_EHAUSTED);
        if ("fail".equals(whenExhausted)) {
            whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
        } else if ("grow".equals(whenExhausted)) {
            whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
        } else if ("block".equals(whenExhausted)) {
            whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
        }
        long maxWait = (Long) properties.get(MAX_WAIT);
        int maxIdle = (Integer) properties.get(MAX_IDLE);
        boolean testOnBorrow = (Boolean) properties.get(TEST_ON_BORROW);
        boolean testOnReturn = (Boolean) properties.get(TEST_ON_RETURN);
        long timeBetweenEvictionRunsMillis = (Long) properties
                .get(TIME_BETWEEN_EVICTION_RUNS_MILLIS);
        int numTestsPerEvictionRun = (Integer) properties.get(NUM_TESTS_PER_EVICTION_RUN);
        long minEvictableIdleTimeMillis = (Long) properties.get(MIN_EVICTABLE_IDLE_TIME_MILLIS);
        boolean testWhileIdle = (Boolean) properties.get(TEST_WHILE_IDLE);
        String[] connections = (String[]) properties.get(CONNECTION_POOL);
        pool = new GenericObjectPool(new ClientConnectionPoolFactory(connections), maxActive,
                whenExhaustedAction, maxWait, maxIdle, testOnBorrow, testOnReturn,
                timeBetweenEvictionRunsMillis, numTestsPerEvictionRun, minEvictableIdleTimeMillis,
                testWhileIdle);

    }

    @Deactivate
    public void deactivate(ComponentContext componentContext) {
        try {
            pool.close();
        } catch (Exception e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sakaiproject.nakamura.lite.cassandra.ConnectionPool#openConnection()
     */
    public ClientConnection openConnection() throws ConnectionPoolException {
        try {
            ClientConnection clientConnection = boundCLient.get();
            if (clientConnection == null) {
                clientConnection = (ClientConnection) pool.borrowObject();
                boundCLient.set(clientConnection);
            }
            return clientConnection;
        } catch (Exception e) {
            throw new ConnectionPoolException("Failed To Borrow connection from pool ", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sakaiproject.nakamura.lite.cassandra.ConnectionPool#closeConnection()
     */
    public void closeConnection() throws ConnectionPoolException {
        ClientConnection clientConnection = boundCLient.get();
        if (clientConnection != null) {
            try {
                pool.returnObject(clientConnection);
            } catch (Exception e) {
                throw new ConnectionPoolException("Failed To Return connection to pool ", e);
            } finally {
                boundCLient.set(null);
            }
        }
    }
}