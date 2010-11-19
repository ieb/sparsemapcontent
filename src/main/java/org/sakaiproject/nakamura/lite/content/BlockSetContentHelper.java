package org.sakaiproject.nakamura.lite.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class BlockSetContentHelper implements BlockContentHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockSetContentHelper.class);
    /**
     * The length of this block
     */
    public static final String BLOCK_LENGTH_FIELD_STUB = "blockLength:";
    /**
     * the stub of all bodies 0..numblocks
     */
   public static final String BODY_FIELD_STUB = "body:";
    /**
     * The number of blocks in this block set
     */
    public static final String NUMBLOCKS_FIELD = "numblocks";

    public static final int DEFAULT_BLOCK_SIZE = 1024 * 1024;
    public static final int DEFAULT_MAX_CHUNKS_PER_BLOCK = 64;

    private StorageClient client;

    public BlockSetContentHelper(StorageClient client) {
        this.client = client;
    }

    public Map<String, Object> writeBody(String keySpace, String contentColumnFamily,
            String contentId, String contentBlockId, int blockSize, int maxChunksPerBlockSet,
            InputStream in) throws StorageClientException, AccessDeniedException, IOException {

        int i = 0;
        int lastBlockWrite = 0;
        long length = 0;
        int bodyNum = 0;
        byte[] buffer = new byte[blockSize];
        for (;;) {
            int offset = 0;
            int nread = 0;
            while (offset < buffer.length) {
                nread = in.read(buffer, offset, buffer.length - offset);
                if (nread < 0) {
                    LOGGER.info("Got to end of stream ");
                    break; // end of input stream, in a block read
                }
                offset += nread;
            }
            LOGGER.info("Read {} bytes ", offset);

            if (offset == 0 && nread < 0) {
                break; // end of the input stream and the block was empty.
            }
            byte[] saveBuffer = buffer;

            if (offset < buffer.length) {
                saveBuffer = new byte[offset];
                System.arraycopy(buffer, 0, saveBuffer, 0, saveBuffer.length);
            }

            String key = contentBlockId + ":" + i;
            String blockLengthKey = BLOCK_LENGTH_FIELD_STUB + bodyNum;
            String bodyKey = BODY_FIELD_STUB + bodyNum;
            int bufferLength = saveBuffer.length;
            length = length + bufferLength;
            lastBlockWrite = i;
            client.insert(keySpace, contentColumnFamily, key, ImmutableMap.of(
                    Content.UUID_FIELD, StorageClientUtils.toStore(contentId),
                    NUMBLOCKS_FIELD, StorageClientUtils.toStore(bodyNum + 1), blockLengthKey,
                    StorageClientUtils.toStore(bufferLength), bodyKey, saveBuffer));
            bodyNum++;
            if (bodyNum > maxChunksPerBlockSet) {
                bodyNum = 0;
                i++;
            }
        }
        Map<String, Object> metadata = Maps.newHashMap();

        metadata.put(Content.BLOCKID_FIELD, StorageClientUtils.toStore(contentBlockId));
        metadata.put(Content.NBLOCKS_FIELD, StorageClientUtils.toStore(lastBlockWrite + 1));
        metadata.put(Content.LENGTH_FIELD, StorageClientUtils.toStore(length));
        metadata.put(Content.BLOCKSIZE_FIELD, StorageClientUtils.toStore(blockSize));
        
        LOGGER.info("Saved Last block ContentID {} BlockID {} Nblocks {}  length {}  blocksize {} ",new Object[] {contentId,contentBlockId, lastBlockWrite+1, length, blockSize});
        return metadata;

    }

    @Override
    public InputStream readBody(String keySpace, String contentColumnFamily, String contentBlockId,
            int nBlocks) throws StorageClientException, AccessDeniedException {
        return new BlockContentInputStream(client, keySpace, contentColumnFamily, contentBlockId, nBlocks);
    }

}
