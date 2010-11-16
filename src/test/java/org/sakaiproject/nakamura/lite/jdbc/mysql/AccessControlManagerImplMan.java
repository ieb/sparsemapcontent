package org.sakaiproject.nakamura.lite.jdbc.mysql;

import org.sakaiproject.nakamura.lite.accesscontrol.AbstractAccessControlManagerImplTest;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;

public class AccessControlManagerImplMan extends AbstractAccessControlManagerImplTest {

    @Override
    protected ConnectionPool getConnectionPool() {
        return MysqlSetup.getConnectionPool();
    }

}
