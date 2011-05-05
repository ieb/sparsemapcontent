package org.sakaiproject.nakamura.lite;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.sakaiproject.nakamura.api.lite.StoreListener;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

@Component(immediate = true, metatype = true)
@Service
public class OSGiStoreListener implements StoreListener {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(OSGiStoreListener.class);
    public static final String DEFAULT_DELETE_TOPIC = TOPIC_BASE + DELETE_TOPIC;
    public static final String DEFAULT_CREATE_TOPIC = TOPIC_BASE + ADDED_TOPIC;
    public static final String DEFAULT_UPDATE_TOPIC = TOPIC_BASE + UPDATED_TOPIC;
    @Reference
    protected EventAdmin eventAdmin;
    private static Map<String, String> deleteTopics;
    private static Map<String, String> createTopics;
    private static Map<String, String> updateTopics;

    static {
        Builder<String, String> deleteTopicsBuilder = ImmutableMap.builder();
        deleteTopicsBuilder.put(Security.ADMIN_AUTHORIZABLES, TOPIC_BASE + "authorizables/" + DELETE_TOPIC);
        deleteTopicsBuilder.put(Security.ADMIN_GROUPS, TOPIC_BASE + "groups/" + DELETE_TOPIC);
        deleteTopicsBuilder.put(Security.ADMIN_USERS, TOPIC_BASE + "users/" + DELETE_TOPIC);
        deleteTopicsBuilder.put(Security.ZONE_ADMIN, TOPIC_BASE + "admin/" + DELETE_TOPIC);
        deleteTopicsBuilder.put(Security.ZONE_AUTHORIZABLES, TOPIC_BASE + "authorizables/" + DELETE_TOPIC);
        deleteTopicsBuilder.put(Security.ZONE_CONTENT, TOPIC_BASE + "content/" + DELETE_TOPIC);
        deleteTopics = deleteTopicsBuilder.build();
        Builder<String, String> createTopicsBuilder = ImmutableMap.builder();
        createTopicsBuilder.put(Security.ADMIN_AUTHORIZABLES, TOPIC_BASE + "authorizables/"+ ADDED_TOPIC);
        createTopicsBuilder.put(Security.ADMIN_GROUPS, TOPIC_BASE + "groups/"+ ADDED_TOPIC);
        createTopicsBuilder.put(Security.ADMIN_USERS, TOPIC_BASE + "users/"+ ADDED_TOPIC);
        createTopicsBuilder.put(Security.ZONE_ADMIN, TOPIC_BASE + "admin/"+ ADDED_TOPIC);
        createTopicsBuilder.put(Security.ZONE_AUTHORIZABLES, TOPIC_BASE + "authorizables/"+ ADDED_TOPIC);
        createTopicsBuilder.put(Security.ZONE_CONTENT, TOPIC_BASE + "content/"+ ADDED_TOPIC);
        createTopics = createTopicsBuilder.build();
        Builder<String, String> updateTopicsBuilder = ImmutableMap.builder();
        updateTopicsBuilder.put(Security.ADMIN_AUTHORIZABLES, TOPIC_BASE + "authorizables/"+ UPDATED_TOPIC);
        updateTopicsBuilder.put(Security.ADMIN_GROUPS, TOPIC_BASE + "groups/"+ UPDATED_TOPIC);
        updateTopicsBuilder.put(Security.ADMIN_USERS, TOPIC_BASE + "users/"+ UPDATED_TOPIC);
        updateTopicsBuilder.put(Security.ZONE_ADMIN, TOPIC_BASE + "admin/"+ UPDATED_TOPIC);
        updateTopicsBuilder.put(Security.ZONE_AUTHORIZABLES, TOPIC_BASE + "authorizables/"+ UPDATED_TOPIC);
        updateTopicsBuilder.put(Security.ZONE_CONTENT, TOPIC_BASE + "content/"+ UPDATED_TOPIC);
        updateTopics = updateTopicsBuilder.build();

    }

    public void onDelete(String zone, String path, String user, Map<String, Object> beforeEvent, String ... attributes) {
        String topic = DEFAULT_DELETE_TOPIC;
        if (deleteTopics.containsKey(zone)) {
            topic = deleteTopics.get(zone);
        }
        postEvent(topic, path, user, beforeEvent, attributes);
    }

    public void onUpdate(String zone, String path, String user, boolean isNew,  Map<String, Object> beforeEvent, String... attributes) {

        String topic = DEFAULT_UPDATE_TOPIC;
        if (isNew) {
            topic = DEFAULT_CREATE_TOPIC;
            if (deleteTopics.containsKey(zone)) {
                topic = createTopics.get(zone);
            }
        } else {
            if (deleteTopics.containsKey(zone)) {
                topic = updateTopics.get(zone);
            }

        }
        postEvent(topic, path, user, beforeEvent, attributes);
    }

    public void onLogin(String userid, String sessionID) {
        LOGGER.debug("Login {} {} ", userid, sessionID);
    }

    public void onLogout(String userid, String sessionID) {
        LOGGER.debug("Logout {} {} ", userid, sessionID);
    }

    private void postEvent(String topic, String path, String user,  Map<String, Object> beforeEvent, String[] attributes) {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>();
        if (attributes != null) {
            for (String attribute : attributes) {
                String[] parts = StringUtils.split(attribute, ":", 2);
                if (parts != null) {
                    if (parts.length == 1) {
                        properties.put(parts[0], parts[0]);
                    } else if (parts.length == 2) {
                        properties.put(parts[0], parts[1]);
                    }
                }
            }
        }
        if (path != null) {
            properties.put(PATH_PROPERTY, path);
        }
        properties.put(USERID_PROPERTY, user);
        if ( beforeEvent != null) {
            properties.put(BEFORE_EVENT_PROPERTY, beforeEvent);
        }
        eventAdmin.postEvent(new Event(topic, properties));

    }

}
