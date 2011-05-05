package org.sakaiproject.nakamura.lite;

import org.sakaiproject.nakamura.api.lite.StoreListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;


public class LoggingStorageListener implements StoreListener {
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingStorageListener.class);

    public void onDelete(String zone, String path, String user, Map<String, Object> beforeEvent, String... attributes) {
        LOGGER.info("Delete {} {} {} {} ", new Object[] {zone,path,user,Arrays.toString(attributes)});
    }

    public void onUpdate(String zone, String path, String user, boolean isNew, Map<String, Object> beforeEvent, String... attributes) {
        LOGGER.info("Update {} {} {} new:{} {} ", new Object[] {zone,path,user,isNew,Arrays.toString(attributes)});
    }

    public void onLogin(String userId, String sessionId) {
        LOGGER.info("Login  {} {}", new Object[] {userId, sessionId});
    }

    public void onLogout(String userId, String sessionId) {
        LOGGER.info("Logout  {} {}", new Object[] {userId, sessionId});
    }


}
