package org.sakaiproject.nakamura.lite.storage.mem;

import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;

import com.google.common.collect.Maps;

public class MemoryStorageClient implements StorageClient {

	Map<String, Map<String, Object>> store = Maps.newHashMap();

	
	public Map<String, Object> get(String keySpace, String columnFamily,
			String key) throws StorageClientException {
		
		return (Map<String, Object>) getOrCreateRow(keySpace, columnFamily, key);
	}
	

	private Map<String, Object> getOrCreateRow(String keySpace,
			String columnFamily, String key) {
		String keyName = getKey(keySpace, columnFamily, key);
		if ( !store.containsKey(keyName)) {
			Map<String, Object> row = Maps.newHashMap();
			store.put(keyName,row);
			return row;
		}
		return store.get(key);
	}


	private String getKey(String keySpace, String columnFamily, String key) {
		return keySpace+":"+columnFamily+":"+key;
	}


	public void insert(String keySpace, String columnFamily, String key,
			Map<String, Object> values) throws StorageClientException {
		Map<String, Object> row = get(keySpace, columnFamily, key);
		
		for ( Entry<String, Object> e : values.entrySet()) {
			row.put(e.getKey(), e.getValue());
		}
	}

	public void remove(String keySpace, String columnFamily, String key)
			throws StorageClientException {
		String keyName = getKey(keySpace, columnFamily, key);
		if ( store.containsKey(keyName)) {
			store.remove(keyName);
		}
	}

}
