package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractIndexer implements Indexer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIndexer.class);
    private Set<String> indexColumns;
    
    public AbstractIndexer(Set<String> indexColumns2) {
        this.indexColumns = indexColumns2;
    }
    
    boolean shouldFind(String keySpace, String columnFamily, String k) {
        String key = columnFamily+":"+k;
        if ( JDBCStorageClient.AUTO_INDEX_COLUMNS.contains(key) || indexColumns.contains(key)) {
            return true;
        } else {
            LOGGER.debug("Ignoring Find operation on {}:{}", columnFamily, k);     
        }
        return false;
    }
    boolean shouldIndex(String keySpace, String columnFamily, String k) {
        String key = columnFamily+":"+k;
        if ( JDBCStorageClient.AUTO_INDEX_COLUMNS.contains(key) || indexColumns.contains(key)) {
            LOGGER.debug("Will Index {}:{}", columnFamily, k);
            return true;
        } else {
            LOGGER.debug("Will Not Index {}:{}", columnFamily, k);
            return false;
        }
    }

}
