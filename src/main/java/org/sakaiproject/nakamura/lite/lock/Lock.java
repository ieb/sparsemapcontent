package org.sakaiproject.nakamura.lite.lock;

import java.util.Map;

public class Lock {

    public Lock(String path, String currentUser, long expires, String extra) {
        // TODO Auto-generated constructor stub
    }

    public Lock(Map<String, Object> lockMap) {
        // TODO Auto-generated constructor stub
    }

    public boolean hasExpired() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isOwner(String currentUser) {
        // TODO Auto-generated method stub
        return false;
    }

    public Map<String, Object> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasToken(String token) {
        // TODO Auto-generated method stub
        return false;
    }

    public String getToken() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getOwner() {
        // TODO Auto-generated method stub
        return null;
    }

}
