package org.sakaiproject.nakamura.lite.jdbc.derby;

import org.sakaiproject.nakamura.lite.authorizable.AbstractAuthorizableManagerImplTest;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;

public class AuthorizableManagerImplTest extends AbstractAuthorizableManagerImplTest {

    @Override
    protected ConnectionPool getConnectionPool() throws ClassNotFoundException {
        return DerbySetup.getConnectionPool();
    }

}
