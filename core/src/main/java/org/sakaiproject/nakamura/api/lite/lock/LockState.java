package org.sakaiproject.nakamura.api.lite.lock;


public class LockState {

    private static final LockState NOT_LOCKED = new LockState(null, false, null, false, false,
            null, null);
    private final boolean isOwner;
    private final String owner;
    private final String path;
    private final boolean locked;
    private String token;
    private String extra;
    private boolean matchedToken;

    public LockState(String path, boolean isOwner, String owner, boolean locked,
            boolean matchedToken, String token, String extra) {
        this.path = path;
        this.isOwner = isOwner;
        this.owner = owner;
        this.locked = locked;
        this.matchedToken = matchedToken;
        this.token = token;
        this.extra = extra;
    }

    public static LockState getOwnerLockedToken(String path, String owner, String token,
            String extra) {
        return new LockState(path, true, owner, true, true, token, extra);
    }

    public static LockState getOwnerLockedNoToken(String path, String owner, String token,
            String extra) {
        return new LockState(path, true, owner, true, false, token, extra);
    }

    public static LockState getUserLocked(String path, String owner, String token, String extra) {
        return new LockState(path, false, owner, true, false, token, extra);
    }

    public static LockState getNotLocked() {
        return NOT_LOCKED;
    }

    public boolean isOwner() {
        return isOwner;
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

    public boolean hasMatchedToken() {
        return matchedToken;
    }

    public String getExtra() {
        return extra;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return " isOwner:" + isOwner + " owner:" + owner + " locked:" + locked + " matchedToken:"
                + matchedToken + " token:" + token + " extra:[" + extra + "]";
    }

}
