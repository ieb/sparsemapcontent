package org.sakaiproject.nakamura.lite;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

@Component(immediate = true, metatype = true)
@Service
public class OSGiStoreListener implements StoreListener {
    private static final String TOPIC_BASE = "org/sakaiproject/nakamura/lite/";
    private static final String DEFAULT_DELETE_TOPIC = TOPIC_BASE + "DELETE";
    private static final String LOGIN_TOPIC = TOPIC_BASE + "LOGIN";
    private static final String LOGOUT_TOPIC = TOPIC_BASE + "LOGOUT";
    private static final String DEFAULT_CREATE_TOPIC = TOPIC_BASE + "ADDED";
    private static final String DEFAULT_UPDATE_TOPIC = TOPIC_BASE + "UPDATED";
    @Reference
    protected EventAdmin eventAdmin;
    private static Map<String, String> deleteTopics;
    private static Map<String, String> createTopics;
    private static Map<String, String> updateTopics;

    static {
        Builder<String, String> deleteTopicsBuilder = ImmutableMap.builder();
        deleteTopicsBuilder.put(Security.ADMIN_AUTHORIZABLES, TOPIC_BASE + "authorizables/DELETE");
        deleteTopicsBuilder.put(Security.ADMIN_GROUPS, TOPIC_BASE + "groups/DELETE");
        deleteTopicsBuilder.put(Security.ADMIN_USERS, TOPIC_BASE + "users/DELETE");
        deleteTopicsBuilder.put(Security.ZONE_ADMIN, TOPIC_BASE + "admin/DELETE");
        deleteTopicsBuilder.put(Security.ZONE_AUTHORIZABLES, TOPIC_BASE + "authorizables/DELETE");
        deleteTopicsBuilder.put(Security.ZONE_CONTENT, TOPIC_BASE + "content/DELETE");
        deleteTopics = deleteTopicsBuilder.build();
        Builder<String, String> createTopicsBuilder = ImmutableMap.builder();
        createTopicsBuilder.put(Security.ADMIN_AUTHORIZABLES, TOPIC_BASE + "authorizables/CREATE");
        createTopicsBuilder.put(Security.ADMIN_GROUPS, TOPIC_BASE + "groups/CREATE");
        createTopicsBuilder.put(Security.ADMIN_USERS, TOPIC_BASE + "users/CREATE");
        createTopicsBuilder.put(Security.ZONE_ADMIN, TOPIC_BASE + "admin/CREATE");
        createTopicsBuilder.put(Security.ZONE_AUTHORIZABLES, TOPIC_BASE + "authorizables/CREATE");
        createTopicsBuilder.put(Security.ZONE_CONTENT, TOPIC_BASE + "content/CREATE");
        createTopics = createTopicsBuilder.build();
        Builder<String, String> updateTopicsBuilder = ImmutableMap.builder();
        updateTopicsBuilder.put(Security.ADMIN_AUTHORIZABLES, TOPIC_BASE + "authorizables/CHANGE");
        updateTopicsBuilder.put(Security.ADMIN_GROUPS, TOPIC_BASE + "groups/CHANGE");
        updateTopicsBuilder.put(Security.ADMIN_USERS, TOPIC_BASE + "users/CHANGE");
        updateTopicsBuilder.put(Security.ZONE_ADMIN, TOPIC_BASE + "admin/CHANGE");
        updateTopicsBuilder.put(Security.ZONE_AUTHORIZABLES, TOPIC_BASE + "authorizables/CHANGE");
        updateTopicsBuilder.put(Security.ZONE_CONTENT, TOPIC_BASE + "content/CHANGE");
        updateTopics = updateTopicsBuilder.build();

    }

    public void onDelete(String zone, String path, String user, String... attributes) {
        String topic = DEFAULT_DELETE_TOPIC;
        if (deleteTopics.containsKey(zone)) {
            topic = deleteTopics.get(zone);
        }
        postEvent(topic, path, user, attributes);
    }

    public void onUpdate(String zone, String path, String user, boolean isNew, String... attributes) {

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
        postEvent(topic, path, user, attributes);
    }

    public void onLogin(String userid, String sessoionID) {
        postEvent(LOGIN_TOPIC, null, userid, new String[] { "session:" + sessoionID });

    }

    public void onLogout(String userid, String sessoionID) {
        postEvent(LOGOUT_TOPIC, null, userid, new String[] { "session:" + sessoionID });
    }

    private void postEvent(String topic, String path, String user, String[] attributes) {
        final Dictionary<String, String> properties = new Hashtable<String, String>();
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
        eventAdmin.postEvent(new Event(topic, properties));

    }

}
