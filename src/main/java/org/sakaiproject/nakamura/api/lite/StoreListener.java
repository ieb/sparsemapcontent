package org.sakaiproject.nakamura.api.lite;


public interface StoreListener {
    public static final String TOPIC_BASE = "org/sakaiproject/nakamura/lite/";
    public static final String DELETE_TOPIC = "DELETE";
    public static final String ADDED_TOPIC = "ADDED";
    public static final String UPDATED_TOPIC = "UPDATED";
    public static final String DEFAULT_DELETE_TOPIC = TOPIC_BASE + DELETE_TOPIC;
    public static final String DEFAULT_CREATE_TOPIC = TOPIC_BASE + ADDED_TOPIC;
    public static final String DEFAULT_UPDATE_TOPIC = TOPIC_BASE + UPDATED_TOPIC;
    public static final String[] DEFAULT_TOPICS = new String[] { DEFAULT_CREATE_TOPIC,
            DEFAULT_UPDATE_TOPIC, DEFAULT_DELETE_TOPIC, TOPIC_BASE + "authorizables/DELETE",
            TOPIC_BASE + "groups/DELETE", TOPIC_BASE + "users/DELETE", TOPIC_BASE + "admin/DELETE",
            TOPIC_BASE + "authorizables/DELETE", TOPIC_BASE + "content/DELETE",
            TOPIC_BASE + "authorizables/CREATE", TOPIC_BASE + "groups/CREATE",
            TOPIC_BASE + "users/CREATE", TOPIC_BASE + "admin/CREATE",
            TOPIC_BASE + "authorizables/CREATE", TOPIC_BASE + "content/CREATE",
            TOPIC_BASE + "authorizables/CHANGE", TOPIC_BASE + "groups/CHANGE",
            TOPIC_BASE + "users/CHANGE", TOPIC_BASE + "admin/CHANGE",
            TOPIC_BASE + "authorizables/CHANGE", TOPIC_BASE + "content/CHANGE" };
    public static final String USERID_PROPERTY = "userid";
    public static final String PATH_PROPERTY = "path";

    void onDelete(String zone, String path, String user, String... attributes);

    void onUpdate(String zone, String path, String user, boolean isNew, String... attributes);

    void onLogin(String id, String string);

    void onLogout(String id, String string);

}
