package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.sql.Connection;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager extends TimerTask {

    private Map<Thread, ConnectionHolder> threadMap = new ConcurrentHashMap<Thread, ConnectionHolder>();

    @Override
    public void run() {
        cleanThreadMap();
    }

    public Connection get() {
        Thread t = Thread.currentThread();
        ConnectionHolder ch = threadMap.get(t);
        if (ch != null && ch.get() != null) {
            ch.ping();
            return ch.get();
        }
        return null;
    }

    public void set(Connection connection) {
        cleanThreadMap();
        Thread t = Thread.currentThread();
        ConnectionHolder c = threadMap.get(t);
        if (c != null) {
            c.close();
            threadMap.remove(t);
        }
        ConnectionHolder ch = new ConnectionHolder(connection);
        threadMap.put(t, ch);
    }

    private void cleanThreadMap() {
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
        while(threadMap.size() > 0 ) {
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
