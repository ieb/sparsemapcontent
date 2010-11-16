package org.sakaiproject.nakamura.lite.jdbc.derby;

import org.sakaiproject.nakamura.lite.content.AbstractContentManagerTest;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;

public class ContentManagerManagerImplTest extends AbstractContentManagerTest{

    @Override
    protected ConnectionPool getConnectionPool() throws ClassNotFoundException {
        return DerbySetup.getConnectionPool();
    }

}
