package org.sakaiproject.nakamura.lite;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.event.EventAdmin;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;

import java.util.Map;

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
        Map<String, Object> testMap = ImmutableMap.of("test", (Object) new String[]{"an", "array"});
        for (String zone : new String[] { Security.ADMIN_AUTHORIZABLES, Security.ADMIN_GROUPS,
                Security.ADMIN_USERS, Security.ZONE_ADMIN, Security.ZONE_AUTHORIZABLES,
                Security.ZONE_CONTENT }) {
            l.onDelete(zone, "path", "user", null);
            l.onDelete(zone, "path", "user", testMap, (String[]) null);
            l.onDelete(zone, "path", "user", null, "xx");
            l.onDelete(zone, "path", "user", testMap, "x:x");
            l.onDelete(zone, null, "user", null, "x:x", "x:x");
            l.onUpdate(zone, "path", "user", true, null);
            l.onUpdate(zone, "path", "user", false, testMap, (String[]) null);
            l.onUpdate(zone, "path", "user", true, null, "xx");
            l.onUpdate(zone, "path", "user", false, testMap, "x:x");
            l.onUpdate(zone, null, "user", true, null, "x:x", "x:x");
        }
        l.onLogin("userId", "sessionId");
        l.onLogout("userId", "sessoionID");
    }
}
