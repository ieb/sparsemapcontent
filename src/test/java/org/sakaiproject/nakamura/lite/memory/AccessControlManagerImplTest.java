package org.sakaiproject.nakamura.lite.memory;

import org.sakaiproject.nakamura.lite.accesscontrol.AbstractAccessControlManagerImplTest;
import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;
import org.sakaiproject.nakamura.lite.storage.mem.MemoryStorageClientPool;

import com.google.common.collect.ImmutableMap;

public class AccessControlManagerImplTest extends AbstractAccessControlManagerImplTest {

    @Override
    protected StorageClientPool getClientPool() throws ClassNotFoundException {
        MemoryStorageClientPool cp = new MemoryStorageClientPool();
        cp.activate(ImmutableMap.of("test", (Object) "test",
                BlockContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK, 9));
        return cp;
    }

}
