package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LongType implements Type<Long> {

    public int getTypeId() {
        return 1;
    }

    public void save(DataOutputStream dos, Object value) throws IOException {
        dos.writeLong((Long) value);
    }

    public Long load(DataInputStream in) throws IOException {
        return in.readLong();
    }

    public Class<Long> getTypeClass() {
        return Long.class;
    }

    public boolean accepts(Object object) {
        return (object instanceof Long);
    }
}
