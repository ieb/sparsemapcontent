package org.sakaiproject.nakamura.lite.storage.cassandra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cassandra.thrift.Cassandra.Client;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.Deletion;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.Mutation;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.thrift.SuperColumn;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientConnection extends Client implements StorageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnection.class);
    private TSocket tSocket;

    public ClientConnection(TProtocol tProtocol, TSocket tSocket) {
        super(tProtocol);
        this.tSocket = tSocket;
    }

    public void destroy() {
        try {
            if (tSocket.isOpen()) {
                tSocket.flush();
                tSocket.close();
            }
        } catch (TTransportException e) {
            LOGGER.error("Failed to close the connection to the cassandra store.", e);
        }
    }

    public void passivate() {
    }

    public void activate() {
    }

    public void validate() throws TException {
        describe_version();
    }

    public Map<String, Object> get(String keySpace, String columnFamily, String key)
            throws StorageClientException {
        try {
            Map<String, Object> row = new HashMap<String, Object>();

            SlicePredicate predicate = new SlicePredicate();
            SliceRange sliceRange = new SliceRange();
            sliceRange.setStart(new byte[0]);
            sliceRange.setFinish(new byte[0]);
            predicate.setSlice_range(sliceRange);

            ColumnParent parent = new ColumnParent(columnFamily);
            List<ColumnOrSuperColumn> results = get_slice(keySpace, key, parent, predicate,
                    ConsistencyLevel.ONE);
            for (ColumnOrSuperColumn result : results) {
                if (result.isSetSuper_column()) {
                    Map<String, byte[]> sc = new HashMap<String, byte[]>();
                    for (Column column : result.super_column.columns) {
                        sc.put(StorageClientUtils.toString(column.name), column.value);
                    }
                    row.put(StorageClientUtils.toString(result.super_column.name), sc);
                } else {
                    row.put(StorageClientUtils.toString(result.column.name), result.column.value);
                }
            }
            return row;
        } catch (InvalidRequestException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (UnavailableException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (TimedOutException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (TException e) {
            throw new StorageClientException(e.getMessage(), e);
        }
    }

    public void insert(String keySpace, String columnFamily, String key, Map<String, Object> values)
            throws StorageClientException {
        try {
            Map<String, Map<String, List<Mutation>>> mutation = new HashMap<String, Map<String, List<Mutation>>>();
            Map<String, List<Mutation>> columnMutations = new HashMap<String, List<Mutation>>();
            mutation.put(columnFamily, columnMutations);
            for (Entry<String, Object> value : values.entrySet()) {
                String name = value.getKey();
                byte[] bname = StorageClientUtils.toBytes(name);
                List<Mutation> keyMutations = getMutationList(columnMutations, name);
                Object v = value.getValue();
                if (v == null) {
                    Deletion deletion = new Deletion();
                    deletion.setSuper_column(bname);
                    Mutation mu = new Mutation();
                    mu.setDeletion(deletion);
                    keyMutations.add(mu);
                } else if (v instanceof byte[]) {
                    byte[] bv = (byte[]) v;
                    Column column = new Column(bname, bv, System.currentTimeMillis());
                    ColumnOrSuperColumn csc = new ColumnOrSuperColumn();
                    csc.setColumn(column);
                    Mutation mu = new Mutation();
                    mu.setColumn_or_supercolumn(csc);
                    keyMutations.add(mu);
                } else if (v instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, byte[]> sc = (Map<String, byte[]>) v;
                    List<Column> columns = new ArrayList<Column>();
                    for (Entry<String, byte[]> sce : sc.entrySet()) {
                        String cname = sce.getKey();
                        byte[] bcname = StorageClientUtils.toBytes(cname);
                        Column column = new Column(bcname, StorageClientUtils.toBytes(sce.getValue()),
                                System.currentTimeMillis());
                        columns.add(column);
                    }

                    SuperColumn superColumn = new SuperColumn(bname, columns);
                    ColumnOrSuperColumn csc = new ColumnOrSuperColumn();
                    csc.setSuper_column(superColumn);
                    Mutation mu = new Mutation();
                    mu.setColumn_or_supercolumn(csc);
                    keyMutations.add(mu);
                }
            }
            batch_mutate(keySpace, mutation, ConsistencyLevel.ONE);
        } catch (InvalidRequestException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (UnavailableException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (TimedOutException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (TException e) {
            throw new StorageClientException(e.getMessage(), e);
        }
    }

    public void remove(String keySpace, String columnFamily, String key)
            throws StorageClientException {
        ColumnPath cp = new ColumnPath(columnFamily);
        try {
            remove(keySpace, key, cp, System.currentTimeMillis(), ConsistencyLevel.ONE);
        } catch (InvalidRequestException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (UnavailableException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (TimedOutException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (TException e) {
            throw new StorageClientException(e.getMessage(), e);
        }
    }

    private List<Mutation> getMutationList(Map<String, List<Mutation>> columnMutations, String key) {
        List<Mutation> m = columnMutations.get(key);
        if (m == null) {
            m = new ArrayList<Mutation>();
            columnMutations.put(key, m);
        }
        return m;
    }

}
