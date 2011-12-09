package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntegerType implements Type<Integer> {

    public int getTypeId() {
        return 2;
    }

    public void save(DataOutputStream dos, Object value) throws IOException {
        dos.writeInt((Integer) value);
    }

    public Integer load(DataInputStream in) throws IOException {
        return in.readInt();
    }

    public Class<Integer> getTypeClass() {
        return Integer.class;
    }
    
    public boolean accepts(Object object) {
        return (object instanceof Integer);
    }

}
