package org.sakaiproject.nakamura.lite.jdbc.derby;

import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClientConnectionPool;

import com.google.common.collect.ImmutableMap;

public class DerbySetup {

    private static JDBCStorageClientConnectionPool connectionPool = createConnectionPool();

    private static JDBCStorageClientConnectionPool createConnectionPool() {
        try {
            JDBCStorageClientConnectionPool connectionPool = new JDBCStorageClientConnectionPool();
            connectionPool.activate(ImmutableMap
                    .of(JDBCStorageClientConnectionPool.CONNECTION_URL,
                            (Object) "jdbc:derby:memory:MyDB;create=true",
                            JDBCStorageClientConnectionPool.JDBC_DRIVER,
                            "org.apache.derby.jdbc.EmbeddedDriver"));
            return connectionPool;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static JDBCStorageClientConnectionPool getConnectionPool() {
        return connectionPool;
    }

}
