package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LongArrayType implements Type<Long[]> {

    public int getTypeId() {
        return 1001;
    }

    public void save(DataOutputStream dos, Object object ) throws IOException {
        Long[] values;
        if ( object instanceof long[] ) {
            // sadly, autoboxing does not work for primitive types.
            long[] primitiveArray = (long[]) object;
            values = new Long[primitiveArray.length];
            for ( int i = 0; i < primitiveArray.length; i++ ) {
                values[i] = primitiveArray[i];
            }
        } else {
            values = (Long[]) object;
        }
        dos.writeInt(values.length);
        for ( Long s : values) {
            dos.writeLong(s);
        }
    }

    public Long[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        Long[] values = new Long[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = in.readLong();
        }
        return values;
    }

    public Class<Long[]> getTypeClass() {
        return Long[].class;
    }

    public boolean accepts(Object object) {
        return (object instanceof Long[] || object instanceof long[]);
    }
}
