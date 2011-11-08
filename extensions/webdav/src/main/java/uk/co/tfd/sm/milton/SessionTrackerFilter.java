package uk.co.tfd.sm.milton;

import java.util.Set;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Filter;
import com.bradmcevoy.http.FilterChain;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;
import com.google.common.collect.Sets;

public class SessionTrackerFilter implements Filter, ThreadedSessionTracker {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionTrackerFilter.class);
	private ThreadLocal<Set<Session>> tracker = new ThreadLocal<Set<Session>>() {
		protected Set<Session> initialValue() { return Sets.newHashSet(); };
	};

	public void process(FilterChain chain, Request request, Response response) {
		try {
			chain.process(request, response);
			for ( Session s : tracker.get()) {
				LOGGER.debug("Committing {} ", s);
				s.commit();
			}
		} finally {
			Set<Session> sessions = tracker.get();
			for ( Session s : sessions) {
				try {
					LOGGER.debug("Logout {} ", s);
					s.logout();
				} catch (ClientPoolException e) {
					LOGGER.error(e.getMessage(),e);
				}
			}
			sessions.clear();
		}

	}

	public Session register(Session session) {
		tracker.get().add(session);
		return session;
	}

}
