package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class BigIntegerArrayType implements Type<BigInteger[]> {

    public int getTypeId() {
        return 1009;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        BigInteger[] values = (BigInteger[]) object;
        dos.writeInt(values.length);
        for ( BigInteger s : values) {
            dos.writeUTF(s.toString());
        }
    }

    public BigInteger[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        BigInteger[] values = new BigInteger[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = new BigInteger(in.readUTF());
        }
        return values;
    }

    public Class<BigInteger[]> getTypeClass() {
        return BigInteger[].class;
    }

    public boolean accepts(Object object) {
        return (object instanceof BigInteger[]);
    }

}
