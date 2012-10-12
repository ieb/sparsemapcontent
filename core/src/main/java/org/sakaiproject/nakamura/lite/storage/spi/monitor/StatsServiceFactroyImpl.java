package org.sakaiproject.nakamura.lite.storage.spi.monitor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component(immediate = true, metatype = true)
@Service(value = StatsServiceFactory.class)
public class StatsServiceFactroyImpl implements StatsServiceFactory {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(StatsServiceFactroyImpl.class);
    
    @Property(boolValue = false)
    private static final String DUMP_SESSION = "dump-session";
    @Property(boolValue = true)
    private static final String DUMP_APP = "dump-app";
    @Property(boolValue = true)
    private static final String CAPTURE = "capture";

    private boolean capture;
    private boolean dumpApp;
    private boolean dumpSession;

    // using AtomicLongs rather than locks because at moderate contention levels
    // Atomic*s work better than concurrent locks. At very high (arguably unrealistic) contention
    // levels locks work better.
    // I am not concerned about roll over of these because lets face it, the server
    // will never be up for long enough and I am not timing nanosecond operations. (maybe I should be).
	private Map<String, AtomicLongArray> apiCalls = Maps.newConcurrentMap();

	private Map<String, AtomicLongArray> storageOps = Maps.newConcurrentMap();

	private Map<String, AtomicLongArray> slowStorageOps = Maps.newConcurrentMap();

	private AtomicLong login = new AtomicLong();

	private AtomicLong logout = new AtomicLong();

	private AtomicLong loginFailed = new AtomicLong();
	private AtomicLong ncalls = new AtomicLong();
	private AtomicLong slowOp = new AtomicLong();
	private AtomicLong op = new AtomicLong();
	private AtomicLong api = new AtomicLong();

	private ReentrantReadWriteLock sortLock = new ReentrantReadWriteLock();


    @Activate
    public void activated(Map<String, Object> properties) {
        modified(properties);
    }
    
    @Modified
    public void modified(Map<String, Object> properties) {
        capture = (Boolean) properties.get(CAPTURE);
        dumpApp = (Boolean) properties.get(DUMP_APP) && capture;
        dumpSession = (Boolean) properties.get(DUMP_SESSION) && capture;

        LOGGER.info("Setup with capture:{}, dump App Stats:{}, dump Session Stats:{} ", new Object[] { capture, dumpApp,
                dumpSession });
    }

    @Override
    public StatsService openSession() {
        return new StatsServiceImpl(this, capture);
    }

    protected void save(StatsServiceImpl sessionStatsService) {
        if (capture) {
        	ReadLock rl = null;
        	try {
        		rl = sortLock.readLock();
        		if ( rl.tryLock(1, TimeUnit.SECONDS) ) {
		            addAll(sessionStatsService.apiCalls, apiCalls);
		            addAll(sessionStatsService.storageOps, storageOps);
		            addAll(sessionStatsService.slowStorageOps, slowStorageOps);
		            login.addAndGet(sessionStatsService.login);
		            logout.addAndGet(sessionStatsService.logout);
		            loginFailed.addAndGet(sessionStatsService.loginFailed);
		            slowOp.addAndGet(sessionStatsService.slowOp);
		            op.addAndGet(sessionStatsService.op);
		            api.addAndGet(sessionStatsService.api);
		            long nc = ncalls.incrementAndGet();
		            if (nc % 1000 == 0) {
		            	// the write lock is belt an braces just incase there are more than 1000 session logouts per
		            	// sort and save operation, possible in a JVM but unlikely.
		            	rl.unlock();
		            	rl = null;
	                    WriteLock wl = sortLock.writeLock();
	                    if (wl.tryLock(1, TimeUnit.SECONDS)) {
	                        try {
	                            // clean the maps up, we are the only
	                            // writer
	                            // so
	                            // it should be safe to iterate over the
	                            // main
	                            // maps.
	                            List<Entry<String, AtomicLongArray>> order = Lists.newArrayList();
	                            for (Entry<String, AtomicLongArray> e : slowStorageOps.entrySet()) {
	                                order.add(e);
	                            }
	                            if (dumpApp) {
	                                dump("app", order);
	                            }
	                            if (order.size() > 20) {
	                                Collections.sort(order, new Comparator<Entry<String, AtomicLongArray>>() {
	                                    @Override
	                                    public int compare(Entry<String, AtomicLongArray> o1, Entry<String, AtomicLongArray> o2) {
	                                        if (o1.getValue().get(1) == o2.getValue().get(1)) {
	                                            return 0;
	                                        }
	                                        return o1.getValue().get(1) > o2.getValue().get(1) ? -1 : 1;
	                                    }
	                                });
	                                for (int i = 10; i < order.size(); i++) {
	                                    slowStorageOps.remove(order.get(i).getKey());
	                                }
	                            }
	                        } finally {
	                            wl.unlock();
	                        }
	                    }
		            }
        		}
            } catch (InterruptedException e) {
                LOGGER.debug(e.getMessage(), e);
            } finally {
                if ( rl != null ) {
                    rl.unlock();
                }
            }
        }
    }
    
    
    private void dump(String name, List<Entry<String, AtomicLongArray>> order) {
        if (order.size() > 0) {
            LOGGER.warn("{} Key Slow Queries, T, N, Q ", name);
            for (Entry<String, AtomicLongArray> o : order) {
                LOGGER.warn("{}     Slow Queries, {}, {}, {}", new Object[] { name, o.getValue().get(1), o.getValue().get(0), o.getKey() });
            }
        }
        LOGGER.info("{}, Key Counters, {}, {}, {}, {}, {}, {}, {}", new Object[] { name, "ncalls", "login", "loginFailed", "logout", "slowOp", "op", "api" });
        LOGGER.info("{},     Counters, {}, {}, {}, {}, {}, {}, {}", new Object[] { name, ncalls, login, loginFailed, logout, slowOp, op, api });
        LOGGER.info("{} Key Storage, T, N, OP", name);
        for (Entry<String, AtomicLongArray> o : storageOps.entrySet()) {
            LOGGER.info("{}     Storage, {}, {}, {}", new Object[] { name, o.getValue().get(1), o.getValue().get(0), o.getKey() });
        }
        LOGGER.info("{} Key Api Call, T, N, M", name);
        for (Entry<String, AtomicLongArray> o : apiCalls.entrySet()) {
            LOGGER.info("{}     Api Call, {}, {}, {}", new Object[] { name, o.getValue().get(1), o.getValue().get(0), o.getKey() });
        }
    }

    private void addAll(Map<String, long[]> src, Map<String, AtomicLongArray> dest) {
        for (Entry<String, long[]> e : src.entrySet()) {
            add(dest, e.getKey(), e.getValue());
        }
    }

    private void add(Map<String, AtomicLongArray> map, String key, long[] v) {
        AtomicLongArray l = map.get(key);
        if (l != null) {
            l.addAndGet(0,  v[0]);
            l.addAndGet(1,  v[1]);
        } else {
            map.put(key, new AtomicLongArray(v));
        }
    }


}
