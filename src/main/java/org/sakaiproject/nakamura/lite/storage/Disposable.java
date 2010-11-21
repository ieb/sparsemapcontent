package org.sakaiproject.nakamura.lite.storage;

/**
 * Things that are disposable, must be closed.
 * @author ieb
 *
 */
public interface Disposable {

    void close();

}
