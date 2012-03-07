package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteArrayType implements Type<byte[]> {
    /**
     * This is the maximum size allowed for a byte[]. The reason there is a
     * maximum is to ensure that the cache is not polluted with large objects
     * that would destroy performance. 64K is a guess of what is too large, 
     * but probably a safe level. Clients can still get round this by splitting
     * their byte[]s into many seperate properties, but there is nothing that I 
     * can really do about that at this level.
     */
    private int MAX_BYTE_ARRAY_SIZE = 64*1024;

    public int getTypeId() {
        return 1010;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        byte[] values = (byte[]) object;
        
        if ( values.length > MAX_BYTE_ARRAY_SIZE  ) {
            throw new IllegalArgumentException("Byte[] exceeds maximum allowed length of "+MAX_BYTE_ARRAY_SIZE);
        }
        dos.writeInt(values.length);
        dos.write(values);
    }

    public byte[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        byte[] b = new byte[l];
        in.read(b);
        return b;
    }

    public Class<byte[]> getTypeClass() {
        return byte[].class;
    }

    public boolean accepts(Object object) {
        return (object instanceof byte[]);
    }

}
