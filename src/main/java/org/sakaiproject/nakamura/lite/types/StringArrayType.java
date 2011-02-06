package org.sakaiproject.nakamura.lite.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StringArrayType implements Type<String[]> {

    public int getTypeId() {
        return 1000;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        String[] values = (String[]) object;
        dos.writeInt(values.length);
        for ( String s : values) {
            dos.writeUTF(s);
        }
    }

    public String[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        String[] values = new String[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = in.readUTF();
        }
        return values;
    }

    public Class<String[]> getTypeClass() {
        return String[].class;
    }

}
