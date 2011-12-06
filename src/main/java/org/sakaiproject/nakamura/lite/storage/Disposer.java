package org.sakaiproject.nakamura.lite.storage;

public interface Disposer {

    void unregisterDisposable(Disposable disposable);

    <T extends Disposable> T registerDisposable(T disposable);

}
