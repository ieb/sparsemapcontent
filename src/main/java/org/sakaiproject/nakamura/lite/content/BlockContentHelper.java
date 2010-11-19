package org.sakaiproject.nakamura.lite.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

public interface BlockContentHelper {

    public static final String CONFIG_BLOCK_SIZE = "block-size";
    public static final String CONFIG_MAX_CHUNKS_PER_BLOCK = "chunks-per-block";


    Map<String, Object> writeBody(String keySpace, String contentColumnFamily, String contentId,
            String contentBlockId, int blockSize, int maxChunksPerBlockSet, InputStream in)
            throws StorageClientException, AccessDeniedException, IOException;

    InputStream readBody(String keySpace, String contentColumnFamily, String contentBlockId,
            int nBlocks) throws StorageClientException, AccessDeniedException;

}
