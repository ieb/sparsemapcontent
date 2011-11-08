package uk.co.tfd.sm.milton;

import org.sakaiproject.nakamura.api.lite.Session;

public interface ThreadedSessionTracker {

	Session register(Session session);

}
