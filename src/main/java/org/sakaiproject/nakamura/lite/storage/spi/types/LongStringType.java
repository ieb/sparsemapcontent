package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LongStringType implements Type<LongString> {

    public int getTypeId() {
        return 100;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        LongString ls = null;
        if ( object instanceof LongString ) {
            ls = (LongString) object;
        } else {
            ls = LongString.create(String.valueOf(object));
        }
        dos.writeUTF(ls.getLocation());
    }

    public LongString load(DataInputStream in) throws IOException {
        return new LongString(in.readUTF());
    }

    public Class<LongString> getTypeClass() {
        return LongString.class;
    }

    public boolean accepts(Object object) {
        if ( object instanceof LongString ) {
            return true;
        }
        if ( object instanceof String) {
            if (StringType.getLengthLimit() > 0 &&  ((String) object).length() >= StringType.getLengthLimit() ) {
                return true;
            }
            return false;
        }
        return false;
    }
    

    
    

}
