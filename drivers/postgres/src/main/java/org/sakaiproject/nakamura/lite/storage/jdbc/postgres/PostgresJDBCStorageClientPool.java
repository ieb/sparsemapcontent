package org.sakaiproject.nakamura.lite.storage.jdbc.postgres;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClientPool;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;

@Component(immediate = true, metatype = true, inherit = true)
@Service(value = StorageClientPool.class)
public class PostgresJDBCStorageClientPool extends JDBCStorageClientPool {

    @Property(value = { "jdbc:postgresql://localhost/nak" })
    public static final String CONNECTION_URL = JDBCStorageClientPool.CONNECTION_URL;
    @Property(value = { "org.postgresql.Driver" })
    public static final String JDBC_DRIVER = JDBCStorageClientPool.JDBC_DRIVER;

    @Property(value = { "nakamura" })
    public static final String USERNAME = JDBCStorageClientPool.USERNAME;
    @Property(value = { "nakamura" })
    public static final String PASSWORD = JDBCStorageClientPool.PASSWORD;

}
