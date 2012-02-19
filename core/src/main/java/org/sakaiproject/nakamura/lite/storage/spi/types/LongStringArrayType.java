package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LongStringArrayType implements Type<LongString[]> {


    public int getTypeId() {
        return 1100;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        LongString[] values = null;
        if ( object instanceof String[] ) {
            values = new LongString[((String[]) object).length];
            int i = 0;
            for ( String s : (String[])object) {
                values[i++] = LongString.create(s);
            }
        } else {
           values = (LongString[])object;
        }
        dos.writeInt(values.length);
        for ( LongString ls : values) {
            
            dos.writeUTF(ls.getLocation());
        }
    }

    public LongString[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        LongString[] values = new LongString[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = new LongString(in.readUTF());
        }
        return values;
    }

    public Class<LongString[]> getTypeClass() {
        return LongString[].class;
    }
    
    public boolean accepts(Object object) {
        if ( object instanceof LongString[] ) {
            return true;
        }
        if (object instanceof String[]) {
            if (StringType.getLengthLimit() > 0) {
                for ( String s : (String[])object) {
                    if ( s.length() > StringType.getLengthLimit()) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    
}
