package org.sakaiproject.nakamura.lite.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
import org.sakaiproject.nakamura.lite.Security;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * <pre>
 * Content Manager.
 * Manages two types of content,
 * Bundles of content properties and bodies.
 * Bodies are chunked into sizes to aide efficiency when retrieving the content.
 * 
 * CF content stores the structure of the content keyed by path.
 * Each item contains child names in columns + the guid of the item
 * eg
 *   path : {
 *       ':id' : thisitemUUID,
 *       subitemA : subitemAUUID,
 *       subitemB : subitemBUUID
 *   }
 * the guid of the item points to the CF content version where items are keyed by the version.
 * These items also contain child nodes under children as an array
 * 
 * eg
 *    itemUUID : {
 *         'id' : thisitemUUID
 *         'children' : [ 
 *           subitemA : subitemAUUID,
 *           subitemB : subitemBUUID
 *         ],
 *         'nblocks' = numberOfBlocksSetsOfContent
 *         'length' = totalLenghtOftheContent
 *         'blocksize' = storageBlockSize
 *         'blockid' = blockID
 *         ... other properties ...
 *    }
 *    
 * The content blocks are stored in CF content body
 * eg
 *   blockID:blockSetNumber : {
 *         'id' : blockID,
 *         'numblocks' : numberOfBlocksInThisSet,
 *         'blocklength0' : lengthOfThisBlock,
 *         'body0' : byte[]
 *         'blocklength1' : lengthOfThisBlock,
 *         'body1' : byte[]
 *         ...
 *         'blocklengthn' : lengthOfThisBlock,
 *         'bodyn' : byte[]
 *    }
 * 
 * 
 * Versioning:
 * 
 * When a version is saved, the CF contentVersion item is cloned and the CF content :id and any subitems IDs are updated.
 * Block 0 is marked as readonly
 * 
 * When the body is written to its CF content row is checked to see if the block is read only. If so a new block is created with and linked in with 'previousversion'
 * 
 * </pre>
 * 
 * @author ieb
 * 
 */
public class ContentManager {

    /**
     * Key containing deleted items.
     */
    private static final String DELETEDITEMS_KEY = ":deleteditems";

    /**
     * The ID of a content item
     */
    private static final String UUID_FIELD = "id";
    /**
     * The path of the content item
     */
    private static final String PATH_FIELD = "path";
    /**
     * content item ID referenced by a Strucutre item
     */
    private static final String STRUCTURE_UUID_FIELD = ":cid";
    /**
     * BlockID where the body of this content item is stored, if there is a body
     */
    private static final String BLOCKID_FIELD = "blockId";
    /**
     * ID of the previous version
     */
    private static final String PREVIOUS_VERSION_UUID_FIELD = "previousVersion";
    /**
     * Previous Block ID.
     */
    private static final String PREVIOUS_BLOCKID_FIELD = "previousBlockId";
    /**
     * The ID of the next version
     */
    private static final String NEXT_VERSION_FIELD = "nextVersion";
    /**
     * Set to "Y" if the content item is read only.
     */
    private static final String READONLY_FIELD = "readOnly";
    /**
     * set to "Y" if deleted.
     */
    private static final String DELETED_FIELD = "deleted";

    /**
     * The block size in bytes in each block in a block set
     */
    private static final String BLOCKSIZE_FIELD = "blocksize";
    /**
     * Total length of the content body
     */
    private static final String LENGTH_FIELD = "length";
    /**
     * The number of block sets in a body
     */
    private static final String NBLOCKS_FIELD = "nblocks";
    /**
     * The number of blocks in this block set
     */
    private static final String NUMBLOCKS_FIELD = "numblocks";
    /**
     * the stub of all bodies 0..numblocks
     */
    private static final String BODY_FIELD_STUB = "body:";
    /**
     * The length of this block
     */
    private static final String BLOCK_LENGTH_FIELD_STUB = "blockLength:";
    /**
     * Yes, True, etc
     */
    private static final String TRUE = "Y";
    /**
     * Yes, True etc in byte form
     */
    private static final byte[] TRUE_B = TRUE.getBytes();
    /**
     * Block size (1MB)
     */
    private static final int BLOCK_SIZE = 1024 * 1024; // 1MB per block
    /**
     * Max block set size (64MB)
     */
    private static final int MAX_CHUNKS = 64; // 64MB per row

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentManager.class);
    private StorageClient client;
    private AccessControlManager accessControlManager;
    private String keySpace;
    private String contentColumnFamily;

    public ContentManager(StorageClient client, AccessControlManager accessControlManager) {
        this.client = client;
        this.accessControlManager = accessControlManager;
    }

    public Content get(String path) throws StorageClientException, AccessDeniedException {
        accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
        Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
        String contentId = StorageClientUtils
                .toString(structure.get(STRUCTURE_UUID_FIELD));
        Map<String, Object> content = client.get(keySpace, contentColumnFamily, contentId);
        if (content != null) {
            return new Content(path, structure, content, this);
        }
        return null;

    }

    public void saveVersion(String path) throws StorageClientException, AccessDeniedException {
        accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
        Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
        String contentId = StorageClientUtils
                .toString(structure.get(STRUCTURE_UUID_FIELD));
        Map<String, Object> saveVersion = client.get(keySpace, contentColumnFamily, contentId);
        Map<String, Object> newVersion = Maps.newHashMap(saveVersion);
        String newVersionId = StorageClientUtils.getUuid();
        String saveVersionId = StorageClientUtils.toString(saveVersion.get(UUID_FIELD));
        String saveBlockId = StorageClientUtils.toString(saveVersion.get(BLOCKID_FIELD));

        Object newVersionIdS = StorageClientUtils.toStore(newVersionId);
        Object saveVersionIdS = StorageClientUtils.toStore(saveVersionId);
        Object saveBlockIdS = StorageClientUtils.toStore(saveBlockId);

        newVersion.put(UUID_FIELD, newVersionIdS);
        newVersion.put(PREVIOUS_VERSION_UUID_FIELD, saveVersionIdS);
        newVersion.put(PREVIOUS_BLOCKID_FIELD, saveBlockIdS);

        saveVersion.put(NEXT_VERSION_FIELD, newVersionIdS);
        saveVersion.put(READONLY_FIELD, TRUE_B);

        client.insert(keySpace, contentColumnFamily, saveVersionId, saveVersion);
        client.insert(keySpace, contentColumnFamily, newVersionId, newVersion);
        client.insert(keySpace, contentColumnFamily, path,
                ImmutableMap.of(STRUCTURE_UUID_FIELD, newVersionIdS));
        client.insert(keySpace, contentColumnFamily, StorageClientUtils.getParentObjectPath(path),
                ImmutableMap.of(StorageClientUtils.getObjectName(path), newVersionIdS));

    }

    public void update(Content content) throws AccessDeniedException, StorageClientException {
        String path = content.getPath();
        accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
        String id = null;
        Object idStore = null;
        Map<String, Object> toSave = null;
        Map<String, Object> contentPropertes = content.getContent();
        if ( content.isNew() ) {
            toSave = Maps.newHashMap(contentPropertes);            
            id = StorageClientUtils.getUuid();
            idStore = StorageClientUtils.toStore(id);
            toSave.put(UUID_FIELD, idStore);
            toSave.put(PATH_FIELD, StorageClientUtils.toStore(path));
        } else if ( content.isUpdated() ) {
            toSave = Maps.newHashMap(content.getUpdated());
            id = StorageClientUtils.toString(contentPropertes.get(UUID_FIELD));
            idStore = StorageClientUtils.toStore(contentPropertes.get(UUID_FIELD));            
        } else {
            // if not new or updated, dont update.
            return;
        }
        
        Map<String, Object> checkContent = client.get(keySpace, contentColumnFamily, id);
        if (TRUE.equals(StorageClientUtils.toString(checkContent.get(READONLY_FIELD)))) {
            throw new AccessDeniedException(Security.ZONE_CONTENT, path, "Read only Content Item");
        }
        if ( content.isNew() ) {
            // only when new do we update the structure.
            if ( !StorageClientUtils.isRoot(path)) {
                client.insert(keySpace, contentColumnFamily, StorageClientUtils.getParentObjectPath(path),
                    ImmutableMap.of(StorageClientUtils.getObjectName(path), idStore));
            }
            client.insert(keySpace, contentColumnFamily, path,
                    ImmutableMap.of(STRUCTURE_UUID_FIELD, idStore));
        }
        // save the content id.
        client.insert(keySpace, contentColumnFamily, id, toSave);
        LOGGER.info("Saved {} at {} as {} ", new Object[] { path, id, toSave });
        // reset state to unmodified to take further modifications.
        content.reset();
    }

    public void delete(String path) throws AccessDeniedException, StorageClientException {
        accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_DELETE);
        Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
        String uuid = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
        client.remove(keySpace, contentColumnFamily, path);
        client.insert(keySpace, contentColumnFamily, StorageClientUtils.getParentObjectPath(path),
                ImmutableMap.of(StorageClientUtils.getObjectName(path), null));
        client.insert(keySpace, contentColumnFamily, uuid,
                ImmutableMap.of(DELETED_FIELD, (Object) TRUE_B));
        client.insert(keySpace, contentColumnFamily, DELETEDITEMS_KEY,
                ImmutableMap.of(uuid, StorageClientUtils.toStore(path)));
    }

    public long writeBody(String path, InputStream in) throws StorageClientException,
            AccessDeniedException, IOException {
        accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
        Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
        String contentId = StorageClientUtils
                .toString(structure.get(STRUCTURE_UUID_FIELD));
        Map<String, Object> content = client.get(keySpace, contentColumnFamily, contentId);

        String contentBlockId = null;
        if (content.containsKey(BLOCKID_FIELD)) {
            contentBlockId = StorageClientUtils.toString(content.get(BLOCKID_FIELD));
        } else {
            contentBlockId = StorageClientUtils.getUuid();
        }
        int i = 0;
        int lastBlockWrite = 0;
        long length = 0;
        int bodyNum = 0;
        byte[] buffer = new byte[BLOCK_SIZE];
        for (;;) {
            int offset = 0;
            int nread = 0;
            while (offset < buffer.length) {
                nread = in.read(buffer, offset, buffer.length - offset);
                if (nread < 0) {
                    break; // end of input stream, in a block read
                }
                offset += nread;
            }
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
            client.insert(keySpace, contentColumnFamily, key, ImmutableMap.of(UUID_FIELD,
                    StorageClientUtils.toStore(contentId), NUMBLOCKS_FIELD,
                    StorageClientUtils.toStore(bodyNum), blockLengthKey,
                    StorageClientUtils.toStore(bufferLength), bodyKey, saveBuffer));
            bodyNum++;
            if (bodyNum > MAX_CHUNKS) {
                bodyNum = 0;
                i++;
            }
        }

        client.insert(keySpace, contentColumnFamily, contentId, ImmutableMap.of(BLOCKID_FIELD,
                StorageClientUtils.toStore(contentBlockId), NBLOCKS_FIELD,
                StorageClientUtils.toStore(lastBlockWrite), LENGTH_FIELD,
                StorageClientUtils.toStore(length), BLOCKSIZE_FIELD,
                StorageClientUtils.toStore(BLOCK_SIZE)));
        return length;

    }

    public InputStream getInputStream(String path) throws StorageClientException,
            AccessDeniedException {
        return new ContentInputStream(path);
    }

    public class ContentInputStream extends InputStream {

        private byte[] buffer;
        private String blockId;
        private int nBlocks;
        private int blockLength;
        private int offset;
        private int currentBlockSet;
        private int currentBlockNumber;
        private int blocksInSet;
        private Map<String, Object> block;

        ContentInputStream(String path) throws StorageClientException, AccessDeniedException {
            accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
            Map<String, Object> content = client.get(keySpace, contentColumnFamily, path);
            blockId = StorageClientUtils.toString(content.get(BLOCKID_FIELD));
            nBlocks = StorageClientUtils.toInt(content.get(NBLOCKS_FIELD));
            currentBlockSet = -1;
            currentBlockNumber = 0;
            blocksInSet = -1;

        }

        @Override
        public int read() throws IOException {
            if (buffer == null) {
                if (!readBuffer()) {
                    return -1;
                }
            }
            if (offset > blockLength) {
                if (!readBuffer()) {
                    return -1;
                }
            }
            return buffer[offset++];
        }

        private boolean readBuffer() throws IOException {
            if (currentBlockNumber > blocksInSet) {
                if (currentBlockSet + 1 == nBlocks) {
                    return false;
                }
                currentBlockSet++;
                String blockKey = blockId + ":" + currentBlockSet;
                try {
                    block = client.get(keySpace, contentColumnFamily, blockKey);
                } catch (StorageClientException e) {
                    throw new IOException(e.getMessage(), e);
                }
                currentBlockNumber = 0;
                blocksInSet = StorageClientUtils.toInt(block.get(NUMBLOCKS_FIELD));
            }
            blockLength = StorageClientUtils.toInt(block.get(BLOCK_LENGTH_FIELD_STUB
                    + currentBlockNumber));
            buffer = (byte[]) block.get(BODY_FIELD_STUB + currentBlockNumber);
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
                        return skipped;
                    }
                }
            }
            return skipped;
        }

    }
}
