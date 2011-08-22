package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.lite.SessionImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.content.BlockSetContentHelper;
import org.sakaiproject.nakamura.lite.storage.DisposableIterator;
import org.sakaiproject.nakamura.lite.storage.SparseRow;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.jdbc.Indexer;
import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

@Component(immediate=false, enabled=false)
public class MigrateToWideColumnIndex {

    
    public interface IdExtractor {

        String getKey(Map<String, Object> properties);

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrateToWideColumnIndex.class);
    
    @Reference
    private Repository repository;
    
    @Reference
    private Configuration configuration;

    @Activate
    public void activate(Map<String, Object> properties) throws StorageClientException, AccessDeniedException  {
        SessionImpl session = (SessionImpl) repository.loginAdministrative();
        StorageClient client = session.getClient();
        if ( client instanceof JDBCStorageClient ) {
            JDBCStorageClient jdbcClient = (JDBCStorageClient) client;
            String keySpace = configuration.getKeySpace();

            Indexer indexer = jdbcClient.getIndexer();
            
            reindex(jdbcClient, keySpace, configuration.getAuthorizableColumnFamily(), indexer, new IdExtractor() {
                
                public String getKey(Map<String, Object> properties) {
                    if ( properties.containsKey(Authorizable.ID_FIELD)) {
                        return (String) properties.get(Authorizable.ID_FIELD);
                    }
                    return null;
                }
            });
            reindex(jdbcClient, keySpace, configuration.getContentColumnFamily(), indexer, new IdExtractor() {
                
                public String getKey(Map<String, Object> properties) {
                    if ( properties.containsKey(BlockSetContentHelper.CONTENT_BLOCK_ID)) {
                        // blocks of a bit stream
                        return (String) properties.get(BlockSetContentHelper.CONTENT_BLOCK_ID);
                    } else if ( properties.containsKey(Content.getUuidField())) {
                        // a content item and content block item
                        return (String) properties.get(Content.getUuidField());
                    } else if ( properties.containsKey(Content.STRUCTURE_UUID_FIELD)) {
                        // a structure item
                        return (String) properties.get(Content.PATH_FIELD);                        
                    }
                    return null;
                }
            });
            
            reindex(jdbcClient, keySpace, configuration.getAclColumnFamily(), indexer, new IdExtractor() {
                public String getKey(Map<String, Object> properties) {
                    if ( properties.containsKey(AccessControlManagerImpl._KEY)) {
                        return (String) properties.get(AccessControlManagerImpl._KEY);
                    }
                    return null;
                }
            });
        } else {
            LOGGER.warn("This class will only re-index content for the JDBCStorageClients");
        }
    }

    private void reindex(StorageClient jdbcClient, String keySpace, String columnFamily, Indexer indexer, IdExtractor idExtractor) throws StorageClientException {
        DisposableIterator<SparseRow> allObjects = jdbcClient.listAll(keySpace, columnFamily);
        Map<String, PreparedStatement> statementCache = Maps.newHashMap();
        while( allObjects.hasNext()) {
            SparseRow r = allObjects.next();
    
            try {
                Map<String, Object> properties = r.getProperties();
                String key = idExtractor.getKey(properties);
                if ( key != null ) {
                indexer.index(statementCache, keySpace, columnFamily, key, r.getRowId(), properties);
                } else {
                    LOGGER.info("Skipped Reindexing, no key in  {}",properties);
                }
            } catch (SQLException e) {
                LOGGER.warn(e.getMessage(),e);
            } catch (StorageClientException e) {
                LOGGER.warn(e.getMessage(),e);
            }
        }
    }
}
