package org.sakaiproject.nakamura.lite.storage.jdbc.oracle;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClientPool;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;

@Component(immediate = true, metatype = true, inherit = true)
@Service(value = StorageClientPool.class)
public class OracleJDBCStorageClientPool extends JDBCStorageClientPool {

    @Property(value = { "jdbc:oracle:thin:@172.16.41.128:1521:XE" })
    public static final String CONNECTION_URL = JDBCStorageClientPool.CONNECTION_URL;
    @Property(value = { "oracle.jdbc.driver.OracleDriver" })
    public static final String JDBC_DRIVER = JDBCStorageClientPool.JDBC_DRIVER;

    @Property(value = { "sakai22" })
    public static final String USERNAME = JDBCStorageClientPool.USERNAME;
    @Property(value = { "sakai22" })
    public static final String PASSWORD = JDBCStorageClientPool.PASSWORD;

}
