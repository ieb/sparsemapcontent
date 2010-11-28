package org.sakaiproject.nakamura.lite.jdbc.derby;

import org.sakaiproject.nakamura.lite.content.AbstractContentManagerTest;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;

public class ContentManagerManagerImplTest extends AbstractContentManagerTest {

    @Override
    protected StorageClientPool getClientPool() throws ClassNotFoundException {
        return DerbySetup.getClientPool();
    }

}
