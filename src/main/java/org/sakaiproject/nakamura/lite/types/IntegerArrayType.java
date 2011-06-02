package org.sakaiproject.nakamura.lite.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntegerArrayType implements Type<int[]> {

    public int getTypeId() {
        return 1002;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        int[] values = (int[]) object;
        dos.writeInt(values.length);
        for ( int s : values) {
            dos.writeInt(s);
        }
    }

    public int[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        int[] values = new int[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = in.readInt();
        }
        return values;
    }

    public Class<int[]> getTypeClass() {
        return int[].class;
    }

    public boolean accepts(Object object) {
        return (object instanceof int[]);
    }
}
