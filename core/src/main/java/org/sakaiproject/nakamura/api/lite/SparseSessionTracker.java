package org.sakaiproject.nakamura.api.lite;

import javax.servlet.http.HttpServletRequest;

/**
 * Tracks sessions and provides a mechanism to retrieve them, based on request
 * or thread. Retrieval on thread should only be used where its know that the
 * underlying request processing model will the thread based. If event based
 * processing is being used, no assumption about thread should be made.
 * 
 * @author ieb
 * 
 */
public interface SparseSessionTracker {

    /**
     * Register a session against a request.
     * 
     * @param login
     *            the session to be registered
     * @param request
     *            the request to register against, if null registration will be
     *            performed on the thread and not on the thread.
     * @return the session just registered.
     */
    Session register(Session login, HttpServletRequest request);

    /**
     * @param request
     *            the request to get the session from, if null, the thread will
     *            be inspected.
     * @return the session that was previously registered, or null if no session
     *         was registered.
     */
    Session get(HttpServletRequest request);

}
