package org.sakaiproject.nakamura.lite.storage.mem;

import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class MemoryStorageClient implements StorageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryStorageClient.class);
    Map<String, Map<String, Object>> store = Maps.newHashMap();

    public Map<String, Object> get(String keySpace, String columnFamily, String key)
            throws StorageClientException {
        return (Map<String, Object>) getOrCreateRow(keySpace, columnFamily, key);
    }

    private Map<String, Object> getOrCreateRow(String keySpace, String columnFamily, String key) {
        String keyName = getKey(keySpace, columnFamily, key);

        if (!store.containsKey(keyName)) {
            Map<String, Object> row = Maps.newHashMap();
            store.put(keyName, row);
            LOGGER.info("Created {}  as {} ", new Object[] { keyName,  row });
            return row;
        }
        Map<String, Object> row = store.get(keyName);
        LOGGER.info("Got {} as {} ", new Object[] { keyName,  row });
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
            if ( value instanceof byte[]) {
                byte[] bvalue = (byte[]) e.getValue();
                byte[] nvalue = new byte[bvalue.length];
                System.arraycopy(bvalue, 0, nvalue, 0, bvalue.length);
                value = nvalue;
            }
            row.put(e.getKey(), value);
        }
        LOGGER.info("Updated {} {} ", key, row);
    }

    public void remove(String keySpace, String columnFamily, String key)
            throws StorageClientException {
        String keyName = getKey(keySpace, columnFamily, key);
        if (store.containsKey(keyName)) {
            store.remove(keyName);
        }
    }

}
