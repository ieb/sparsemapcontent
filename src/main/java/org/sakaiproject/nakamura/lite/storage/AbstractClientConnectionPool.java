package org.sakaiproject.nakamura.lite.storage;

import java.util.Map;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.sakaiproject.nakamura.api.lite.ConnectionPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(componentAbstract=true)
public abstract class AbstractClientConnectionPool implements ConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientConnectionPool.class);

    @Property(intValue = 200)
    private static final String MAX_ACTIVE = "max-active";
    @Property(longValue = 10)
    private static final String MAX_WAIT = "max-wait";
    @Property(intValue = 5)
    private static final String MAX_IDLE = "max-idle";
    @Property(boolValue = true)
    private static final String TEST_ON_BORROW = "test-on-borrow";
    @Property(boolValue = true)
    private static final String TEST_ON_RETURN = "test-on-return";
    @Property(longValue = 60000)
    private static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "time-between-eviction-run";
    @Property(intValue = 1000)
    private static final String NUM_TESTS_PER_EVICTION_RUN = "num-tests-per-eviction-run";
    @Property(longValue = 10000)
    private static final String MIN_EVICTABLE_IDLE_TIME_MILLIS = "min-evictable-idle-time-millis";
    @Property(boolValue = false)
    private static final String TEST_WHILE_IDLE = "test-while-idle";
    @Property(value = "block | fail | grow ")
    private static final String WHEN_EHAUSTED = "when-exhausted-action";

    private ThreadLocal<StorageClient> boundCLient = new ThreadLocal<StorageClient>();

    private GenericObjectPool pool;

    public AbstractClientConnectionPool() {
    }

    @Activate
    public void activate(Map<String, Object> properties) throws ClassNotFoundException {
        int maxActive = getProperty(properties.get(MAX_ACTIVE),200);
        byte whenExhaustedAction = GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION;
        String whenExhausted = (String) properties.get(WHEN_EHAUSTED);
        if ("fail".equals(whenExhausted)) {
            whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
        } else if ("grow".equals(whenExhausted)) {
            whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
        } else if ("block".equals(whenExhausted)) {
            whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
        }
        long maxWait = getProperty(properties.get(MAX_WAIT), 10L);
        int maxIdle = getProperty(properties.get(MAX_IDLE), 5);
        boolean testOnBorrow = getProperty(properties.get(TEST_ON_BORROW), true);
        boolean testOnReturn = getProperty(properties.get(TEST_ON_RETURN), true);
        long timeBetweenEvictionRunsMillis = getProperty(
                properties.get(TIME_BETWEEN_EVICTION_RUNS_MILLIS), 60000L);
        int numTestsPerEvictionRun = getProperty(properties.get(NUM_TESTS_PER_EVICTION_RUN), 1000);
        long minEvictableIdleTimeMillis = getProperty(properties.get(MIN_EVICTABLE_IDLE_TIME_MILLIS), 10000L);
        boolean testWhileIdle = getProperty(properties.get(TEST_WHILE_IDLE), false);

        pool = new GenericObjectPool(getConnectionPoolFactory(), maxActive, whenExhaustedAction,
                maxWait, maxIdle, testOnBorrow, testOnReturn, timeBetweenEvictionRunsMillis,
                numTestsPerEvictionRun, minEvictableIdleTimeMillis, testWhileIdle);

    }

    @SuppressWarnings("unchecked")
    private <T> T getProperty(Object o, T l) {
        if (o == null) {
            return l;
        }
        return (T) o;
    }

    protected abstract PoolableObjectFactory getConnectionPoolFactory();

    @Deactivate
    public void deactivate(Map<String, Object> properties) {
        try {
            pool.clear();
            pool.close();
            LOGGER.info("Sparse Map Content client pool closed ");
        } catch (Exception e) {
            LOGGER.error("Failed to close pool ",e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sakaiproject.nakamura.lite.cassandra.ConnectionPool#openConnection()
     */
    public StorageClient openConnection() throws ConnectionPoolException {
        try {
            StorageClient clientConnection = boundCLient.get();
            if (clientConnection == null) {
                clientConnection = (StorageClient) pool.borrowObject();
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
        StorageClient clientConnection = boundCLient.get();
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