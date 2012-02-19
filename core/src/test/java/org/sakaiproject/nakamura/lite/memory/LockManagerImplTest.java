package org.sakaiproject.nakamura.lite.memory;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.lite.lock.AbstractLockManagerImplTest;
import org.sakaiproject.nakamura.lite.storage.mem.MemoryStorageClientPool;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;
import org.sakaiproject.nakamura.lite.storage.spi.content.BlockContentHelper;

import com.google.common.collect.ImmutableMap;

public class LockManagerImplTest extends AbstractLockManagerImplTest {

    @Override
    protected StorageClientPool getClientPool(Configuration configuration) throws ClassNotFoundException {
        MemoryStorageClientPool cp = new MemoryStorageClientPool();
        cp.activate(ImmutableMap.of("test", (Object) "test",
                BlockContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK, 9,
                Configuration.class.getName(), configuration));
        return cp;
    }
}
