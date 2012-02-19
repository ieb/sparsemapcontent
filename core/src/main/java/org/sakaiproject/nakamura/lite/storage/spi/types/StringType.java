package org.sakaiproject.nakamura.lite.storage.spi.types;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StringType implements Type<String> {


    private static int lengthLimit;

    public int getTypeId() {
        return 0;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        dos.writeUTF((String) object);
    }

    public String load(DataInputStream in) throws IOException {
        return in.readUTF();
    }

    public Class<String> getTypeClass() {
        return String.class;
    }

    public boolean accepts(Object object) {
        if ( object instanceof String) {
            if (StringType.lengthLimit > 0 &&  ((String) object).length() > StringType.lengthLimit ) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    public static int getLengthLimit() {
        return lengthLimit;
    }
    
    public static void setLengthLimit(int stringLengthLimit) {
        StringType.lengthLimit = stringLengthLimit;
    }

    
    

}
