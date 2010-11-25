package org.sakaiproject.nakamura.lite.jdbc.mysql;

import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClientPool;

import com.google.common.collect.ImmutableMap;

public class MysqlSetup {

    private static JDBCStorageClientPool clientPool = createClientPool();

    public synchronized static JDBCStorageClientPool createClientPool() {
        try {
            JDBCStorageClientPool connectionPool = new JDBCStorageClientPool();
            connectionPool.activate(ImmutableMap.of(JDBCStorageClientPool.CONNECTION_URL,
                    (Object) "jdbc:mysql://127.0.0.1:3306/sakai22?useUnicode=true&amp;characterEncoding=UTF-8",
                    JDBCStorageClientPool.JDBC_DRIVER,
                    "com.mysql.jdbc.Driver",
                    "username","sakai22",
                    "password","sakai22"));
        return connectionPool;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static JDBCStorageClientPool getClientPool() {
        return clientPool;
    }
    

}
