/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.lite.content;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClient;
import org.sakaiproject.nakamura.lite.storage.spi.content.BlockSetContentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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

    public BlockContentInputStream(StorageClient client, String keySpace,
            String contentColumnFamily, String blockId, int nBlocks) throws StorageClientException,
            AccessDeniedException {

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
            LOGGER.debug("No more blocks In set, next set ? blocks {} {} ", currentBlockSet, nBlocks);
            if (currentBlockSet + 1 == nBlocks) {
                LOGGER.debug("No more blocks {} {} ", currentBlockSet, nBlocks);
                return false;
            }
            currentBlockSet++;
            String blockKey = blockId + ":" + currentBlockSet;
            try {
                block = client.get(keySpace, contentColumnFamily, blockKey);
                LOGGER.debug("New Block Loaded {} {} ", blockKey, block);
            } catch (StorageClientException e) {
                throw new IOException(e.getMessage(), e);
            }
            currentBlockNumber = 0;
            blocksInSet = toInt(block.get(BlockSetContentHelper.NUMBLOCKS_FIELD));
            LOGGER.debug("Loaded New Block Set {}  containing {} blocks ", currentBlockSet,
                    blocksInSet);

        }

        LOGGER.debug("Loading block {} {} ", currentBlockNumber, blocksInSet);
        blockLength = toInt(block
                .get(BlockSetContentHelper.BLOCK_LENGTH_FIELD_STUB + currentBlockNumber));
        buffer = (byte[]) block.get(BlockSetContentHelper.BODY_FIELD_STUB + currentBlockNumber);
        offset = 0;
        LOGGER.debug("Loaded Buffer {} {} size {} ", new Object[] { currentBlockSet,
                currentBlockNumber, buffer.length });
        return true;
    }

    private int toInt(Object object) {
        if (object instanceof Integer) {
            return ((Integer) object).intValue();
        }
        return 0;
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
                    LOGGER.debug("Skipped over EOF {} ", skipped);
                    return skipped;
                }
                LOGGER.debug("Skipped Partial {} ", skipped);
            }
        }
        LOGGER.debug("Skipped Final {} ", skipped);
        return skipped;
    }

}