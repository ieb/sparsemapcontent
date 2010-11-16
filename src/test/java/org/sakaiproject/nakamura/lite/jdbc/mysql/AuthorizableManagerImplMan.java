package org.sakaiproject.nakamura.lite.jdbc.mysql;

import org.sakaiproject.nakamura.lite.authorizable.AbstractAuthorizableManagerImplTest;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;

public class AuthorizableManagerImplMan extends AbstractAuthorizableManagerImplTest {

    @Override
    protected ConnectionPool getConnectionPool() {
        return MysqlSetup.getConnectionPool();
    }

}
