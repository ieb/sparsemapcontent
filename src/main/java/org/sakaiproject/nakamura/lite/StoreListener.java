package org.sakaiproject.nakamura.lite;

public interface StoreListener {

    public static final String USERID_PROPERTY = "userid";
    public static final String PATH_PROPERTY = "path";

    
    void onDelete(String zone, String path, String user, String ... attributes);

    void onUpdate(String zone, String path, String user, boolean isNew, String ... attributes);

    void onLogin(String id, String string);

    void onLogout(String id, String string);

}
