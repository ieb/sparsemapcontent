package org.sakaiproject.nakamura.lite.jdbc.mysql;

import org.sakaiproject.nakamura.lite.authorizable.AbstractAuthorizableManagerImplTest;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;

public class AuthorizableManagerImplMan extends AbstractAuthorizableManagerImplTest {

    @Override
    protected StorageClientPool getClientPool() throws ClassNotFoundException {
        return MysqlSetup.getClientPool();
    }

}
