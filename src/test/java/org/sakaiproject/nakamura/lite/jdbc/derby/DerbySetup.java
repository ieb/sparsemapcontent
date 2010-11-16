package org.sakaiproject.nakamura.lite.jdbc.derby;

import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClientConnectionPool;

import com.google.common.collect.ImmutableMap;

public class DerbySetup {

    private static JDBCStorageClientConnectionPool connectionPool;

    public static ConnectionPool getConnectionPool() throws ClassNotFoundException {
        if (connectionPool == null) {
            connectionPool = new JDBCStorageClientConnectionPool();
            connectionPool.activate(ImmutableMap.of(JDBCStorageClientConnectionPool.CONNECTION_URL,
                    (Object) "jdbc:derby:memory:MyDB;create=true",
                    JDBCStorageClientConnectionPool.JDBC_DRIVER,
                    "org.apache.derby.jdbc.EmbeddedDriver"));
        }
        return connectionPool;
    }

}
