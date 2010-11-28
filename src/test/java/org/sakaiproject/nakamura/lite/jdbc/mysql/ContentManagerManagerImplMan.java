package org.sakaiproject.nakamura.lite.jdbc.mysql;

import org.sakaiproject.nakamura.lite.content.AbstractContentManagerTest;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;

public class ContentManagerManagerImplMan extends AbstractContentManagerTest {

    @Override
    protected StorageClientPool getClientPool() throws ClassNotFoundException {
        return MysqlSetup.getClientPool();
    }

}
