package org.sakaiproject.nakamura.api.lite.lock;

import org.sakaiproject.nakamura.api.lite.StorageClientException;

/**
 * A simple hierarchical lock manager with tokens to identify locks.
 * Implementations of the interface should not be bound to the content system.
 * Locks live in their own hierarchy, and may exist even if there is not object
 * present at that location in any other hierarchy.
 * 
 * @author ieb
 * 
 */
public interface LockManager {

    /**
     * Locks a path returning a token for the lock if successful, null if not
     * 
     * @param path
     *            the path to lock
     * @param timeoutInSeconds
     *            ttl for the lock in s from the time it was created.
     * @param extra
     *            any extra information to be stored with the lock.
     * @return the lock token.
     * @throws StorageClientException
     * @throws AlreadyLockedException
     */
    String lock(String path, long timeoutInSeconds, String extra) throws StorageClientException,
            AlreadyLockedException;

    /**
     * Unlock a path for a given token, if the token and current user match.
     * 
     * @param path
     *            the path
     * @param token
     *            the token.
     * @throws StorageClientException
     */
    void unlock(String path, String token) throws StorageClientException;

    /**
     * Get the lock state for a path given a token
     * 
     * @param path
     *            the path
     * @param token
     *            the token
     * @return a lock state object which indicates if the token is current and
     *         bound to the current user. Lock state also indicates the location
     *         of the current lock.
     * @throws StorageClientException
     */
    LockState getLockState(String path, String token) throws StorageClientException;

    /**
     * Check the it path is locked.
     * 
     * @param path
     *            the path.
     * @return true if the path is locked.
     * @throws StorageClientException
     */
    boolean isLocked(String path) throws StorageClientException;

    /**
     * Refresh the lock keeping the same token.
     * @param path
     * @param timeoutInSeconds
     * @param string
     * @param token
     * @return the token, which should be the same.
     * @throws StorageClientException 
     */
    String refreshLock(String path, long timeoutInSeconds, String extra, String token) throws StorageClientException;

}
