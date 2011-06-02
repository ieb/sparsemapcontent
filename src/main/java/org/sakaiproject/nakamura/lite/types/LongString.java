package org.sakaiproject.nakamura.lite.types;

import org.apache.commons.io.IOUtils;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A string that saves to a file and does not appear in memory. However, there is no garbage collection and strings are immutable at present.
 */
public class LongString {

    
    private static final Logger LOGGER = LoggerFactory.getLogger(LongString.class);
    private String location;
    private static String base;

    LongString(String location) {
        this.location = location;
    }
    
    public static LongString create(String content) throws IOException {
        String id = StorageClientUtils.getUuid();
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        String location = year + "/" + month + "/" + id.substring(0, 2) + "/" + id.substring(2, 4)
                + "/" + id.substring(4, 6) + "/" + id;
        LongString ls = new LongString(location);
        ls.update(content,true);
        return ls;
        
    }
    
    public void update(String content, boolean isnew) throws IOException {
        File f = new File(base, location);
        if ( isnew && f.exists()) {
            throw new IOException("LongString Storage file at location "+location+" already exists, this should not happen, nothing stored");
        }
        f.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(f);
        fw.write(content);
        fw.close();
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof LongString ) {
            return location.equals(((LongString) obj).location);
        }
        return location.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return location.hashCode();
    }
    
    @Override
    public String toString() {
        try {
        File f = new File(base, location);
        FileReader fr = new FileReader(f);
        String value = IOUtils.toString(fr);
        fr.close();
        return value;
        } catch ( IOException e) {
            LOGGER.error(e.getMessage(),e);
            return "ERROR, unable to load LongString body, see error log on server for details at "+String.valueOf(new Date());
        }
    }

    public String getLocation() {
        return location;
    }
    
}
