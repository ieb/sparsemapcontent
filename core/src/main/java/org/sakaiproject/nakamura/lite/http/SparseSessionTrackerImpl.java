package org.sakaiproject.nakamura.lite.http;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.SparseSessionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

@Component(immediate=true, metatype=true)
@Service(value={Filter.class,SparseSessionTracker.class})
@Properties( value={
        @Property(name="pattern", value="/.*")
})
public class SparseSessionTrackerImpl implements Filter, SparseSessionTracker {

    
    private static final String SESSION_ATTRIBUTE = SparseSessionTrackerImpl.class.getName()+".session";
    private static final Logger LOGGER = LoggerFactory.getLogger(SparseSessionTrackerImpl.class);

    public Session register(Session login, HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Set<Session> o = (Set<Session>) request.getAttribute(SESSION_ATTRIBUTE);
        if ( o == null ) {
           o = Sets.newLinkedHashSet();
           request.setAttribute(SESSION_ATTRIBUTE, o);
        }
        o.add(login);
        return login;
    }

    public Session get(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Set<Session> o = (Set<Session>) request.getAttribute(SESSION_ATTRIBUTE);
        if ( o == null ) {
            return null;
        }
        Session s = null;
        // get the one last added to this linkedHashSet.
        for ( Session sc : o ) {
            s = sc;
        }
        return s;
    }

    public void destroy() {
    }
    
    public void init(FilterConfig config) throws ServletException {
    }


    @SuppressWarnings("unchecked")
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
            Session[] sessionArray = null;
            try {
                filterChain.doFilter(request, response);
                if ( request instanceof HttpServletRequest) {
                    HttpServletRequest hrequest = (HttpServletRequest) request;
                    Set<Session> sessions = (Set<Session>) hrequest.getAttribute(SESSION_ATTRIBUTE);
                    if ( sessions != null ) {
                        hrequest.setAttribute(SESSION_ATTRIBUTE, null);
                        // commit from the last one
                        sessionArray = sessions.toArray(new Session[sessions.size()]);
                        for ( int i = sessionArray.length-1; i >= 0; i--) {
                            LOGGER.debug("Committing {} ", sessionArray[i]);
                            sessionArray[i].commit();
                        }                        
                    }
                }
            } finally {
                if ( sessionArray != null ) {
                    for ( int i = sessionArray.length-1; i >= 0; i--) {
                        try {
                            LOGGER.debug("Logout {} ", sessionArray[i]);
                            sessionArray[i].logout();
                        } catch (ClientPoolException e) {
                            LOGGER.error(e.getMessage(),e);
                        }
                    
                    }
                    
                }
            }
            
    }


}
