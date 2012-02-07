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
package org.sakaiproject.nakamura.lite.storage.hbase;

import java.util.Map;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTablePool;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.StorageCacheManager;
import org.sakaiproject.nakamura.lite.storage.spi.AbstractClientConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HBaseStorageClientPool extends AbstractClientConnectionPool {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(HBaseStorageClientPool.class);

  @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY, policy = ReferencePolicy.DYNAMIC)
  private StorageCacheManager storageManagerCache;
  private String connection = "127.0.0.1:2181";
  private Map<String, Object> properties;

  public static class ClientConnectionPoolFactory extends BasePoolableObjectFactory {
    private Map<String, Object> properties;
    private HBaseStorageClientPool pool;
    private HTablePool htablePool = null;

    public ClientConnectionPoolFactory(HBaseStorageClientPool pool, String connection,
        Map<String, Object> properties) {
      this.properties = properties;
      this.pool = pool;

      String[] connectionArray = connection.split(":");
      String host = connectionArray[0];
      String port = connectionArray[1];

      Configuration config = HBaseConfiguration.create();
      config.set("hbase.zookeeper.quorum", host + ":" + port);
      config.set("hbase.zookeeper.property.clientPort", port);
      htablePool = new HTablePool(config, 10);

    }

    @Override
    public Object makeObject() throws Exception {
      HBaseStorageClient client = new HBaseStorageClient(pool, properties, htablePool);
      return client;
    }

    @Override
    public void passivateObject(Object obj) throws Exception {
      HBaseStorageClient clientConnection = (HBaseStorageClient) obj;
      clientConnection.passivate();
      super.passivateObject(obj);
    }

    @Override
    public void activateObject(Object obj) throws Exception {
      HBaseStorageClient clientConnection = (HBaseStorageClient) obj;
      clientConnection.activate();
      super.activateObject(obj);
    }

    @Override
    public void destroyObject(Object obj) throws Exception {
      HBaseStorageClient clientConnection = (HBaseStorageClient) obj;
      clientConnection.destroy();
    }

    @Override
    public boolean validateObject(Object obj) {
      HBaseStorageClient clientConnection = (HBaseStorageClient) obj;
      try {
        clientConnection.validate();
      } catch (Exception e) {
        LOGGER.error("Failed to validate connection " + e.getMessage(), e);
        return false;
      }
      return super.validateObject(obj);
    }

  }

  @Override
  protected PoolableObjectFactory getConnectionPoolFactory() {
    return new ClientConnectionPoolFactory(this, connection, properties);
  }

  public void activate(Map<String, Object> properties) throws ClassNotFoundException {
    this.properties = properties;
    super.activate(properties);
    // this should come from the memory service ultimately.

    HBaseStorageClient client = null;
    try {
      client = (HBaseStorageClient) getClient();
      if (client == null) {
        LOGGER.warn("No connection");
      }
    } catch (ClientPoolException e) {
      LOGGER.error("Failed to check Schema", e);
    } finally {
      if (client != null) {
        client.close();
      }
    }


  }

  @Deactivate
  public void deactivate(Map<String, Object> properties) {
    super.deactivate(properties);
  }

  public StorageCacheManager getStorageCacheManager() {
    if (storageManagerCache != null) {
      return storageManagerCache;
    }
    return null;
  }
}