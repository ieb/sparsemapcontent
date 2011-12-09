package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BooleanType implements Type<Boolean> {

    public int getTypeId() {
        return 5;
    }

    public void save(DataOutputStream dos, Object value) throws IOException {
        dos.writeBoolean((Boolean) value);
    }

    public Boolean load(DataInputStream in) throws IOException {
        return in.readBoolean();
    }

    public Class<Boolean> getTypeClass() {
        return Boolean.class;
    }
    
    public boolean accepts(Object object) {
        return (object instanceof Boolean);
    }


}
