package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DoubleArrayType implements Type<Double[]> {

    public int getTypeId() {
        return 1003;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        Double[] values;
        if ( object instanceof double[] ) {
            // sadly, autoboxing does not work for primitive types.
            double[] primitiveArray = (double[]) object;
            values = new Double[primitiveArray.length];
            for ( int i = 0; i < primitiveArray.length; i++ ) {
                values[i] = primitiveArray[i];
            }
        } else {
            values = (Double[]) object;
        }
        dos.writeInt(values.length);
        for ( Double s : values) {
            dos.writeDouble(s);
        }
    }

    public Double[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        Double[] values = new Double[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = in.readDouble();
        }
        return values;
    }

    public Class<Double[]> getTypeClass() {
        return Double[].class;
    }

    public boolean accepts(Object object) {
        return (object instanceof Double[] || object instanceof double[]);
    }
}
