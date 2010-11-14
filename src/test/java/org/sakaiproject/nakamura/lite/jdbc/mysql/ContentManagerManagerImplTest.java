package org.sakaiproject.nakamura.lite.jdbc.mysql;

import org.sakaiproject.nakamura.lite.content.AbstractContentManagerTest;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;

public class ContentManagerManagerImplTest extends AbstractContentManagerTest{

    @Override
    protected ConnectionPool getConnectionPool() {
        return MysqlSetup.getConnectionPool();
    }

}
