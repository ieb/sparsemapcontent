package org.sakaiproject.nakamura.lite;

import junit.framework.Assert;

import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Type1UUIDSoak {

    
    protected static final Logger LOGGER = LoggerFactory.getLogger(Type1UUIDSoak.class);
    protected static final int TEST_SIZE = 100000;
    private static final int N_THREADS = 4;
    protected Map<String,Long> check = new ConcurrentHashMap<String, Long>(TEST_SIZE*N_THREADS);
    private int errors;

    @Test
    public void testType1UUID() {
        errors = 0;
        Thread[] t = new Thread[N_THREADS];
        for ( int i = 0; i < t.length; i++ ) {
            t[i] = new Thread(new Runnable() {
                

                public void run() {
                    for ( int i = 0; i < TEST_SIZE; i++ ) {
                        String id = StorageClientUtils.getUuid();
                        if ( check.containsKey(id)) {
                            LOGGER.error("Collision {} ago {} ",id, System.currentTimeMillis()-check.get(id));
                            errors++;
                            //treturn;
                        }
                        check.put(id,System.currentTimeMillis());
                    }
                    LOGGER.info("Completed {} ",TEST_SIZE);
                };
            });
            t[i].start();
        }
        for ( int i = 0; i < t.length; i++ ) {
            try {
                t[i].join();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(),e);
            }
        }
        Assert.assertEquals("Collided "+errors+" times out of "+(TEST_SIZE*N_THREADS)+"  ie about "+((100*errors)/(TEST_SIZE*N_THREADS))+"%",0, errors);
        
    }
}
