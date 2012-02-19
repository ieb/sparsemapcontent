package org.sakaiproject.nakamura.lite.storage.spi;

/**
 * Things that implement Disposer can dispose {@link Disposable}s
 * @author ieb
 *
 */
public interface Disposer {

    /**
     * Unregister the disposable
     * @param disposable
     */
    void unregisterDisposable(Disposable disposable);

    /**
     * register the Disposable.
     * @param <T>
     * @param disposable
     * @return the disposable just registered.
     */
    <T extends Disposable> T registerDisposable(T disposable);

}
