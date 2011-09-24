package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.lite.storage.StorageClientListener;
import org.sakaiproject.nakamura.lite.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class FileRedoLogger implements StorageClientListener {
    
    

    private static final Map<String, Object> EMPTY_MAP = ImmutableMap.of();
    private static final String START_MARKER = "<";
    private static final String END_MARKER = ">";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileRedoLogger.class);
    private Map<String, Map<String, Object>> logMap = Maps.newLinkedHashMap();
    private File redoLocation;
    private File currentFile;
    private DataOutputStream dos;
    private DateFormat logFileNameFormat;
    private int maxLogFileSize;
    private Logger feedback;

    public FileRedoLogger(String redoLogLocation, int maxLogFileSize, Logger feedback) {
        this.maxLogFileSize = maxLogFileSize;
        logFileNameFormat = new SimpleDateFormat("yyyyMMddHHmmssZ");
        this.feedback = feedback;
        this.redoLocation = new File(redoLogLocation,logFileNameFormat.format(new Date()));
        
    }


    public void delete(String keySpace, String columnFamily, String key) {
        logMap.put(getKey(keySpace, columnFamily, key, "d"), EMPTY_MAP);
    }

    public void after(String keySpace, String columnFamily, String key, Map<String, Object> mapAfter) {
        logMap.put(getKey(keySpace, columnFamily, key, "a"), mapAfter);
    }


    public void before(String keySpace, String columnFamily, String key,
            Map<String, Object> mapBefore) {
        logMap.put(getKey(keySpace, columnFamily, key, "b"), mapBefore);        
    }

    public void commit() {
        try {
            writeLog(true);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(),e);
        }
        logMap.clear();
    }


    public void begin() {
        logMap.clear();
    }

    public void rollback() {
        try {
            writeLog(false);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(),e);
        }
        logMap.clear();
    }
    
    private String getKey(String keySpace, String columnFamily, String key, String command) {
        return command+":"+keySpace+":"+columnFamily+":"+key;
    }
    
    
    
    
    private void writeLog(boolean committed) throws IOException {
        DataOutputStream dos = getCurrentRedoLogStream();
        dos.writeUTF(START_MARKER);
        dos.writeBoolean(committed);
        dos.writeInt(logMap.size());
        for ( Entry<String, Map<String, Object>> e : logMap.entrySet()) {
            String k = e.getKey();
            String[] parts = StringUtils.split(k,":",3);
            String columnFamily = parts[2];
            Types.writeMapToStream(k, e.getValue(), columnFamily, dos);
        }
        dos.writeUTF(END_MARKER);
    }

    private DataOutputStream getCurrentRedoLogStream() throws IOException {
        if ( dos == null ) {
            currentFile = getNewLogFile();
            dos = new DataOutputStream(new FileOutputStream(currentFile));
            feedback.info("Switched Log file to {} ",currentFile.getAbsoluteFile());
        } else if ( dos.size() > maxLogFileSize ) {
            dos.flush();
            dos.close();
            dos = null;
            currentFile = getNewLogFile();
            dos = new DataOutputStream(new FileOutputStream(currentFile));
            feedback.info("Switched Log file to {} ",currentFile.getAbsoluteFile());
        }
        return dos;
    }

    private File getNewLogFile() {
        return new File(redoLocation, logFileNameFormat.format(new Date())+".log");
    }

    public void close() throws IOException {
        if ( dos != null ) {
            dos.flush();
            dos.close();
            dos = null;
        }
    }


}
