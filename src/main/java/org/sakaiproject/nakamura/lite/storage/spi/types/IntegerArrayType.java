package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntegerArrayType implements Type<Integer[]> {

    public int getTypeId() {
        return 1002;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        Integer[] values;
        if ( object instanceof int[] ) {
            // sadly, autoboxing does not work for primitive types.
            int[] primitiveArray = (int[]) object;
            values = new Integer[primitiveArray.length];
            for ( int i = 0; i < primitiveArray.length; i++ ) {
                values[i] = primitiveArray[i];
            }
        } else {
            values = (Integer[]) object;
        }
        dos.writeInt(values.length);
        for ( Integer s : values) {
            dos.writeInt(s);
        }
    }

    public Integer[] load(DataInputStream in) throws IOException {
        Integer l = in.readInt();
        Integer[] values = new Integer[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = in.readInt();
        }
        return values;
    }

    public Class<Integer[]> getTypeClass() {
        return Integer[].class;
    }

    public boolean accepts(Object object) {
        return (object instanceof Integer[] || object instanceof int[]);
    }
}
