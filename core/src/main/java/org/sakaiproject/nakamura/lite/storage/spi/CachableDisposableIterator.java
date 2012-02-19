package org.sakaiproject.nakamura.lite.storage.spi;

import java.util.Map;

/**
 * These iterators can be cached. They must provide a results map that may be
 * used later to create a new iterator from the cached results.
 * 
 * @author ieb
 * 
 * @param <T>
 */
public interface CachableDisposableIterator<T> extends DisposableIterator<T> {

    Map<String, Object> getResultsMap();

}
