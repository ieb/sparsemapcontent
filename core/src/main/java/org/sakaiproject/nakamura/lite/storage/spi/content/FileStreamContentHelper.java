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
package org.sakaiproject.nakamura.lite.storage.spi.content;

import com.google.common.collect.Maps;

import org.apache.commons.io.IOUtils;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.lite.storage.spi.AbstractClientConnectionPool;
import org.sakaiproject.nakamura.lite.storage.spi.RowHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

public class FileStreamContentHelper implements StreamedContentHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileStreamContentHelper.class);
    private static final String STORE_LOCATION_FIELD = Repository.SYSTEM_PROP_PREFIX + "bodyLocation";
    private String fileStore;
    private RowHasher rowHasher;

    public FileStreamContentHelper(RowHasher rowHasher, Map<String, Object> properties) {
        fileStore = StorageClientUtils.getSetting(properties.get(AbstractClientConnectionPool.FS_STORE_BASE_DIR),
                AbstractClientConnectionPool.DEFAULT_FILE_STORE);
        this.rowHasher = rowHasher;
    }

    public Map<String, Object> writeBody(String keySpace, String columnFamily, String contentId,
            String contentBlockId, String streamId, Map<String, Object> content, InputStream in) throws IOException,
            StorageClientException {
        String path = getPath(keySpace, columnFamily, contentBlockId);
        File file = new File(fileStore + "/" + path);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                throw new IOException("Unable to create directory " + parentFile.getAbsolutePath());
            }
        }
        FileOutputStream out = new FileOutputStream(file);
        long length = IOUtils.copyLarge(in, out);
        out.close();
        LOGGER.debug("Wrote {} bytes to {} as body of {}:{}:{} stream {} ", new Object[] { length, path,
                keySpace, columnFamily, contentBlockId, streamId });
        Map<String, Object> metadata = Maps.newHashMap();
        metadata.put(StorageClientUtils.getAltField(Content.LENGTH_FIELD, streamId), length);
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

    public InputStream readBody(String keySpace, String columnFamily, String contentBlockId, String streamId,
            Map<String, Object> content) throws IOException {
        String path = (String) content.get(StorageClientUtils.getAltField(STORE_LOCATION_FIELD, streamId));
        LOGGER.debug("Reading from {} as body of {}:{}:{} ", new Object[] { path, keySpace,
                columnFamily, contentBlockId });
        File file = new File(fileStore + "/" + path);
        if ( file.exists() ) {
            return new FileInputStream(file);
        } else {
            return null;
        }
    }

    public boolean hasStream(Map<String, Object> content, String streamId ) {
        String path = (String) content.get(StorageClientUtils.getAltField(STORE_LOCATION_FIELD, streamId));
        File file = new File(fileStore + "/" + path);
        return file.exists();
    }


}
