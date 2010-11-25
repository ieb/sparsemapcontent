package org.sakaiproject.nakamura.lite.jdbc.derby;

import org.sakaiproject.nakamura.lite.accesscontrol.AbstractAccessControlManagerImplTest;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;

public class AccessControlManagerImplTest extends AbstractAccessControlManagerImplTest {

    @Override
    protected StorageClientPool getClientPool() throws ClassNotFoundException {
        return DerbySetup.getClientPool();
    }

}
