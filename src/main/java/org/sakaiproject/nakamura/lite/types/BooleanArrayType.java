package org.sakaiproject.nakamura.lite.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BooleanArrayType implements Type<boolean[]> {

    public int getTypeId() {
        return 1005;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        boolean[] values = (boolean[]) object;
        dos.writeInt(values.length);
        for ( boolean s : values) {
            dos.writeBoolean(s);
        }
    }

    public boolean[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        boolean[] values = new boolean[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = in.readBoolean();
        }
        return values;
    }

    public Class<boolean[]> getTypeClass() {
        return boolean[].class;
    }
    
    public boolean accepts(Object object) {
        return (object instanceof boolean[]);
    }


}
