package org.sakaiproject.nakamura.lite.storage.spi.monitor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component(immediate = true, metatype = true)
@Service(value = StatsService.class)
public class StatsServiceImpl implements StatsService {

    @Property(boolValue=false)
    private static final String DUMP_SESSION = "dump-session";
    @Property(boolValue=true)
    private static final String DUMP_APP = "dump-app";
    @Property(boolValue=true)
    private static final String CAPTURE = "capture";
    private static final Logger LOGGER = LoggerFactory.getLogger(StatsServiceImpl.class);
    private long login;
    private long loginFailed;
    private long logout;
    private Map<String, Long[]> slowStorageOps = Maps.newConcurrentMap();
    private Map<String, Long> storageOps = Maps.newConcurrentMap();
    private Map<String, Long> apiCalls = Maps.newConcurrentMap();
    private StatsServiceImpl statsServiceParent;
    private long ncalls;
    private ReentrantReadWriteLock sortLock = new ReentrantReadWriteLock();
    private int op;
    private int slowOp;
    private int api;
    private boolean capture;
    private boolean dumpApp;
    private boolean dumpSession;

    public StatsServiceImpl() {
    }

    public StatsServiceImpl(StatsServiceImpl statsServiceParent) {
	this.statsServiceParent = statsServiceParent;
    }

    @Modified
    public void modified(Map<String, Object> properties) {
	capture = (Boolean) properties.get(CAPTURE);
	dumpApp = (Boolean) properties.get(DUMP_APP) && capture;
	dumpSession = (Boolean) properties.get(DUMP_SESSION) && capture;

    }

    @Override
    public void sessionLogin() {
	login++;
    }

    @Override
    public void sessionFailLogin() {
	if (capture) {
	    loginFailed++;
	    if (statsServiceParent != null) {
		statsServiceParent.save(this);
	    }
	}
    }

    @Override
    public void sessionLogout() {
	if (capture) {
	    logout++;
	    if (statsServiceParent != null) {
		WriteLock wl = sortLock.writeLock();
		try {
		    if (wl.tryLock(0, TimeUnit.SECONDS)) {
			try {
			    statsServiceParent.save(this);
			} finally {
			    wl.unlock();
			}
		    }
		} catch (InterruptedException e) {
		    LOGGER.debug(e.getMessage(), e);
		}
	    }
	}
    }

    @Override
    public void slowStorageOp(String columnFamily, String type, long t, String operation) {
	if (capture) {
	    slowOp++;
	    ReadLock rl = sortLock.readLock();
	    try {
		if (rl.tryLock(0, TimeUnit.SECONDS)) {
		    try {
			addCount(slowStorageOps, columnFamily + ":" + type + ":" + operation, new Long[] { 1L, t });
			if (dumpSession) {
			    dump("session", ImmutableList.copyOf(slowStorageOps.entrySet()));
			}
		    } finally {
			rl.unlock();

		    }
		}
	    } catch (InterruptedException e) {
		LOGGER.debug(e.getMessage(), e);
	    }
	}
    }

    @Override
    public void storageOp(String columnFamily, String type, long t) {
	if (capture) {
	    op++;
	    ReadLock rl = sortLock.readLock();
	    try {
		if (rl.tryLock(0, TimeUnit.SECONDS)) {
		    try {
			add(storageOps, columnFamily + ":" + type, t);
		    } finally {
			rl.unlock();

		    }
		}
	    } catch (InterruptedException e) {
		LOGGER.debug(e.getMessage(), e);
	    }
	}
    }

    @Override
    public void apiCall(String className, String methodName, long t) {
	if (capture) {
	    api++;
	    ReadLock rl = sortLock.readLock();
	    try {
		if (rl.tryLock(0, TimeUnit.SECONDS)) {
		    try {
			add(apiCalls, className + "." + methodName, t);
		    } finally {
			rl.unlock();

		    }
		}
	    } catch (InterruptedException e) {
		LOGGER.debug(e.getMessage(), e);
	    }
	}
    }

    protected void save(StatsServiceImpl sessionStatsService) {
	if (capture) {
	    ReadLock rl = sortLock.readLock();
	    try {
		if (rl.tryLock(0, TimeUnit.SECONDS)) {
		    try {
			addAll(sessionStatsService.apiCalls, apiCalls);
			addAll(sessionStatsService.storageOps, storageOps);
			addAllCount(sessionStatsService.slowStorageOps, slowStorageOps);
			login += sessionStatsService.login;
			logout += sessionStatsService.logout;
			loginFailed += sessionStatsService.loginFailed;
			ncalls++;
			if (ncalls % 100 == 0) {

			    WriteLock wl = sortLock.writeLock();
			    if (wl.tryLock(0, TimeUnit.SECONDS)) {
				try {
				    // clean the maps up, we are not the only
				    // writer
				    // so
				    // it should be safe to iterate over the
				    // main
				    // maps.
				    List<Entry<String, Long[]>> order = Lists.newArrayList();
				    for (Entry<String, Long[]> e : slowStorageOps.entrySet()) {
					order.add(e);
				    }
				    if (dumpApp && ncalls % 1000 == 0) {
					dump("app", order);
				    }
				    if (order.size() > 20) {
					Collections.sort(order, new Comparator<Entry<String, Long[]>>() {
					    @Override
					    public int compare(Entry<String, Long[]> o1, Entry<String, Long[]> o2) {
						if (o1.getValue()[1] == o2.getValue()[1]) {
						    return 0;
						}
						return o1.getValue()[1] > o2.getValue()[1] ? -1 : 1;
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
		    } finally {
			rl.unlock();

		    }
		}
	    } catch (InterruptedException e) {
		LOGGER.debug(e.getMessage(), e);
	    }
	}
    }

    private void dump(String name, List<Entry<String, Long[]>> order) {
	if (order.size() > 0) {
	    LOGGER.warn("{} Slow Queries, T, N, Q ", name);
	    for (Entry<String, Long[]> o : order) {
		LOGGER.warn("{} Slow, {}, {}, {}", new Object[] { name, o.getValue()[1], o.getValue()[0], o.getKey() });
	    }
	}
	LOGGER.info("{}, {}, {}, {},{}, {}, {}, {}", new Object[] { name, ncalls, login, loginFailed, logout, slowOp, op, api });
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("{} Storage, N, OP", name);
	    for (Entry<String, Long> o : storageOps.entrySet()) {
		LOGGER.debug("{} Storage, {}, {}", new Object[] { name, o.getValue(), o.getKey() });
	    }
	    LOGGER.debug("{} Api Call, N, M", name);
	    for (Entry<String, Long> o : apiCalls.entrySet()) {
		LOGGER.debug("{} Api Call, {}, {}", new Object[] { name, o.getValue(), o.getKey() });
	    }
	}
    }

    private void addAllCount(Map<String, Long[]> src, Map<String, Long[]> dest) {
	for (Entry<String, Long[]> e : src.entrySet()) {
	    addCount(dest, e.getKey(), e.getValue());
	}
    }

    private void addAll(Map<String, Long> src, Map<String, Long> dest) {
	for (Entry<String, Long> e : src.entrySet()) {
	    add(dest, e.getKey(), e.getValue());
	}
    }

    private void addCount(Map<String, Long[]> map, String key, Long[] v) {
	Long[] l = map.get(key);
	if (l != null) {
	    l[0] += v[0];
	    l[1] += v[1];
	} else {
	    map.put(key, v);
	}
    }

    private void add(Map<String, Long> map, String key, long t, String... path) {
	if (map.containsKey(key)) {
	    map.put(key, map.get(key) + t);
	} else {
	    map.put(key, t);
	}
    }

    @Override
    public StatsService openSession() {
	return new StatsServiceImpl(this);
    }

}
