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
package org.sakaiproject.nakamura.lite.storage.cassandra;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.sakaiproject.nakamura.lite.types.Types;
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
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.sakaiproject.nakamura.api.lite.RemoveProperty;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
import org.sakaiproject.nakamura.lite.content.BlockSetContentHelper;
import org.sakaiproject.nakamura.lite.storage.Disposable;
import org.sakaiproject.nakamura.lite.storage.DisposableIterator;
import org.sakaiproject.nakamura.lite.storage.Disposer;
import org.sakaiproject.nakamura.lite.storage.SparseRow;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class CassandraClient extends Client implements StorageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraClient.class);
    public static final String CONFIG_BLOCK_SIZE = "block-size";
    public static final String CONFIG_MAX_CHUNKS_PER_BLOCK = "chunks-per-block";

    private static final int DEFAULT_BLOCK_SIZE = 1024 * 1024;
    private static final int DEFAULT_MAX_CHUNKS_PER_BLOCK = 64;
    private static final String INDEX_COLUMN_FAMILY = "smcindex";


    private TSocket tSocket;
    private BlockContentHelper contentHelper;
    private int blockSize;
    private int maxChunksPerBlockSet;
    private CassandraClientPool pool;

    private Set<String> indexColumns;

    private boolean active;
    private List<Disposable> toDispose = Lists.newArrayList();
    public List<Map<String, Object>> tResultRows;


    public CassandraClient(CassandraClientPool pool, TProtocol tProtocol, TSocket tSocket,
            Map<String, Object> properties, Set<String> indexColums) {
        super(tProtocol);
        this.indexColumns = indexColums;
        this.tSocket = tSocket;
        this.pool = pool;
        contentHelper = new BlockSetContentHelper(this);
        blockSize = StorageClientUtils.getSetting(properties.get(CONFIG_BLOCK_SIZE),
                DEFAULT_BLOCK_SIZE);
        maxChunksPerBlockSet = StorageClientUtils.getSetting(
                properties.get(CONFIG_MAX_CHUNKS_PER_BLOCK), DEFAULT_MAX_CHUNKS_PER_BLOCK);
    }

    public void close() {
        pool.releaseClient(this);
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
        Map<String, Object> row = new HashMap<String, Object>();
        try {
            SlicePredicate predicate = new SlicePredicate();
            SliceRange sliceRange = new SliceRange();
            sliceRange.setStart(new byte[0]);
            sliceRange.setFinish(new byte[0]);
            predicate.setSlice_range(sliceRange);

            ColumnParent parent = new ColumnParent(columnFamily);
            List<ColumnOrSuperColumn> results = get_slice(keySpace, key, parent, predicate,ConsistencyLevel.ONE);

            for (ColumnOrSuperColumn result : results) {
                if (result.isSetSuper_column()) {
                    Map<String, Object> sc = new HashMap<String, Object>();

                    for (Column column : result.super_column.columns) {
                        Object columnValue = Types.toObject(column.value);
                        sc.put(new String(column.name, "UTF-8"), columnValue);
                    }
                    row.put(new String(result.super_column.name, "UTF-8"), sc);
                } else {
                    row.put(new String(result.column.name, "UTF-8"),
                            Types.toObject(result.column.value));
                }
            }

        } catch (InvalidRequestException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (UnavailableException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (TimedOutException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (TException e) {
            throw new StorageClientException(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.debug(e.getMessage());
        }
        return row;
    }

    public void insert(String keySpace, String columnFamily, String key, Map<String, Object> values, boolean probablyNew)
            throws StorageClientException {
        try {
            Map<String, Map<String, List<Mutation>>> mutation = new HashMap<String, Map<String, List<Mutation>>>();
            Map<String, List<Mutation>> columnMutations = new HashMap<String, List<Mutation>>();
            LOGGER.debug("Saving changes to {}:{}:{} ",
                    new Object[] { keySpace, columnFamily, key });
            List<Mutation> keyMutations = Lists.newArrayList();
            columnMutations.put(columnFamily, keyMutations);
            mutation.put(key, columnMutations);
            
            for (Entry<String, Object> value : values.entrySet()) {
                String name = value.getKey();
                byte[] bname=null;
                try {
                    bname = name.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e1) {
                    LOGGER.debug(e1.getMessage());
                    }                
                Object v = value.getValue();
                if (v instanceof RemoveProperty) {
                    Deletion deletion = new Deletion();
                    SlicePredicate deletionPredicate = new SlicePredicate();
                    deletionPredicate.addToColumn_names(bname);
                    deletion.setPredicate(deletionPredicate);
                    Mutation mu = new Mutation();
                    mu.setDeletion(deletion);
                    keyMutations.add(mu);
                }
                else {
                    try{
                         byte b[]=Types.toByteArray(v);
                         Column column = new Column(bname, b, System.currentTimeMillis());
                         ColumnOrSuperColumn csc = new ColumnOrSuperColumn();
                         csc.setColumn(column);
                         Mutation mu = new Mutation();
                         mu.setColumn_or_supercolumn(csc);
                         keyMutations.add(mu);
                         
                         if((!columnFamily.equals(INDEX_COLUMN_FAMILY))&&shouldIndex(keySpace, columnFamily, name)) {
                              addIndex(keySpace,columnFamily,key,bname,b);
                         }

                    }
                    catch(IOException e)
                    {
                         LOGGER.debug("IOException. Stack trace:",e);
                    }  
                }
            }
            LOGGER.debug("Mutation {} ", mutation);
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
      if(!columnFamily.equals(INDEX_COLUMN_FAMILY)){
        Map<String, Object> indexRow = null;
        Map<String, Object> row = get(keySpace, columnFamily, key);
        
        for (Entry<String, Object> value : row.entrySet()) {
          try {
            String columnname = value.getKey();
            String columnvalue = null;
            columnvalue = new String(Types.toByteArray(value.getValue()));
            columnvalue=StorageClientUtils.insecureHash(columnvalue);
            String indexKey=columnname+":"+INDEX_COLUMN_FAMILY+":"+columnvalue;
            indexRow=get(keySpace,INDEX_COLUMN_FAMILY,indexKey);         
            indexRow.remove(key);
            remove(keySpace,INDEX_COLUMN_FAMILY,indexKey);
            insert(keySpace,INDEX_COLUMN_FAMILY,indexKey,indexRow,true);
          }  catch (IOException e) {
            LOGGER.debug("IOException. ",e);
          }
        }
      }

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

    public Map<String, Object> streamBodyIn(String keySpace, String contentColumnFamily,
            String contentId, String contentBlockId, String streamId, Map<String, Object> content, InputStream in)
            throws StorageClientException, AccessDeniedException, IOException {
        return contentHelper.writeBody(keySpace, contentColumnFamily, contentId, contentBlockId, streamId,
                blockSize, maxChunksPerBlockSet, in);
    }

    public InputStream streamBodyOut(String keySpace, String contentColumnFamily, String contentId,
            String contentBlockId, String streamId, Map<String, Object> content) throws StorageClientException,
            AccessDeniedException {

        int nBlocks = StorageClientUtils.toInt(content.get(Content.NBLOCKS_FIELD));
        return contentHelper.readBody(keySpace, contentColumnFamily, contentBlockId, streamId, nBlocks);
    }

  public DisposableIterator<Map<String, Object>> find(String keySpace,
      String authorizableColumnFamily, Map<String, Object> properties)
      throws StorageClientException {
    final String fKeyspace = keySpace;
    final String fAuthorizableColumnFamily = authorizableColumnFamily;
    List<Set<String>> andTerms = new ArrayList<Set<String>>();

    for (Entry<String, Object> e : properties.entrySet()) {
      String k = e.getKey();
      Object v = e.getValue();

      if (shouldIndex(keySpace, authorizableColumnFamily, k) || (v instanceof Map)) {
        if (v != null) {
          if (v instanceof Map) {
            List<Set<String>> orTerms = new ArrayList<Set<String>>();
            Set<String> orResultSet = new HashSet<String>();

            @SuppressWarnings("unchecked")
            Set<Entry<String, Object>> subterms = ((Map<String, Object>) v).entrySet();

            for (Iterator<Entry<String, Object>> subtermsIter = subterms.iterator(); subtermsIter
                .hasNext();) {
              Entry<String, Object> subterm = subtermsIter.next();
              String subk = subterm.getKey();
              Object subv = subterm.getValue();
              if (shouldIndex(keySpace, authorizableColumnFamily, subk)) {
                try {
                  Set<String> or = new HashSet<String>();
                  String indexKey = new String(subk.getBytes("UTF-8"))
                      + ":"
                      + authorizableColumnFamily
                      + ":"
                      + StorageClientUtils.insecureHash(new String(Types
                          .toByteArray(subv)));
                  Map<String, Object> tempRow = get(keySpace, INDEX_COLUMN_FAMILY, indexKey);
                  for (Entry<String, Object> tempRows : tempRow.entrySet()) {
                    or.add(tempRows.getKey());
                  }
                  orTerms.add(or);
                } catch (IOException e1) {
                  LOGGER.warn("IOException {}", e1.getMessage());
                }
              }
            }

            if (!orTerms.isEmpty())
              orResultSet = orTerms.get(0);

            for (int i = 0; i < orTerms.size(); i++) {
              orResultSet = Sets.union(orResultSet, orTerms.get(i));

            }
            andTerms.add(orResultSet);
          } else {
            try {
              Set<String> and = new HashSet<String>();
              String indexKey = new String(k.getBytes("UTF-8")) + ":" + authorizableColumnFamily
                  + ":"
                  + StorageClientUtils.insecureHash(new String(Types.toByteArray(v)));
              Map<String, Object> tempRow = get(keySpace, INDEX_COLUMN_FAMILY, indexKey);
              for (Entry<String, Object> tempRows : tempRow.entrySet()) {
                and.add(tempRows.getKey());
              }
              andTerms.add(and);
            } catch (IOException e1) {
              LOGGER.warn("IOException {}", e1.getMessage());
            }
          }
        }
      }
    }

    Set<String> andResultSet = new HashSet<String>();

    if (!andTerms.isEmpty())
      andResultSet = andTerms.get(0);

    for (int i = 0; i < andTerms.size(); i++) {
      andResultSet = Sets.intersection(andResultSet, andTerms.get(i));
    }

    List<Map<String, Object>> resultRows = new ArrayList<Map<String, Object>>();

    Iterator<String> iterator = andResultSet.iterator();

    while (iterator.hasNext()) {
      Map<String, Object> row = get(keySpace, authorizableColumnFamily, iterator.next());
      resultRows.add(row);
    }

    tResultRows = resultRows;
    final Iterator<String> fIterator = andResultSet.iterator();

    if (tResultRows.isEmpty()) {
      return new DisposableIterator<Map<String, Object>>() {

        private Disposer disposer;

        public boolean hasNext() {
          return false;
        }

        public Map<String, Object> next() {
          return null;
        }

        public void remove() {
        }

        public void close() {
          if (disposer != null) {
            disposer.unregisterDisposable(this);
          }
        }

        public void setDisposer(Disposer disposer) {
          this.disposer = disposer;
        }
      };
    }
    return registerDisposable(new PreemptiveIterator<Map<String, Object>>() {

      private Map<String, Object> nextValue = Maps.newHashMap();
      private boolean open = true;

      protected Map<String, Object> internalNext() {
        return nextValue;
      }

      protected boolean internalHasNext() {
        if (fIterator.hasNext()) {
          try {
            String id = fIterator.next();
            nextValue = get(fKeyspace, fAuthorizableColumnFamily, id);
            LOGGER.debug("Got Row ID {} {} ", id, nextValue);
            return true;
          } catch (StorageClientException e) {

          }
        }
        close();
        nextValue = null;
        LOGGER.debug("End of Set ");
        return false;
      }

      @Override
      public void close() {
        if (open) {
          open = false;
        }

      }
    });
  }

    public DisposableIterator<Map<String, Object>> listChildren(String keySpace,
            String columnFamily, String key) throws StorageClientException {
        throw new UnsupportedOperationException();
    }

    public boolean hasBody(Map<String, Object> content, String streamId) {
        return contentHelper.hasBody(content, streamId);
    }
    
    private void addIndex(String keySpace, String columnFamily, String key, byte[] bname, byte[] b)
            throws StorageClientException {
        String indexKey = new String(bname) + ":" + columnFamily + ":" + StorageClientUtils.insecureHash(b);
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(key, (Object) "Whatever value of index");
        insert(keySpace, INDEX_COLUMN_FAMILY, indexKey, values, true);
}


    private boolean shouldIndex(String keySpace, String columnFamily, String columnName)
            throws StorageClientException {
        if (indexColumns.contains(columnFamily + ":" + columnName)) {
            LOGGER.debug("Should Index {}:{}", columnFamily, columnName);
            return true;
        } else {
            LOGGER.debug("Should Not Index {}:{}", columnFamily, columnName);
            return false;
        }
    }

    private <T extends Disposable> T registerDisposable(T disposable) {
      toDispose.add(disposable);
      return disposable;
  }
    public void shutdownConnection() {
      if (active) {
          disposeDisposables();
          active = false;
      }
  }

    private void disposeDisposables() {
      for (Disposable d : toDispose) {
          d.close();
      }
  }

    public DisposableIterator<SparseRow> listAll(String keySpace, String columnFamily) {
        // TODO Auto-generated method stub
        return null;
    }

    public long allCount(String keySpace, String columnFamily) {
        // TODO Auto-generated method stub
        return 0;
    }  
    
}