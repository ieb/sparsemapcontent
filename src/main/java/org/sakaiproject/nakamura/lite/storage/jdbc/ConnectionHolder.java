/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.lite.storage.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionHolder.class);
    private static final long TTL = 3600000L;
    private static final long validateWait = 120000L;
    private Connection connection;
    private long lastUsed;
    private long lastValidated;

    public ConnectionHolder(Connection connection) {
        this.lastUsed = System.currentTimeMillis();
        this.lastValidated = 0L; // force the connection to get validated, even if its new.
        this.connection = connection;
    }

    public void ping() {
        lastUsed = System.currentTimeMillis();
    }

    public boolean hasExpired() {
        //add validity check
        /*
            if enough time has elapsed
                run validation query
                on any exception return false;
         */
        long now = System.currentTimeMillis();

        if (now > lastValidated + validateWait)
        {
            boolean valid = false;
            try
            {
                valid = connection.isValid(10000);
            }
            catch (Throwable e)
            {
                LOGGER.warn("Error running validation query", e);
            }

            if (!valid) return true;

            lastValidated = now;
        }

        return (now > lastUsed + TTL);
    }

    public Connection get() {
        if (hasExpired()) return null;

        ping();

        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.debug("Failed to close connection " + e.getMessage(), e);
            }
        }
    }

}
