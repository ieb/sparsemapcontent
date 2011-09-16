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
import org.sakaiproject.nakamura.lite.CachingManager;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockManagerImpl extends CachingManager implements LockManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockManagerImpl.class);
    private StorageClient storageClient;
    private String lockColumnFamily;
    private String keySpace;
    private String currentUser;

    public LockManagerImpl(StorageClient storageClient, Configuration config, User currentUser, Map<String, CacheHolder> sharedCache) {
        super(storageClient, sharedCache);
        this.storageClient = storageClient;
        this.lockColumnFamily = config.getLockColumnFamily();
        this.keySpace = config.getKeySpace();
        this.currentUser = currentUser.getId();
    }

    public void close() {
        // TODO Auto-generated method stub
        
    }

    private Lock get(String path) throws StorageClientException {
        Map<String, Object> lockMap = getCached(keySpace, lockColumnFamily, path);
        if ( lockMap != null && lockMap.size() > 0) {
            return new Lock(lockMap);
        }
        return null;
    }
    
    private void clear(String path) throws StorageClientException {
        removeFromCache(path, lockColumnFamily, path);
        storageClient.remove(path, lockColumnFamily, path);
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
        putCached(keySpace, lockColumnFamily, path, newLock.getProperties() , false);
        return newLock.getToken();
    }
    

    public void unlock(String path, String token) throws StorageClientException {
        String currentPath = path;
        for(;;) {
            Lock lock = get(currentPath);
            if ( lock != null && !lock.hasExpired() ) {
                if ( lock.isOwner(currentUser) && lock.hasToken(token)) {
                    clear(currentPath);
                }
            }
            if ( StorageClientUtils.isRoot(currentPath)) {
                return;
            }
            currentPath = StorageClientUtils.getParentObjectPath(currentPath);
        }
    }
    
    public LockState holdsLock(String path, String token) throws StorageClientException {
        String currentPath = path;
        for(;;) {
            Lock lock = get(currentPath);
            if ( lock != null && !lock.hasExpired() ) {
                if ( lock.isOwner(currentUser) ) {
                    if ( lock.hasToken(token)) {
                        return LockState.getOwnerLockedToken(currentPath, token);
                    } else {
                        return LockState.getOwnerLockedNoToken(currentPath);
                    }
                } else {
                    return LockState.getUserLocked(currentPath, lock.getOwner());
                }
            }
            if ( StorageClientUtils.isRoot(currentPath)) {
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
                return true;
            }
            if ( StorageClientUtils.isRoot(currentPath)) {
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
