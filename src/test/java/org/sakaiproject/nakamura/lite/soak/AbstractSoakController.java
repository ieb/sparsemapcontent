package org.sakaiproject.nakamura.lite.soak;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;

/**
 * A controller for a multi threaded test.
 * 
 * @author ieb
 * 
 */
public abstract class AbstractSoakController {

    public void launchSoak(int nthreads) throws ConnectionPoolException, StorageClientException,
            AccessDeniedException {
        for (int tr = 1; tr <= nthreads; tr++) {
            long s = System.currentTimeMillis();
            Thread[] threads = new Thread[tr];
            for (int t = 0; t < tr; t++) {
                threads[t] = new Thread(getRunnable(tr));
                threads[t].start();
            }
            for (int t = 0; t < tr; t++) {
                try {
                    threads[t].join();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            long e = System.currentTimeMillis();
            double t = (e - s) / ((double) 1000);
            logRate(t, tr);
        }

    }

    protected abstract void logRate(double t, int currentThreads);

    protected abstract Runnable getRunnable(int tr) throws ConnectionPoolException,
            StorageClientException, AccessDeniedException;
}
