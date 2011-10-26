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

import java.sql.Connection;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager extends TimerTask {

    private Map<Thread, ConnectionHolder> threadMap = new ConcurrentHashMap<Thread, ConnectionHolder>();
    private boolean closing = false;
    private JDBCStorageClientPool jdbcStorageClientPool;
    

    public ConnectionManager(JDBCStorageClientPool jdbcStorageClientPool) {
        this.jdbcStorageClientPool = jdbcStorageClientPool;
    }

    @Override
    public void run() {
        cleanThreadMap();
    }

    public Connection get() {
        if ( closing ) {
            return null;
        }
        Thread t = Thread.currentThread();
        ConnectionHolder ch = threadMap.get(t);
        if (ch != null) {
            return ch.get();
        }
        return null;
    }

    public void set(Connection connection) {
        if ( closing ) {
            throw new IllegalStateException("ConnectionManager is closing ");
        }
        cleanThreadMap();
        Thread t = Thread.currentThread();
        ConnectionHolder c = threadMap.get(t);
        if (c != null) {
            c.close();
            threadMap.remove(t);
        }
        ConnectionHolder ch = new ConnectionHolder(connection, jdbcStorageClientPool);
        threadMap.put(t, ch);
    }

    private void cleanThreadMap() {
        if ( closing ) {
            return;
        }
        Thread[] copy = threadMap.keySet().toArray(new Thread[threadMap.size()]);
        for (Thread t : copy) {
            if (!t.isAlive()) {
                ConnectionHolder ch = threadMap.remove(t);
                if (ch != null) {
                    ch.close();
                }
            } else {
                ConnectionHolder ch = threadMap.get(t);
                if (ch != null && ch.hasExpired()) {
                    ch = threadMap.remove(t);
                    if (ch != null) {
                        ch.close();
                    }

                }
            }
        }
    }

    public void close() {
        closing = true;
        while (threadMap.size() > 0) {
            
            Thread[] copy = threadMap.keySet().toArray(new Thread[threadMap.size()]);
            for (Thread t : copy) {
                ConnectionHolder ch = threadMap.remove(t);
                if (ch != null) {
                    ch.close();
                }
            }
        }
        threadMap.clear();
        threadMap = null;
    }

}
