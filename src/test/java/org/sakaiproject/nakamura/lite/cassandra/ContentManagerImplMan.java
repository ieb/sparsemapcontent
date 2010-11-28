package org.sakaiproject.nakamura.lite.cassandra;

import com.google.common.collect.ImmutableMap;

import org.sakaiproject.nakamura.lite.content.AbstractContentManagerTest;
import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;
import org.sakaiproject.nakamura.lite.storage.cassandra.CassandraClientPool;

public class ContentManagerImplMan extends AbstractContentManagerTest {

    @Override
    protected StorageClientPool getClientPool() throws ClassNotFoundException {
        CassandraClientPool cp = new CassandraClientPool();
        cp.activate(ImmutableMap.of("test", (Object) "test",
                BlockContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK, 9));
        return cp;
    }

}
