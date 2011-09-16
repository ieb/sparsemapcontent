package org.sakaiproject.nakamura.lite.lock;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.StorageClientUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class Lock {

    private static final String PATH_FIELD = "p";
    private static final String USER_FIELD = "u";
    private static final String EXPIRES_FIELD = "x";
    private static final String EXPIRES_AT_FIELD = "a";
    private static final String EXTRA_FIELD = "e";
    private static final String TOKEN_FIELD = "t";
    private Map<String, Object> lockMap;

    public Lock(String path, String currentUser, long expires, String extra) {
        Builder<String, Object> b = ImmutableMap.builder();
        b.put(Lock.PATH_FIELD, path);
        b.put(Lock.USER_FIELD, currentUser);
        b.put(Lock.EXPIRES_FIELD, expires);
        b.put(Lock.EXPIRES_AT_FIELD, System.currentTimeMillis()+(expires*1000L));
        b.put(Lock.EXTRA_FIELD, extra);
        b.put(Lock.TOKEN_FIELD, StorageClientUtils.insecureHash(System.currentTimeMillis()+":"+path+":"+currentUser+":"+expires));
        
        lockMap = b.build();
    }

    public Lock(Map<String, Object> lockMap) {
        this.lockMap = ImmutableMap.copyOf(lockMap);
    }

    public boolean hasExpired() {
        return System.currentTimeMillis() > (Long)lockMap.get(Lock.EXPIRES_AT_FIELD);
    }

    public boolean isOwner(String currentUser) {
        return currentUser.equals(lockMap.get(Lock.USER_FIELD));
    }

    public Map<String, Object> getProperties() {
        return lockMap;
    }

    public boolean hasToken(String token) {
        return token.equals(lockMap.get(TOKEN_FIELD));
    }

    public String getToken() {
        return (String) lockMap.get(TOKEN_FIELD);
    }

    public String getOwner() {
        return (String) lockMap.get(USER_FIELD);
    }
    
    @Override
    public String toString() {
        return String.valueOf(lockMap);
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof Lock ) {
            return getToken().equals(((Lock) obj).getToken());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return getToken().hashCode();
    }

}
