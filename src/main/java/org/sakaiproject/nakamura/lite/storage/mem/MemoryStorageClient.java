package org.sakaiproject.nakamura.lite.storage.mem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
import org.sakaiproject.nakamura.lite.content.BlockSetContentHelper;
import org.sakaiproject.nakamura.lite.storage.DisposableIterator;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryStorageClient implements StorageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryStorageClient.class);
    Map<String, Map<String, Object>> store;
    private int blockSize;
    private int maxChunksPerBlockSet;
    private BlockContentHelper contentHelper;
    private MemoryStorageClientPool pool;

    public MemoryStorageClient(MemoryStorageClientPool pool, Map<String, Map<String, Object>> store,
            Map<String, Object> properties) {
        this.store = store;
        this.pool = pool;
        contentHelper = new BlockSetContentHelper(this);
        blockSize = StorageClientUtils.getSetting(properties.get(BlockSetContentHelper.CONFIG_BLOCK_SIZE),
                BlockSetContentHelper.DEFAULT_BLOCK_SIZE);
        maxChunksPerBlockSet = StorageClientUtils.getSetting(
                properties.get(BlockSetContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK), BlockSetContentHelper.DEFAULT_MAX_CHUNKS_PER_BLOCK);

    }
    
    public void close() {
        pool.releaseClient(this);
    }

    public void destroy() {
    }

    public Map<String, Object> get(String keySpace, String columnFamily, String key)
            throws StorageClientException {
        return (Map<String, Object>) getOrCreateRow(keySpace, columnFamily, key);
    }

    private Map<String, Object> getOrCreateRow(String keySpace, String columnFamily, String key) {
        String keyName = getKey(keySpace, columnFamily, key);

        if (!store.containsKey(keyName)) {
            Map<String, Object> row = new ConcurrentHashMap<String, Object>();
            store.put(keyName, row);
            LOGGER.debug("Created {}  as {} ", new Object[] { keyName, row });
            return row;
        }
        Map<String, Object> row = store.get(keyName);
        LOGGER.debug("Got {} as {} ", new Object[] { keyName, row });
        return row;
    }

    private String getKey(String keySpace, String columnFamily, String key) {
        return keySpace + ":" + columnFamily + ":" + key;
    }

    public void insert(String keySpace, String columnFamily, String key, Map<String, Object> values)
            throws StorageClientException {
        Map<String, Object> row = get(keySpace, columnFamily, key);

        for (Entry<String, Object> e : values.entrySet()) {
            Object value = e.getValue();
            if (value instanceof byte[]) {
                byte[] bvalue = (byte[]) e.getValue();
                byte[] nvalue = new byte[bvalue.length];
                System.arraycopy(bvalue, 0, nvalue, 0, bvalue.length);
                value = nvalue;
            }
            if (value == null) {
                row.remove(e.getKey());
            } else {
                row.put(e.getKey(), value);
            }
        }
        LOGGER.debug("Updated {} {} ", key, row);
    }

    public void remove(String keySpace, String columnFamily, String key)
            throws StorageClientException {
        String keyName = getKey(keySpace, columnFamily, key);
        if (store.containsKey(keyName)) {
            store.remove(keyName);
        }
    }

    @Override
    public Map<String, Object> streamBodyIn(String keySpace, String contentColumnFamily,
            String contentId, String contentBlockId, Map<String, Object> content, InputStream in) throws StorageClientException,
            AccessDeniedException, IOException {
        return contentHelper.writeBody(keySpace, contentColumnFamily, contentId, contentBlockId,
                blockSize, maxChunksPerBlockSet, in);
    }

    @Override
    public InputStream streamBodyOut(String keySpace, String contentColumnFamily, String contentId,
            String contentBlockId, Map<String, Object> content) throws StorageClientException,
            AccessDeniedException {

        int nBlocks = StorageClientUtils.toInt(content.get(Content.NBLOCKS_FIELD));
        return contentHelper.readBody(keySpace, contentColumnFamily, contentBlockId, nBlocks);
    }

    @Override
    public DisposableIterator<Map<String, Object>> find(String keySpace, String authorizableColumnFamily,
            Map<String, Object> properties) {
        //TODO: Implement
        throw new UnsupportedOperationException();
    }


}
