package org.sakaiproject.nakamura.lite.lock;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.CacheHolder;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.api.lite.lock.AlreadyLockedException;
import org.sakaiproject.nakamura.api.lite.lock.LockManager;
import org.sakaiproject.nakamura.api.lite.lock.LockState;
import org.sakaiproject.nakamura.lite.CachingManagerImpl;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockManagerImpl extends CachingManagerImpl implements LockManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockManagerImpl.class);
    private String lockColumnFamily;
    private String keySpace;
    private String currentUser;

    public LockManagerImpl(StorageClient storageClient, Configuration config, User currentUser, Map<String, CacheHolder> sharedCache) {
        super(storageClient, sharedCache);
        this.lockColumnFamily = config.getLockColumnFamily();
        this.keySpace = config.getKeySpace();
        this.currentUser = currentUser.getId();
    }

    public void close() {
    }

    private Lock get(String path) throws StorageClientException {
        Map<String, Object> lockMap = getCached(keySpace, lockColumnFamily, path);
        if ( lockMap != null && lockMap.size() > 0) {
            Lock nl =  new Lock(lockMap);
            LOGGER.debug("Got Lock {} {} ",path, nl);
            return nl;
        }
        return null;
    }
    
    private void clear(String path) throws StorageClientException {
        removeCached(keySpace, lockColumnFamily, path);
    }


    public String lock(String path, long expires, String extra) throws StorageClientException, AlreadyLockedException {
        String currentPath = path;
        for(;;) {
            Lock lock = get(currentPath);
            if ( lock != null ) {
                if ( !lock.hasExpired() ) {
                    if ( !lock.isOwner(currentUser)) {
                        throw new AlreadyLockedException(currentPath);                        
                    }
                } else {
                    clear(currentPath);
                }
            }
            if ( StorageClientUtils.isRoot(currentPath)) {
                break;
            }
            currentPath = StorageClientUtils.getParentObjectPath(currentPath);
        }
        Lock newLock = new Lock(path, currentUser, expires, extra);
        LOGGER.debug("Applying lock {} {} ",path, newLock);
        putCached(keySpace, lockColumnFamily, path, newLock.getProperties() , true);
        return newLock.getToken();
    }
    
    public String refreshLock(String path, long timeoutInSeconds, String extra, String token) throws StorageClientException {
        String currentPath = path;
        for(;;) {
            Lock lock = get(currentPath);
            if ( lock != null && !lock.hasExpired() ) {
                LOGGER.debug("Lock is  not null and has not expired");
                if ( lock.isOwner(currentUser) ) {
                    if ( lock.hasToken(token)) {
                        LOGGER.info("Has Owner locked with token {} {} {} {} {}", new Object[]{path, currentUser, timeoutInSeconds, extra, token});
                        Lock newLock = new Lock(path, currentUser, timeoutInSeconds, extra, token);
                        putCached(keySpace, lockColumnFamily, path, newLock.getProperties() , false);
                        return newLock.getToken();
                    }
                }
            }
            if ( StorageClientUtils.isRoot(currentPath)) {
                return null;
            }
            currentPath = StorageClientUtils.getParentObjectPath(currentPath);
        }
    }
    

    public void unlock(String path, String token) throws StorageClientException {
        String currentPath = path;
        for(;;) {
            Lock lock = get(currentPath);
            if ( lock != null && !lock.hasExpired() ) {
                if ( lock.isOwner(currentUser) && lock.hasToken(token)) {
                    LOGGER.debug("Clearing lock at {} {} ",currentPath, lock);
                    clear(currentPath);
                }
            }
            if ( StorageClientUtils.isRoot(currentPath)) {
                return;
            }
            currentPath = StorageClientUtils.getParentObjectPath(currentPath);
        }
    }
    
    public LockState getLockState(String path, String token) throws StorageClientException {
        String currentPath = path;
        for(;;) {
            Lock lock = get(currentPath);
            if ( lock != null && !lock.hasExpired() ) {
                LOGGER.debug("Lock is  not null and has not expired");
                if ( lock.isOwner(currentUser) ) {
                    if ( lock.hasToken(token)) {
                        LOGGER.debug("Has Owner locked with token");
                        return LockState.getOwnerLockedToken(currentPath, currentUser, token, lock.getExtra());
                    } else {
                        LOGGER.debug("Has Owner locked with not token");
                        return LockState.getOwnerLockedNoToken(currentPath, currentUser, lock.getToken(), lock.getExtra());
                    }
                } else {
                    LOGGER.debug("Has User locked");
                    return LockState.getUserLocked(currentPath, lock.getOwner(), lock.getToken(), lock.getExtra());
                }
            } else {
                LOGGER.debug("Lock is null or has expired {} ",lock);
            }
            if ( StorageClientUtils.isRoot(currentPath)) {
                LOGGER.debug("Has Not locked");
                return LockState.getNotLocked();
            }
            currentPath = StorageClientUtils.getParentObjectPath(currentPath);
        }
    }
    

    public boolean isLocked(String path) throws StorageClientException {
        String currentPath = path;
        for(;;) {
            Lock lock = get(currentPath);
            if ( lock != null && !lock.hasExpired() ) {
                LOGGER.debug("Is Locked {} {} {} ",new Object[]{path, currentPath, lock});
                return true;
            }
            if ( StorageClientUtils.isRoot(currentPath)) {
                LOGGER.debug("Is Not Locked {} ", path);
                return false;
            }
            currentPath = StorageClientUtils.getParentObjectPath(currentPath);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
