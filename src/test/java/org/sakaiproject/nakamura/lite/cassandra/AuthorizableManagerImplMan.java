package org.sakaiproject.nakamura.lite.cassandra;

import org.sakaiproject.nakamura.lite.authorizable.AbstractAuthorizableManagerImplTest;
import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.cassandra.CassandraClientConnectionPool;

import com.google.common.collect.ImmutableMap;

public class AuthorizableManagerImplMan extends AbstractAuthorizableManagerImplTest {

    @Override
    protected ConnectionPool getConnectionPool() throws ClassNotFoundException {
        CassandraClientConnectionPool cp = new CassandraClientConnectionPool();
        cp.activate(ImmutableMap.of("test", (Object) "test",
                BlockContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK, 9));
        return cp;
    }

}
