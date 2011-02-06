package org.sakaiproject.nakamura.lite.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StringType implements Type<String> {

    public int getTypeId() {
        return 0;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        dos.writeUTF((String) object);
    }

    public String load(DataInputStream in) throws IOException {
        return in.readUTF();
    }

    public Class<String> getTypeClass() {
        return String.class;
    }

}
