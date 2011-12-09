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
package org.sakaiproject.nakamura.lite.storage.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.lite.storage.spi.RowHasher;
import org.sakaiproject.nakamura.lite.storage.spi.content.StreamedContentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * Store content bodies in GridFS.
 * http://www.mongodb.org/display/DOCS/GridFS
 * 
 * This file started as a copy of org.sakaiproject.nakamura.lite.content.FileStreamContentHelper 
 * in case you didn't notice. It took remarkably little changing to turn it into a GridFS helper.
 */
public class GridFSContentHelper implements StreamedContentHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GridFSContentHelper.class);
    
    private static final String STORE_LOCATION_FIELD = Repository.SYSTEM_PROP_PREFIX + "bodyLocation";

    private GridFS contentBodies;
    
    private RowHasher rowHasher;

    public GridFSContentHelper(DB mongodb, RowHasher rowHasher, Map<String, Object> properties) {
    	this.rowHasher = rowHasher;
    	
    	String bucket = StorageClientUtils.getSetting(properties.get(MongoClientPool.PROP_BUCKET), GridFS.DEFAULT_BUCKET);
    	this.contentBodies = new GridFS(mongodb, bucket);
    }
    
	public InputStream readBody(String keySpace, String columnFamily, String contentBlockId, String streamId,
            Map<String, Object> content) throws IOException {
    	// give me whatever is stored in the property _bodyLocation/streamId
        String path = (String) content.get(StorageClientUtils.getAltField(STORE_LOCATION_FIELD, streamId));
        LOGGER.debug("Reading from {} as body of {}:{}:{} ", new Object[] { path, keySpace, columnFamily, contentBlockId });
        GridFSDBFile file = contentBodies.findOne(path);
        if ( file != null ) {
            return file.getInputStream();
        } else {
            return null;
        }
    }

	public Map<String, Object> writeBody(String keySpace, String columnFamily, String contentId,
            String contentBlockId, String streamId, Map<String, Object> content, InputStream in) throws IOException,
            StorageClientException {
        String path = getPath(keySpace, columnFamily, contentBlockId);
        GridFSInputFile file = contentBodies.createFile(in, path);
        file.save();
        LOGGER.debug("Wrote {} bytes to {} as body of {}:{}:{} stream {} ", new Object[] { file.getLength(), path,
                keySpace, columnFamily, contentBlockId, streamId });
        Map<String, Object> metadata = Maps.newHashMap();
        metadata.put(StorageClientUtils.getAltField(Content.LENGTH_FIELD, streamId), file.getLength());
        metadata.put(StorageClientUtils.getAltField(Content.BLOCKID_FIELD, streamId), contentBlockId);
        metadata.put(StorageClientUtils.getAltField(STORE_LOCATION_FIELD, streamId), path);
        return metadata;
    }

    private String getPath(String keySpace, String columnFamily, String contentBlockId)
            throws StorageClientException {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        String rowHash = rowHasher.rowHash(keySpace, columnFamily, contentBlockId);
        return year + "/" + month + "/" + rowHash.substring(0, 2) + "/" + rowHash.substring(2, 4)
                + "/" + rowHash.substring(4, 6) + "/" + rowHash;
    }

	public boolean hasStream(Map<String, Object> content, String streamId ) {
        String path = (String) content.get(StorageClientUtils.getAltField(STORE_LOCATION_FIELD, streamId));
        GridFSDBFile file = contentBodies.findOne(path);
        return file != null;
    }
}