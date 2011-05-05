package org.sakaiproject.nakamura.api.lite;

import java.util.Map;


public interface StoreListener {
    public static final String TOPIC_BASE = "org/sakaiproject/nakamura/lite/";
    public static final String DELETE_TOPIC = "DELETE";
    public static final String ADDED_TOPIC = "ADDED";
    public static final String UPDATED_TOPIC = "UPDATED";
    public static final String DEFAULT_DELETE_TOPIC = TOPIC_BASE + DELETE_TOPIC;
    public static final String DEFAULT_CREATE_TOPIC = TOPIC_BASE + ADDED_TOPIC;
    public static final String DEFAULT_UPDATE_TOPIC = TOPIC_BASE + UPDATED_TOPIC;
    public static final String[] DEFAULT_TOPICS = new String[] { DEFAULT_CREATE_TOPIC,
            DEFAULT_UPDATE_TOPIC, DEFAULT_DELETE_TOPIC, 
            TOPIC_BASE + "authorizables/" + DELETE_TOPIC,
            TOPIC_BASE + "groups/" + DELETE_TOPIC, 
            TOPIC_BASE + "users/" + DELETE_TOPIC, 
            TOPIC_BASE + "admin/" + DELETE_TOPIC,
            TOPIC_BASE + "authorizables/" + DELETE_TOPIC, 
            TOPIC_BASE + "content/" + DELETE_TOPIC,
            TOPIC_BASE + "authorizables/"+ADDED_TOPIC,
            TOPIC_BASE + "groups/"+ADDED_TOPIC,
            TOPIC_BASE + "users/"+ADDED_TOPIC,
            TOPIC_BASE + "admin/"+ADDED_TOPIC,
            TOPIC_BASE + "authorizables/"+ADDED_TOPIC, 
            TOPIC_BASE + "content/"+ADDED_TOPIC,
            TOPIC_BASE + "authorizables/"+UPDATED_TOPIC, 
            TOPIC_BASE + "groups/"+UPDATED_TOPIC,
            TOPIC_BASE + "users/"+UPDATED_TOPIC,
            TOPIC_BASE + "admin/"+UPDATED_TOPIC,
            TOPIC_BASE + "authorizables/"+UPDATED_TOPIC, 
            TOPIC_BASE + "content/"+UPDATED_TOPIC };
    public static final String USERID_PROPERTY = "userid";
    public static final String PATH_PROPERTY = "path";
    public static final String BEFORE_EVENT_PROPERTY = "_beforeEvent";
    void onDelete(String zone, String path, String user, Map<String, Object> beforeEvent, String... attributes);

    void onUpdate(String zone, String path, String user, boolean isNew,  Map<String, Object> beforeEvent, String... attributes);

    void onLogin(String id, String string);

    void onLogout(String id, String string);

}
