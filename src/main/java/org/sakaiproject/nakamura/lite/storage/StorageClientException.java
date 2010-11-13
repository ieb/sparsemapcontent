package org.sakaiproject.nakamura.lite.storage;

public class StorageClientException extends Exception {

    public StorageClientException(String message, Throwable t) {
        super(message, t);
    }

    public StorageClientException(String message) {
        super(message);
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 2026297729817830516L;

}
