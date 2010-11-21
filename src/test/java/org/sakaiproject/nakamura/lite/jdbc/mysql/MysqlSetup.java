package org.sakaiproject.nakamura.lite.jdbc.mysql;

import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClientConnectionPool;

import com.google.common.collect.ImmutableMap;

public class MysqlSetup {

    private static JDBCStorageClientConnectionPool connectionPool = createConnectionPool();

    public synchronized static JDBCStorageClientConnectionPool createConnectionPool() {
        try {
            JDBCStorageClientConnectionPool connectionPool = new JDBCStorageClientConnectionPool();
            connectionPool.activate(ImmutableMap.of(JDBCStorageClientConnectionPool.CONNECTION_URL,
                    (Object) "jdbc:mysql://127.0.0.1:3306/sakai22?useUnicode=true&amp;characterEncoding=UTF-8",
                    JDBCStorageClientConnectionPool.JDBC_DRIVER,
                    "com.mysql.jdbc.Driver",
                    "username","sakai22",
                    "password","sakai22"));
        return connectionPool;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static JDBCStorageClientConnectionPool getConnectionPool() {
        return connectionPool;
    }
    

}
