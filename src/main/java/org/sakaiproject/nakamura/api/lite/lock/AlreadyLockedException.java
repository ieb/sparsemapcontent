package org.sakaiproject.nakamura.api.lite.lock;

public class AlreadyLockedException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6198174336492911030L;

    public AlreadyLockedException(String path) {
        super("Lock path: "+path);
    }

}
