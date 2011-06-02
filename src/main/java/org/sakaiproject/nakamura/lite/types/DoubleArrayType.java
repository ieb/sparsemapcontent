package org.sakaiproject.nakamura.lite.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DoubleArrayType implements Type<double[]> {

    public int getTypeId() {
        return 1003;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        double[] values = (double[]) object;
        dos.writeInt(values.length);
        for ( double s : values) {
            dos.writeDouble(s);
        }
    }

    public double[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        double[] values = new double[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = in.readDouble();
        }
        return values;
    }

    public Class<double[]> getTypeClass() {
        return double[].class;
    }

    public boolean accepts(Object object) {
        return (object instanceof double[]);
    }
}
