package org.sakaiproject.nakamura.lite.storage.spi.monitor;

public interface StatsServiceFactory {

    /**
     * return a StatsService implementation for the lifetime for the session.
     * @return
     */
    StatsService openSession();


}
