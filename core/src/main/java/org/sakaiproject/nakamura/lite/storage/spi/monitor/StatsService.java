package org.sakaiproject.nakamura.lite.storage.spi.monitor;

/**
 * Gathers statistics. Implementations must be thread safe, fast and non blocking.
 * @author ieb
 *
 */
public interface StatsService {

    /**
     * Login.
     */
    void sessionLogin();

    /**
     * Login failed.
     */
    void sessionFailLogin();

    /**
     * Session logout.
     */
    void sessionLogout();

    /**
     * Slow storage operation reported.
     * @param columnFamily
     * @param type
     * @param t
     * @param operation
     */
    void slowStorageOp(String columnFamily, String type, long t, String operation);

    /**
     * Storage operation
     * @param columnFamily
     * @param type
     * @param t
     */
    void storageOp(String columnFamily, String type, long t);

    /**
     * Top level api call.
     * @param className
     * @param methodName
     * @param t
     */
    void apiCall(String className, String methodName, long t);


}
