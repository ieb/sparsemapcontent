package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DoubleType implements Type<Double> {

    public int getTypeId() {
        return 3;
    }

    public void save(DataOutputStream dos, Object value) throws IOException {
        dos.writeDouble((Double) value);
    }

    public Double load(DataInputStream in) throws IOException {
        return in.readDouble();
    }

    public Class<Double> getTypeClass() {
        return Double.class;
    }

    public boolean accepts(Object object) {
        return (object instanceof Double);
    }
}
