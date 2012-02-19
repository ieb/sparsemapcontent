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
package org.sakaiproject.nakamura.lite.storage.spi.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.lite.content.BlockContentInputStream;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class BlockSetContentHelper implements BlockContentHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockSetContentHelper.class);
    public static final String CONTENT_BLOCK_ID = Repository.SYSTEM_PROP_PREFIX + "cblockId";
    /**
     * The length of this block
     */
    public static final String BLOCK_LENGTH_FIELD_STUB = Repository.SYSTEM_PROP_PREFIX + "blockLength:";
    /**
     * the stub of all bodies 0..numblocks
     */
    public static final String BODY_FIELD_STUB = Repository.SYSTEM_PROP_PREFIX + "body:";
    /**
     * The number of blocks in this block set
     */
    public static final String NUMBLOCKS_FIELD = Repository.SYSTEM_PROP_PREFIX + "numblocks";

    public static final int DEFAULT_BLOCK_SIZE = 1024 * 1024;
    public static final int DEFAULT_MAX_CHUNKS_PER_BLOCK = 64;

    private StorageClient client;

    public BlockSetContentHelper(StorageClient client) {
        this.client = client;
    }

    public Map<String, Object> writeBody(String keySpace, String contentColumnFamily,
            String contentId, String contentBlockId, String streamId, int blockSize, int maxChunksPerBlockSet,
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
                    LOGGER.debug("Got to end of stream ");
                    break; // end of input stream, in a block read
                }
                offset += nread;
            }
            LOGGER.debug("Read {} bytes ", offset);

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
            client.insert(keySpace, contentColumnFamily, key, ImmutableMap.of(Content.UUID_FIELD,
                    (Object)contentId, 
                    CONTENT_BLOCK_ID, key,
                    NUMBLOCKS_FIELD,
                    bodyNum + 1, blockLengthKey,
                    bufferLength, bodyKey, saveBuffer), false);
            bodyNum++;
            if (bodyNum > maxChunksPerBlockSet) {
                bodyNum = 0;
                i++;
            }
        }
        Map<String, Object> metadata = Maps.newHashMap();

        metadata.put(StorageClientUtils.getAltField(Content.BLOCKID_FIELD, streamId), contentBlockId);
        metadata.put(StorageClientUtils.getAltField(Content.NBLOCKS_FIELD, streamId), lastBlockWrite + 1);
        metadata.put(StorageClientUtils.getAltField(Content.LENGTH_FIELD, streamId), length);
        metadata.put(StorageClientUtils.getAltField(Content.BLOCKSIZE_FIELD, streamId), blockSize);

        LOGGER.debug(
                "Saved Last block ContentID {} BlockID {} Nblocks {}  length {}  blocksize {} ",
                new Object[] { contentId, contentBlockId, lastBlockWrite + 1, length, blockSize });
        return metadata;

    }

    public InputStream readBody(String keySpace, String contentColumnFamily, String contentBlockId, String streamId,
            int nBlocks) throws StorageClientException, AccessDeniedException {
        // all the information is stored against the contentBlockId which is unique to the stream
        return new BlockContentInputStream(client, keySpace, contentColumnFamily, contentBlockId, 
                nBlocks);
    }

    public boolean hasBody(Map<String, Object> content, String streamId) {
        return content.containsKey(StorageClientUtils.getAltField(Content.BLOCKID_FIELD, streamId));
    }
}
