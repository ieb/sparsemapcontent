package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.io.IOException;
import java.io.InputStream;

import org.sakaiproject.nakamura.lite.storage.Disposable;
import org.sakaiproject.nakamura.lite.storage.Disposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamDisposable implements Disposable {

        private static final Logger LOGGER = LoggerFactory.getLogger(StreamDisposable.class);
        private boolean open = true;
        private Disposer disposer = null;
        private InputStream in;

        public StreamDisposable(InputStream in) {
            this.in = in;
        }
        public void close() {
            if (open && in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage(), e);
                }
                if ( disposer != null ) {
                    disposer.unregisterDisposable(this);
                }
                open = false;
                
            } 
        }
        public void setDisposer(Disposer disposer) {
            this.disposer = disposer;
        }
}
