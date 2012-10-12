package org.sakaiproject.nakamura.lite.storage.spi.monitor;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * This is a small session bound monitor that allows fast collection of stats with no blocking of synchronisation.
 * Its used inside a session to collect information. On logout the session data is stored in an accumulator. This avoids
 * all of the issues with monitors that rely on locking and synchronisation to work.
 * @author ieb
 *
 */
public class StatsServiceImpl implements StatsService {

    Map<String, long[]> slowStorageOps = Maps.newConcurrentMap();
    Map<String, long[]> storageOps = Maps.newConcurrentMap();
    Map<String, long[]> apiCalls = Maps.newConcurrentMap();
    private boolean capture;
	private StatsServiceFactory statsServiceFactory;
	
	// monitors. no need for roll over since this is a per session object
    protected long ncalls;
    protected long op;
    protected long slowOp;
    protected long api;
    protected long login;
    protected long loginFailed;
    protected long logout;


    /**
     * Called to create the session bound stats service.
     * @param statsServiceParent
     * @param capture
     * @param dumpApp
     * @param dumpSession
     */
    public StatsServiceImpl(StatsServiceFactory statsServiceFactory, boolean capture) {
        this.statsServiceFactory = statsServiceFactory;
        this.capture = capture;
    }


    @Override
    public void sessionLogin() {
        login++;
    }

    @Override
    public void sessionFailLogin() {
        if (capture) {
            loginFailed++;
            ((StatsServiceFactroyImpl) statsServiceFactory).save(this);
        }
    }

    @Override
    public void sessionLogout() {
        if (capture) {
            logout++;
            ((StatsServiceFactroyImpl) statsServiceFactory).save(this);
        }
    }

    @Override
    public void slowStorageOp(String columnFamily, String type, long t, String operation) {
        if (capture) {
            slowOp++;
            add(slowStorageOps, columnFamily + ":" + type + ":" + operation, new long[] { 1L, t });
        }
    }

    @Override
    public void storageOp(String columnFamily, String type, long t) {
        if (capture) {
            op++;
            add(storageOps, columnFamily + ":" + type,  new long[] { 1L, t });
        }
    }

    @Override
    public void apiCall(String className, String methodName, long t) {
        if (capture) {
            api++;
            add(apiCalls, className + "." + methodName,  new long[] { 1L, t });
        }
    }

    private void add(Map<String, long[]> map, String key, long[] v) {
        long[] l = map.get(key);
        if (l != null) {
            l[0] += v[0];
            l[1] += v[1];
        } else {
            map.put(key, v);
        }
    }



}
