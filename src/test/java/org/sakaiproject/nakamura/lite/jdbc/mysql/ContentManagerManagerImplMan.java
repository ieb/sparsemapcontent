package org.sakaiproject.nakamura.lite.jdbc.mysql;

import org.sakaiproject.nakamura.lite.content.AbstractContentManagerTest;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;

public class ContentManagerManagerImplMan extends AbstractContentManagerTest{

    @Override
    protected ConnectionPool getConnectionPool() throws ClassNotFoundException {
        return MysqlSetup.getConnectionPool();
    }

}
