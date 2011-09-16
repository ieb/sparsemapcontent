package org.sakaiproject.nakamura.lite.hbase;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
import org.sakaiproject.nakamura.lite.lock.AbstractLockManagerImplTest;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;
import org.sakaiproject.nakamura.lite.storage.hbase.HBaseStorageClientPool;

import com.google.common.collect.ImmutableMap;

public class LockManagerImplMan extends AbstractLockManagerImplTest {

  @Override
  protected StorageClientPool getClientPool(Configuration configuration)
      throws ClassNotFoundException {
      if ( true ) {
          return null;
      }
    HBaseStorageClientPool cp = new HBaseStorageClientPool();
    cp.activate(ImmutableMap.of("test", (Object) "test",
        BlockContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK, 9, Configuration.class.getName(),
        configuration));
    return cp;
  }

}
