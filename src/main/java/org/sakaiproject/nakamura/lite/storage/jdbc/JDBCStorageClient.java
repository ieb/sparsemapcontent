package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.content.StreamedContentHelper;
import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;
import org.sakaiproject.nakamura.lite.storage.RowHasher;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class JDBCStorageClient implements StorageClient, RowHasher {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCStorageClient.class);
    private static final String SQL_VALIDATE = "validate";
    private static final String SQL_CHECKSCHEMA = "check-schema";
    private static final String SQL_COMMENT = "#";
    private static final String SQL_EOL = ";";
    private static final String SQL_DELETE_STRING_ROW = "delete-string-row";
    private static final String SQL_SELECT_STRING_ROW = "select-string-row";
    private static final String SQL_INSERT_STRING_COLUMN = "insert-string-column";
    private static final String SQL_UPDATE_STRING_COLUMN = "update-string-column";
    private static final String PROP_HASH_ALG = "rowid-hash";
    private static final String SQL_REMOVE_STRING_COLUMN = "remove-string-column";
    private static final Set<String> preparedStatementKeys = ImmutableSet.of(SQL_DELETE_STRING_ROW,
            SQL_SELECT_STRING_ROW, SQL_INSERT_STRING_COLUMN, SQL_UPDATE_STRING_COLUMN,
            SQL_REMOVE_STRING_COLUMN);
    private Connection connection;
    private Map<String, Object> sqlConfig;
    private boolean active;
    private MessageDigest hasher;
    private boolean alive;
    private StreamedContentHelper streamedContentHelper;
    private Map<String, PreparedStatement> preparedStatements = Maps.newHashMap();

    public JDBCStorageClient(Connection connection, Map<String, Object> properties, Map<String, Object> sqlConfig)
            throws SQLException, NoSuchAlgorithmException, StorageClientException {
        this.connection = connection;
        streamedContentHelper = new FileStreamContentHelper(this, properties);
        
        this.sqlConfig = sqlConfig;
        String rowidHash = getSql(PROP_HASH_ALG);
        if (rowidHash == null) {
            rowidHash = "MD5";
        }
        hasher = MessageDigest.getInstance(rowidHash);

    }

    public Map<String, Object> get(String keySpace, String columnFamily, String key)
            throws StorageClientException {
        ResultSet strings = null;
        ResultSet blobs = null;
        Map<String, Object> result = Maps.newHashMap();
        String rid = rowHash(keySpace, columnFamily, key);
        try {
            startUpConnection();
            PreparedStatement selectStringRow = getStatement(keySpace, columnFamily,
                    SQL_SELECT_STRING_ROW, rid);
            selectStringRow.clearWarnings();
            selectStringRow.clearParameters();
            selectStringRow.setString(1, rid);
            strings = selectStringRow.executeQuery();
            while (strings.next()) {
                result.put(strings.getString(1), strings.getString(2));
            }
        } catch (SQLException e) {
            LOGGER.warn("Failed to perform get operation on {}:{}:{} ", new Object[] { keySpace,
                    columnFamily, key }, e);
            throw new StorageClientException(e.getMessage(), e);
        } finally {
            try {
                if (strings != null) {
                    strings.close();
                }
            } catch (Throwable e) {
                LOGGER.debug("Failed to close result set, ok to ignore this message ", e);
            }
            try {
                if (blobs != null) {
                    blobs.close();
                }
            } catch (Throwable e) {
                LOGGER.debug("Failed to close result set, ok to ignore this message ", e);
            }
        }
        return result;
    }

    public String rowHash(String keySpace, String columnFamily, String key) {
        hasher.reset();
        String keystring = keySpace + ":" + columnFamily + ":" + key;
        byte[] ridkey;
        try {
            ridkey = keystring.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            ridkey = keystring.getBytes();
        }
        return StorageClientUtils.encode(hasher.digest(ridkey),
                StorageClientUtils.URL_SAFE_ENCODING);
    }

    public void insert(String keySpace, String columnFamily, String key, Map<String, Object> values)
            throws StorageClientException {
        try {
            startUpConnection();
            String rid = rowHash(keySpace, columnFamily, key);
            for (Entry<String, Object> e : values.entrySet()) {
                String k = e.getKey();
                Object o = e.getValue();
                if (o instanceof byte[]) {
                    throw new RuntimeException("Invalid content in " + k
                            + ", storing byte[] rather than streaming it");
                }
            }
            PreparedStatement updateStringColumn = getStatement(keySpace, columnFamily,
                    SQL_UPDATE_STRING_COLUMN, rid);
            PreparedStatement insertStringColumn = getStatement(keySpace, columnFamily,
                    SQL_INSERT_STRING_COLUMN, rid);
            PreparedStatement removeStringColumn = getStatement(keySpace, columnFamily,
                    SQL_REMOVE_STRING_COLUMN, rid);
            for (Entry<String, Object> e : values.entrySet()) {
                String k = e.getKey();
                Object o = e.getValue();
                if (o instanceof String) {
                    updateStringColumn.clearWarnings();
                    updateStringColumn.clearParameters();
                    updateStringColumn.setString(1, (String) o);
                    updateStringColumn.setString(2, rid);
                    updateStringColumn.setString(3, k);
                    if (updateStringColumn.executeUpdate() == 0) {
                        insertStringColumn.clearWarnings();
                        insertStringColumn.clearParameters();
                        insertStringColumn.setString(1, (String) o);
                        insertStringColumn.setString(2, rid);
                        insertStringColumn.setString(3, k);
                        if (insertStringColumn.executeUpdate() == 0) {
                            throw new StorageClientException("Failed to save "
                                    + getRowId(keySpace, columnFamily, key) + "  column:[" + k
                                    + "] ");
                        } else {
                            LOGGER.debug("Inserted {} {} [{}]",
                                    new Object[] { getRowId(keySpace, columnFamily, key), k, o });
                        }
                    } else {
                        LOGGER.debug("Updated {} {} [{}]",
                                new Object[] { getRowId(keySpace, columnFamily, key), k, o });
                    }
                } else if (o == null) {
                    removeStringColumn.clearWarnings();
                    removeStringColumn.clearParameters();
                    removeStringColumn.setString(1, rid);
                    removeStringColumn.setString(2, k);
                    if (removeStringColumn.executeUpdate() == 0) {
                        Map<String, Object> m = get(keySpace, columnFamily, key);
                        LOGGER.debug("Column Not present did not remove {} {} Current Column:{} ",
                                new Object[] { getRowId(keySpace, columnFamily, key), k, m });
                    } else {
                        LOGGER.debug("Removed {} {} ", getRowId(keySpace, columnFamily, key), k);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.warn("Failed to perform insert/update operation on {}:{}:{} ", new Object[] {
                    keySpace, columnFamily, key }, e);
            throw new StorageClientException(e.getMessage(), e);
        }
    }

    private String getRowId(String keySpace, String columnFamily, String key) {
        return keySpace + ":" + columnFamily + ":" + key;
    }

    public void remove(String keySpace, String columnFamily, String key)
            throws StorageClientException {
        String rid = rowHash(keySpace, columnFamily, key);
        try {
            startUpConnection();
            PreparedStatement deleteStringRow = getStatement(keySpace, columnFamily,
                    SQL_DELETE_STRING_ROW, rid);
            deleteStringRow.clearWarnings();
            deleteStringRow.clearParameters();
            deleteStringRow.setString(1, rid);
            deleteStringRow.executeUpdate();

            deleteStringRow.clearWarnings();
            deleteStringRow.clearParameters();
            deleteStringRow.setString(1, rid);
            deleteStringRow.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warn("Failed to perform delete operation on {}:{}:{} ", new Object[] { keySpace,
                    columnFamily, key }, e);
            throw new StorageClientException(e.getMessage(), e);
        }
    }

    public void close() {
        try {
            shutdownConnection();
            connection.close();
        } catch (Throwable t) {
            LOGGER.debug("Failed to close connection ", t);
        }
    }

    /**
     * Get a prepared statement, potentially optimized and sharded.
     * @param keySpace
     * @param columnFamily
     * @param sqlSelectStringRow
     * @param rid
     * @return
     */
    private PreparedStatement getStatement(String keySpace, String columnFamily,
            String sqlSelectStringRow, String rid) {
        String shard = rid.substring(0,1);
        String[] keys = new String[]{
                sqlSelectStringRow+"."+keySpace+"."+columnFamily+"._"+shard,
                sqlSelectStringRow+"."+columnFamily+"._"+shard,
                sqlSelectStringRow+"."+keySpace+"._"+shard,
                sqlSelectStringRow+"._"+shard,
                sqlSelectStringRow+"."+keySpace+"."+columnFamily,
                sqlSelectStringRow+"."+columnFamily,
                sqlSelectStringRow+"."+keySpace,
                sqlSelectStringRow};
        for ( String k : keys ) {
            if ( preparedStatements.containsKey(k)) {
                return preparedStatements.get(k);
            }
        }
        return null;
    }

    public void startUpConnection() throws SQLException {
        if (!active && alive) {
            LOGGER.debug("Activating {}", this);
            openPreparedStatements();

            active = true;
        } else if ( !alive ){
            LOGGER.info("Delaying Activation, connection not alive ");
        }
    }

    private void openPreparedStatements() throws SQLException {
        for (Entry<String, Object> sql : sqlConfig.entrySet()) {
            String[] sqlSpec = StringUtils.split(sql.getKey(), '.');
            if ( preparedStatementKeys.contains(sqlSpec[0])) {
                preparedStatements.put(sql.getKey(), connection.prepareStatement((String) sql.getValue()));
            }
        }
    }

    public void shutdownConnection() {
        if (active) {
            LOGGER.info("Passivating {}", this);
            closePreparedStatements();
            active = false;
        }
    }

    private void closePreparedStatements() {
        for ( PreparedStatement p : preparedStatements.values()) {
            try {
                p.close();
            } catch (Throwable t) {

            } 
        }
        preparedStatements.clear();
    }

    public boolean validate() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(getSql(SQL_VALIDATE));
            return true;
        } catch (SQLException e) {
            LOGGER.warn("Failed to validate connection ", e);
            return false;
        } finally {
            try {
                statement.close();
            } catch (Throwable e) {
                LOGGER.debug("Failed to close statement in validate ", e);
            }
        }
    }

    private String getSql(String statementName) {
        return (String) sqlConfig.get(statementName);
    }

    public void checkSchema(String[] clientConfigLocations) throws ConnectionPoolException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            try {
                if (statement.execute(getSql(SQL_CHECKSCHEMA))) {
                    alive = true;
                    return;
                }
            } catch (SQLException e) {
                LOGGER.debug("Schema does not exist ", e);
            }

            for (String clientSQLLocation : clientConfigLocations) {
                String clientDDL = clientSQLLocation + ".ddl";
                InputStream in = this.getClass().getClassLoader().getResourceAsStream(clientDDL);
                if (in != null) {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));
                        int lineNo = 1;
                        String line = br.readLine();
                        StringBuilder sqlStatement = new StringBuilder();
                        while (line != null) {
                            line = StringUtils.stripEnd(line, null);
                            if (!line.isEmpty()) {
                                if (line.startsWith(SQL_COMMENT)) {
                                    LOGGER.info("Comment {} ", line);
                                } else if (line.endsWith(SQL_EOL)) {
                                    sqlStatement.append(line.substring(0, line.length() - 1));
                                    String ddl = sqlStatement.toString();
                                    try {
                                        statement.executeUpdate(ddl);
                                        LOGGER.info("SQL OK    {}:{} {} ", new Object[] {
                                                clientDDL, lineNo, ddl });
                                    } catch (SQLException e) {
                                        LOGGER.warn("SQL ERROR {}:{} {} {} ", new Object[] {
                                                clientDDL, lineNo, ddl, e.getMessage() });
                                    }
                                    sqlStatement = new StringBuilder();
                                } else {
                                    sqlStatement.append(line);
                                }
                            }
                            line = br.readLine();
                            lineNo++;
                        }
                        br.close();
                        alive = true;
                        break;
                    } catch (Throwable e) {
                        LOGGER.error("Failed to load Schema from {}", clientDDL, e);
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            LOGGER.error("Failed to close stream from {}", clientDDL, e);
                        }
                        
                    }
                }

            }

        } catch (SQLException e) {
            LOGGER.info("Failed to create schema ",e);
            throw new ConnectionPoolException("Failed to create schema ", e);
        } finally {
            LOGGER.info("Check Schema finished ");
            try {
                statement.close();
            } catch (Throwable e) {
                LOGGER.debug("Failed to close statement in validate ", e);
            }
        }
    }

    public void activate() {
    }

    public void passivate() {
    }

    @Override
    public Map<String, Object> streamBodyIn(String keySpace, String columnFamily, String contentId,
            String contentBlockId, Map<String, Object> content, InputStream in)
            throws StorageClientException, AccessDeniedException, IOException {
        return streamedContentHelper.writeBody(keySpace, columnFamily, contentId, contentBlockId,
                content, in);
    }

    @Override
    public InputStream streamBodyOut(String keySpace, String columnFamily, String contentId,
            String contentBlockId, Map<String, Object> content) throws StorageClientException,
            AccessDeniedException, IOException {
        return streamedContentHelper.readBody(keySpace, columnFamily, contentBlockId, content);
    }

    public void setAlive() {
        alive = true;
    }

    protected Connection getConnection() {
        return connection;
    }
}
