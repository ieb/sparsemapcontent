package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionHolder.class);
    private static final long TTL = 3600000L;
    private Connection connection;
    private long lastUsed;

    public ConnectionHolder(Connection connection) {
        this.lastUsed = System.currentTimeMillis();
        this.connection = connection;
    }

    public void ping() {
        lastUsed = System.currentTimeMillis();
    }
    
    public boolean hasExpired() {
        return (System.currentTimeMillis() > lastUsed+TTL);
    }

    public Connection get() {
        return connection;
    }

    public void close() {
        if ( connection != null ) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.debug("Failed to close connection "+e.getMessage(),e);
            }
        }
    }

}
