package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.Feedback;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class FileRedoLogger implements StorageClientListener {
    
    

    private static final Map<String, Object> EMPTY_MAP = ImmutableMap.of();
    private static final Logger LOGGER = LoggerFactory.getLogger(FileRedoLogger.class);
    private Map<String, Map<String, Object>> logMap = Maps.newLinkedHashMap();
    private File redoLocation;
    private File currentFile;
    private DataOutputStream dos;
    private DateFormat logFileNameFormat;
    private int maxLogFileSize;
    private Feedback feedback;

    public FileRedoLogger(String redoLogLocation, int maxLogFileSize, Feedback feedback) {
        this.maxLogFileSize = maxLogFileSize;
        logFileNameFormat = new SimpleDateFormat("yyyyMMddHHmmssZ");
        this.feedback = feedback;
        this.redoLocation = new File(redoLogLocation,logFileNameFormat.format(new Date()));
        if( !this.redoLocation.exists() ) {
            if (!this.redoLocation.mkdirs() ) {
                throw new IllegalArgumentException("Unable to create redo log at "+this.redoLocation.getPath());
            }
        }
        
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
            LogFileRecord.write(getCurrentRedoLogStream(),true, logMap);
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
            LogFileRecord.write(getCurrentRedoLogStream(),false, logMap);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(),e);
        }
        logMap.clear();
    }
    
    private String getKey(String keySpace, String columnFamily, String key, String command) {
        return command+":"+keySpace+":"+columnFamily+":"+key;
    }
    
    
    
    

    private DataOutputStream getCurrentRedoLogStream() throws IOException {
        if ( dos == null ) {
            currentFile = getNewLogFile();
            dos = new DataOutputStream(new FileOutputStream(currentFile));
            feedback.newLogFile(currentFile);
        } else if ( dos.size() > maxLogFileSize ) {
            dos.flush();
            dos.close();
            dos = null;
            currentFile = getNewLogFile();
            dos = new DataOutputStream(new FileOutputStream(currentFile));
            feedback.newLogFile(currentFile);
        }
        return dos;
    }

    private File getNewLogFile() {
        int i = 0;
        File f =  new File(redoLocation, logFileNameFormat.format(new Date())+"-"+i+".log");
        while ( f.exists() ) {
            i++;
            f =  new File(redoLocation, logFileNameFormat.format(new Date())+"-"+i+".log");
        }
        return f;
    }

    public void close() throws IOException {
        if ( dos != null ) {
            dos.flush();
            dos.close();
            dos = null;
        }
    }


    public File getLocation() {
        return redoLocation;
    }


}
