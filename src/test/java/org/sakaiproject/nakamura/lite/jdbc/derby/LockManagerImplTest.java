package org.sakaiproject.nakamura.lite.jdbc.derby;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.lite.lock.AbstractLockManagerImplTest;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;

public class LockManagerImplTest extends AbstractLockManagerImplTest {

    @Override
    protected StorageClientPool getClientPool(Configuration configuration) throws ClassNotFoundException {
        return DerbySetup.getClientPool(configuration);
    }

}
