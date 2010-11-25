package org.sakaiproject.nakamura.lite.storage;

/**
 * This class need to bind weakly to a thread, so when the thread dies, the object gets a unbind
 * @author ieb
 *
 * @param <T>
 */
public class ReferenceCountThreadLocal<T> {

    
    
    public class Holder {

        private int ref;
        private T payload;

        public Holder(T payload) {
            this.payload = payload;
        }

        public T get() {
            return payload;
        }

        public int inc() {
            ref++;
            return ref;
        }

        public int dec() {
            ref--;
            return ref;
        }

    }

    private ThreadLocal<Holder> store = new ThreadLocal<Holder>();

    public T get() {
        Holder h = store .get();
        if ( h == null ) {
            return null;
        }
        h.inc();
        return h.get();
    }

    public void set(T client) {
        store.set(new Holder(client));
        
    }

    public T release() {
        Holder h = store.get();
        if ( h == null ) {
            return null;
        }
        if ( h.dec() == 0 ) {
            store.set(null);
            return h.get();
        }
        return null;
    }
    

}
