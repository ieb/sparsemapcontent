package org.sakaiproject.nakamura.lite.storage.jdbc.mysql;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClientPool;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;

@Component(immediate = true, metatype = true, inherit = true)
@Service(value = StorageClientPool.class)
public class MySQLJDBCStorageClientPool extends JDBCStorageClientPool {

    @Property(value = { "jdbc:mysql://127.0.0.1:3306/sakai22?useUnicode=true&amp;characterEncoding=UTF-8" })
    public static final String CONNECTION_URL = JDBCStorageClientPool.CONNECTION_URL;
    @Property(value = { "com.mysql.jdbc.Driver" })
    public static final String JDBC_DRIVER = JDBCStorageClientPool.JDBC_DRIVER;

    @Property(value = { "sakai22" })
    public static final String USERNAME = JDBCStorageClientPool.USERNAME;
    @Property(value = { "sakai22" })
    public static final String PASSWORD = JDBCStorageClientPool.PASSWORD;

}
