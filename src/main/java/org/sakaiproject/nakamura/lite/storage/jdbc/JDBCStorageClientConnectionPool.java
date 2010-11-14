package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.felix.scr.annotations.Activate;
import org.sakaiproject.nakamura.lite.storage.AbstractClientConnectionPool;
import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCStorageClientConnectionPool extends AbstractClientConnectionPool {

    public static final String CONNECTION_URL = "jdbc-url";
    private static final Logger LOGGER = LoggerFactory
            .getLogger(JDBCStorageClientConnectionPool.class);
    public static final String JDBC_DRIVER = "jdbc-driver";

    public class JCBCStorageClientConnection implements PoolableObjectFactory {

        private String url;
        private Properties connectionProperties;
        private String username;
        private String password;

        public JCBCStorageClientConnection(Map<String, Object> config) {
            connectionProperties = getConnectionProperties(config);
            username = StorageClientUtils.getSetting(config.get("username"), "");
            password = StorageClientUtils.getSetting(config.get("password"), "");
            url = getConnectionUrl(config);
        }

        public void activateObject(Object obj) throws Exception {
            JDBCStorageClient client = (JDBCStorageClient) obj;
            client.activate();
        }

        public void destroyObject(Object obj) throws Exception {
            JDBCStorageClient client = (JDBCStorageClient) obj;
            client.close();

        }

        public Object makeObject() throws Exception {
            if ("".equals(username) ) {
                Connection connection = DriverManager.getConnection(url, connectionProperties);
                return new JDBCStorageClient(connection, properties);                
            } else {
                Connection connection = DriverManager.getConnection(url, username, password);
                return new JDBCStorageClient(connection, properties);
            }
        }

        public void passivateObject(Object obj) throws Exception {
            JDBCStorageClient client = (JDBCStorageClient) obj;
            client.passivate();
        }

        public boolean validateObject(Object obj) {
            JDBCStorageClient client = (JDBCStorageClient) obj;
            return client.validate();
        }

    }

    private Map<String, Object> properties;

    @Activate
    public void activate(Map<String, Object> properties) {
        this.properties = properties;
        super.activate(properties);
        JDBCStorageClient client = null;
        try {
            client = (JDBCStorageClient) openConnection();
            client.checkSchema();
        } catch (ConnectionPoolException e) {
            LOGGER.warn("Failed to check Schema", e);
        } finally {
            try {
                closeConnection();
            } catch (ConnectionPoolException e) {
                LOGGER.warn("Failed to close connection after schema check ",e);
            }
        }

    }

    public String getConnectionUrl(Map<String, Object> config) {
        return (String) config.get(CONNECTION_URL);
    }

    public Properties getConnectionProperties(Map<String, Object> config) {
        Properties connectionProperties = new Properties();
        for (Entry<String, Object> e : config.entrySet()) {
            connectionProperties.put(e.getKey(), e.getValue());
        }
        return connectionProperties;
    }

    @Override
    protected PoolableObjectFactory getConnectionPoolFactory() {
        return new JCBCStorageClientConnection(properties);
    }

}
