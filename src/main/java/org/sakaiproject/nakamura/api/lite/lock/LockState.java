package org.sakaiproject.nakamura.api.lite.lock;

public class LockState {

    private static final LockState NOT_LOCKED = new LockState(null, false, false, null);
    private final boolean owner;
    private final String path;
    private final boolean locked;
    private String token;

    public LockState(String path, boolean owner, boolean locked, String token) {
        this.path = path;
        this.owner = owner;
        this.locked = locked;
        this.token = token;
    }

    public static LockState getOwnerLockedToken(String path, String token) {
        return new LockState(path, true, true, token);
    }

    public static LockState getOwnerLockedNoToken(String path) {
        return new LockState(path, true, true, null);
    }

    public static LockState getUserLocked(String path, Object owner) {
        return new LockState(path, false, true, null);
    }

    public static LockState getNotLocked() {
        return NOT_LOCKED;
    }


    public boolean isOwnerLocked() {
        return owner ;
    }

    public String getLockPath() {
        return path;
    }

    public boolean isLocked() {
        return locked;
    }
    
    public String getToken() {
        return token;
    }

}
