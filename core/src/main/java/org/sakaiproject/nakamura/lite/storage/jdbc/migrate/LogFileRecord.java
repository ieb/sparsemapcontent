package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.lite.storage.spi.types.Types;

import com.google.common.collect.Maps;

public class LogFileRecord {

    private static final String START_MARKER = "<";
    private static final String END_MARKER = ">";

    public static void write(DataOutputStream dos, boolean committed,
            Map<String, Map<String, Object>> logMap) throws IOException {
            dos.writeUTF(START_MARKER);
            dos.writeBoolean(committed);
            dos.writeInt(logMap.size());
            for ( Entry<String, Map<String, Object>> e : logMap.entrySet()) {
                String k = e.getKey();
                dos.writeUTF(k);
                Types.writeMapToStream(e.getValue(), dos);
                String[] parts = StringUtils.split(k,":",3);
                String columnFamily = parts[2];
                dos.writeUTF(columnFamily);
            }
            dos.writeUTF(END_MARKER);
    }
    
    public static boolean read(DataInputStream din, Map<String, Map<String, Object>> logMap) throws IOException {
        logMap.clear();
        if ( !START_MARKER.equals(din.readUTF())) {
            throw new IllegalStateException("Input Stream Not at start record marker ");
        } 
        boolean committed = din.readBoolean();
        int size = din.readInt();
        for ( int i = 0; i < size; i++ ) {
            String k = din.readUTF();
            Map<String, Object> record = Maps.newHashMap();
            Types.readMapFromStream(record, din);
            @SuppressWarnings("unused")
            String columnFamily = din.readUTF();
            logMap.put(k, record);
        }
        if ( !END_MARKER.equals(din.readUTF()) ) {
            throw new IllegalStateException("Input Stream Not at end record marker after reading record");
        } 
        return committed;
        
    }

}
