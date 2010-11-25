package org.sakaiproject.nakamura.lite.jdbc.mysql;

import org.sakaiproject.nakamura.lite.accesscontrol.AbstractAccessControlManagerImplTest;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;

public class AccessControlManagerImplMan extends AbstractAccessControlManagerImplTest {

    @Override
    protected StorageClientPool getClientPool() throws ClassNotFoundException {
        return MysqlSetup.getClientPool();
    }

}
