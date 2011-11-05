package org.sakaiproject.nakamura.lite;

import org.sakaiproject.nakamura.api.lite.StoreListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public class LoggingStorageListener implements StoreListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingStorageListener.class);
    private boolean quiet;

    public LoggingStorageListener(boolean quiet) {
        this.quiet = quiet;
    }

    public LoggingStorageListener() {
        this.quiet = false;
    }

    public void onDelete(String zone, String path, String user, String resourceType, Map<String, Object> beforeEvent,
            String... attributes) {
        if (!quiet) {
            LOGGER.info("Delete {} {} {} {} {} ",
                    new Object[] { zone, path, user, resourceType, Arrays.toString(attributes) });
        }
    }

    public void onUpdate(String zone, String path, String user, String resourceType, boolean isNew,
            Map<String, Object> beforeEvent, String... attributes) {
        if (!quiet) {
            LOGGER.info("Update {} {} {} {} new:{} {} ", new Object[] { zone, path, user, resourceType, isNew,
                    Arrays.toString(attributes) });
        }
    }

    public void onLogin(String userId, String sessionId) {
        if (!quiet) {
            LOGGER.info("Login  {} {}", new Object[] { userId, sessionId });
        }
    }

    public void onLogout(String userId, String sessionId) {
        if (!quiet) {
            LOGGER.info("Logout  {} {}", new Object[] { userId, sessionId });
        }
    }

}
