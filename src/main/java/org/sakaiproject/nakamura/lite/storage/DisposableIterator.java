package org.sakaiproject.nakamura.lite.storage;

import java.util.Iterator;

/**
 * Disposable Iterators must be closed when they have been used.
 * @author ieb
 *
 * @param <T>
 */
public interface DisposableIterator<T> extends Iterator<T>, Disposable {
}
