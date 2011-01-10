package org.sakaiproject.nakamura.lite;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.event.EventAdmin;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;

public class OSGiStoreListenerTest {

    @Mock
    private EventAdmin eventAdmin;

    public OSGiStoreListenerTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() {
        OSGiStoreListener l = new OSGiStoreListener();
        l.eventAdmin = eventAdmin;
        for (String zone : new String[] { Security.ADMIN_AUTHORIZABLES, Security.ADMIN_GROUPS,
                Security.ADMIN_USERS, Security.ZONE_ADMIN, Security.ZONE_AUTHORIZABLES,
                Security.ZONE_CONTENT }) {
            l.onDelete(zone, "path", "user");
            l.onDelete(zone, "path", "user", (String[]) null);
            l.onDelete(zone, "path", "user", "xx");
            l.onDelete(zone, "path", "user", "x:x");
            l.onDelete(zone, null, "user", "x:x", "x:x");
            l.onUpdate(zone, "path", "user", true);
            l.onUpdate(zone, "path", "user", false, (String[]) null);
            l.onUpdate(zone, "path", "user", true, "xx");
            l.onUpdate(zone, "path", "user", false, "x:x");
            l.onUpdate(zone, null, "user", true, "x:x", "x:x");
        }
        l.onLogin("userId", "sessionId");
        l.onLogout("userId", "sessoionID");
    }
}
