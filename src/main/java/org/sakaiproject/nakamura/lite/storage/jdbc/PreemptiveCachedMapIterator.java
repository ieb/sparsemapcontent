package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
import org.sakaiproject.nakamura.lite.storage.spi.DirectCacheAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;

public class PreemptiveCachedMapIterator extends PreemptiveIterator<Map<String, Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreemptiveCachedMapIterator.class);
    private Map<String, Object> nextValue = Maps.newHashMap();
    private List<Map<String, Object>> preloadedResults = null;
    private boolean open = true;
    private boolean started = false;
    private int preloadedResultsIndex;
    private ResultSet resultSet;
    private boolean rawResults;
    private String keySpace;
    private String columnFamily;
    private DirectCacheAccess cachingManager;
    private JDBCStorageClient client;
    private PreparedStatement preparedStatement;
    private ResultSetMetaData resultSetMetadata;

    /**
     * Construct an iterator from a query response.
     * @param client
     * @param resultSet
     * @param preparedStatement
     * @param rawResults
     * @param cachingManager
     * @throws SQLException
     */
    public PreemptiveCachedMapIterator(JDBCStorageClient client, String keySpace, String columnFamily, ResultSet resultSet,
            PreparedStatement preparedStatement, boolean rawResults,
            DirectCacheAccess cachingManager) throws SQLException {
        this.keySpace = keySpace;
        this.columnFamily = columnFamily;
        this.resultSet = resultSet;
        this.rawResults = rawResults;
        this.client = client;
        this.cachingManager = cachingManager;

        this.resultSetMetadata = resultSet.getMetaData();
        this.preparedStatement = preparedStatement;
    }

    /**
     * Construct a iterator from a cached response.
     * @param client
     * @param cachedResults
     * @param cachingManager
     */
    @SuppressWarnings("unchecked")
    public PreemptiveCachedMapIterator(JDBCStorageClient client, String keySpace, String columnFamily, Map<String, Object> cachedResults, boolean rawResults,
            DirectCacheAccess cachingManager) {
        this.keySpace = keySpace;
        this.columnFamily = columnFamily;
        this.rawResults = rawResults;
        this.client = client;
        this.cachingManager = cachingManager;
        
        this.preloadedResults = (List<Map<String, Object>>) cachedResults.get("rows");
        this.preloadedResultsIndex = 0;
    }

    @Override
    protected Map<String, Object> internalNext() {
        started = true;
        return nextValue;
    }

    @Override
    public Map<String, Object> getResultsMap() {
        try {
            // load the first X results into a list, for caching. If we still
            // have more, then stop and return a null.
            if (preloadedResults == null && resultSet != null) {
                if (started) {
                    throw new IllegalStateException("Cant get results map once iteration has started");
                }
                com.google.common.collect.ImmutableList.Builder<Map<String, Object>> resultsBuilder = ImmutableList
                        .builder();
                int size = 0;
                while (size < 500 && resultSet.next()) {
                    Builder<String, Object> b = ImmutableMap.builder();
                    for (int i = 1; i <= resultSetMetadata.getColumnCount(); i++) {
                        b.put(String.valueOf(i), resultSet.getObject(i));
                    }
                    resultsBuilder.add(b.build());
                    size++;
                }
                preloadedResults = resultsBuilder.build();
                preloadedResultsIndex = 0;
                if (size >= 500) {
                    // don't cache if there are more than 500 results.
                    return null;
                }
            }
            if ( preloadedResults == null && resultSet == null ) {
                throw new IllegalStateException("Cant get results map, no source results set.");                
            }
            return ImmutableMap.of("rows", (Object) preloadedResults);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            close();
            nextValue = null;
            return null;
        }
    }

    @Override
    protected boolean internalHasNext() {
        try {
            started = true;
            if (open && preloadedResults != null && preloadedResultsIndex < preloadedResults.size()) {
                if (rawResults) {
                    nextValue = preloadedResults.get(preloadedResultsIndex);
                } else {
                    String id = (String) preloadedResults.get(preloadedResultsIndex).get("1");
                    nextValue = client.internalGet(keySpace, columnFamily, id, cachingManager);
                }
                preloadedResultsIndex++;
                return true;
            }
            if (open && resultSet != null && resultSet.next()) {
                if (rawResults) {
                    Builder<String, Object> b = ImmutableMap.builder();
                    for (int i = 1; i <= resultSetMetadata.getColumnCount(); i++) {
                        b.put(String.valueOf(i), resultSet.getObject(i));
                    }
                    nextValue = b.build();
                } else {
                    String id = resultSet.getString(1);
                    nextValue = client.internalGet(keySpace, columnFamily, id, cachingManager);
                    LOGGER.debug("Got Row ID {} {} ", id, nextValue);
                }
                return true;
            }
            close();
            nextValue = null;
            LOGGER.debug("End of Set ");
            return false;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            close();
            nextValue = null;
            return false;
        } catch (StorageClientException e) {
            LOGGER.error(e.getMessage(), e);
            close();
            nextValue = null;
            return false;
        }
    }

    @Override
    public void close() {
        if (open) {
            open = false;
            try {
                if (resultSet != null) {
                    resultSet.close();
                    client.dec("iterator r");
                }
            } catch (SQLException e) {
                LOGGER.warn(e.getMessage(), e);
            }
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                    client.dec("iterator");
                }
            } catch (SQLException e) {
                LOGGER.warn(e.getMessage(), e);
            }
            super.close();
        }

    }
}
