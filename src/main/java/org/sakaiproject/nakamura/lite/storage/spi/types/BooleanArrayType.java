package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BooleanArrayType implements Type<Boolean[]> {

    public int getTypeId() {
        return 1005;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        Boolean[] values;
        if ( object instanceof boolean[] ) {
            // sadly, autoboxing does not work for primitive types.
            boolean[] primitiveArray = (boolean[]) object;
            values = new Boolean[primitiveArray.length];
            for ( int i = 0; i < primitiveArray.length; i++ ) {
                values[i] = primitiveArray[i];
            }
        } else {
            values = (Boolean[]) object;
        }
        dos.writeInt(values.length);
        for ( Boolean s : values) {
            dos.writeBoolean(s);
        }
    }

    public Boolean[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        Boolean[] values = new Boolean[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = in.readBoolean();
        }
        return values;
    }

    public Class<Boolean[]> getTypeClass() {
        return Boolean[].class;
    }
    
    public boolean accepts(Object object) {
        return (object instanceof Boolean[] || object instanceof boolean[]);
    }


}
