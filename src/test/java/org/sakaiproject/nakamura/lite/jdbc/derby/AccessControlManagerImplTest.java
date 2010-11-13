package org.sakaiproject.nakamura.lite.jdbc.derby;

import org.sakaiproject.nakamura.lite.accesscontrol.AbstractAccessControlManagerImplTest;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;

public class AccessControlManagerImplTest extends AbstractAccessControlManagerImplTest {

    @Override
    protected ConnectionPool getConnectionPool() {
        return DerbySetup.getConnectionPool();
    }

}
