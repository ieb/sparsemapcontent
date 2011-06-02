package org.sakaiproject.nakamura.lite.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LongArrayType implements Type<long[]> {

    public int getTypeId() {
        return 1001;
    }

    public void save(DataOutputStream dos, Object object ) throws IOException {
        long[] values = (long[]) object;
        dos.writeInt(values.length);
        for ( long s : values) {
            dos.writeLong(s);
        }
    }

    public long[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        long[] values = new long[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = in.readLong();
        }
        return values;
    }

    public Class<long[]> getTypeClass() {
        return long[].class;
    }

    public boolean accepts(Object object) {
        return (object instanceof long[]);
    }
}
