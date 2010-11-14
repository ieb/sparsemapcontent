package org.sakaiproject.nakamura.lite.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;

public interface BlockContentHelper {

    Map<String, Object> writeBody(String keySpace, String contentColumnFamily, String contentId,
            String contentBlockId, int blockSize, int maxChunksPerBlockSet, InputStream in)
            throws StorageClientException, AccessDeniedException, IOException;

    InputStream readBody(String keySpace, String contentColumnFamily, String contentBlockId,
            int nBlocks) throws StorageClientException, AccessDeniedException;

}
