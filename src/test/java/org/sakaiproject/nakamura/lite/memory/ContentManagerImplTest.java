package org.sakaiproject.nakamura.lite.memory;

import org.sakaiproject.nakamura.lite.content.AbstractContentManagerTest;
import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.mem.MemoryStorageClientConnectionPool;

import com.google.common.collect.ImmutableMap;

public class ContentManagerImplTest extends AbstractContentManagerTest {

    @Override
    protected ConnectionPool getConnectionPool() {
        MemoryStorageClientConnectionPool cp = new MemoryStorageClientConnectionPool();
        cp.activate(ImmutableMap.of("test", (Object) "test",
                BlockContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK, 9));
        return cp;
    }

}
