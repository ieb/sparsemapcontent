package org.sakaiproject.nakamura.lite.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockContentInputStream extends InputStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockContentInputStream.class);
    private byte[] buffer;
    private String blockId;
    private int nBlocks;
    private int blockLength;
    private int offset;
    private int currentBlockSet;
    private int currentBlockNumber;
    private int blocksInSet;
    private Map<String, Object> block;
    private String keySpace;
    private String contentColumnFamily;
    private StorageClient client;

    public BlockContentInputStream(StorageClient client, String keySpace, String contentColumnFamily,
            String blockId, int nBlocks) throws StorageClientException, AccessDeniedException {

        this.blockId = blockId;
        this.nBlocks = nBlocks;
        this.keySpace = keySpace;
        this.contentColumnFamily = contentColumnFamily;
        this.client = client;

        currentBlockSet = -1;
        currentBlockNumber = -1;
        blocksInSet = -1;

    }

    @Override
    public int read() throws IOException {
        if (buffer == null) {
            if (!readBuffer()) {
                return -1;
            }
        }
        if (offset >= buffer.length) {
            if (!readBuffer()) {
                return -1;
            }
        }
        int v = (int) buffer[offset] & 0xff;
        offset++;
        return v;
    }

    private boolean readBuffer() throws IOException {
        currentBlockNumber++;
        if (currentBlockNumber >= blocksInSet) {
            LOGGER.info("No more blocks In set, next set ? blocks {} {} ", currentBlockSet, nBlocks);
            if (currentBlockSet + 1 == nBlocks) {
                LOGGER.info("No more blocks {} {} ", currentBlockSet, nBlocks);
                return false;
            }
            currentBlockSet++;
            String blockKey = blockId + ":" + currentBlockSet;
            try {
                block = client.get(keySpace, contentColumnFamily, blockKey);
                LOGGER.info("New Block Loaded {} {} ", blockKey, block);
            } catch (StorageClientException e) {
                throw new IOException(e.getMessage(), e);
            }
            currentBlockNumber = 0;
            blocksInSet = StorageClientUtils.toInt(block
                    .get(BlockSetContentHelper.NUMBLOCKS_FIELD));
            LOGGER.info("Loaded New Block Set {}  containing {} blocks ", currentBlockSet,
                    blocksInSet);

        }

        LOGGER.info("Loading block {} {} ", currentBlockNumber, blocksInSet);
        blockLength = StorageClientUtils.toInt(block
                .get(BlockSetContentHelper.BLOCK_LENGTH_FIELD_STUB + currentBlockNumber));
        buffer = (byte[]) block
                .get(BlockSetContentHelper.BODY_FIELD_STUB + currentBlockNumber);
        offset = 0;
        LOGGER.info("Loaded Buffer {} {} size {} ", new Object[] { currentBlockSet,
                currentBlockNumber, buffer.length });
        return true;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = 0;
        while (n > 0) {
            if (n < blockLength - offset) {
                offset += n;
                skipped += n;
                n = 0;
            } else {
                n = n - (blockLength - offset);
                skipped += (blockLength - offset);
                if (!readBuffer()) {
                    LOGGER.info("Skipped over EOF {} ", skipped);
                    return skipped;
                }
                LOGGER.info("Skipped Partial {} ", skipped);
            }
        }
        LOGGER.debug("Skipped Final {} ", skipped);
        return skipped;
    }

}