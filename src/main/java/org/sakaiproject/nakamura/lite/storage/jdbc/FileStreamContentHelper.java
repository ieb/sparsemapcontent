package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.sakaiproject.nakamura.lite.content.ContentManager;
import org.sakaiproject.nakamura.lite.content.StreamedContentHelper;
import org.sakaiproject.nakamura.lite.storage.RowHasher;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;

import com.google.common.collect.Maps;

public class FileStreamContentHelper implements StreamedContentHelper {

    private static final String DEFAULT_FILE_STORE = "store";
    private static final String CONFIG_STOREBASE = "store-base-dir";
    private String fileStore;
    private RowHasher rowHasher;

    public FileStreamContentHelper(RowHasher rowHasher, Map<String,Object> properties) {
        fileStore = StorageClientUtils.getSetting(properties.get(CONFIG_STOREBASE), DEFAULT_FILE_STORE);
        this.rowHasher = rowHasher;
    }
    
    @Override
    public Map<String, Object> writeBody(String keySpace, String columnFamily, String contentId,
            String contentBlockId, InputStream in) throws IOException {
        String path = getPath(keySpace, columnFamily, contentBlockId);
        File file = new File(path);
        file.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(file);
        long length = IOUtils.copyLarge(in, out);
        out.close();
        Map<String, Object> metadata = Maps.newHashMap();
        metadata.put(ContentManager.LENGTH_FIELD, StorageClientUtils.toStore(length));
        return metadata;
    }

    private String getPath(String keySpace, String columnFamily, String contentBlockId) {
        String rowHash = rowHasher.rowHash(keySpace, columnFamily, contentBlockId);
        return fileStore+"/"+rowHash.substring(0,2)+"/"+rowHash.substring(2,4)+"/"+rowHash.substring(4,6)+"/"+rowHash;
    }

    @Override
    public InputStream readBody(String keySpace, String columnFamily, String contentBlockId) throws IOException {
        String path = getPath(keySpace, columnFamily, contentBlockId);
        File file = new File(path);
        return new FileInputStream(file);
    }

}
