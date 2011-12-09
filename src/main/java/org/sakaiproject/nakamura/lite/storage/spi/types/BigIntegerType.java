package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class BigIntegerType implements Type<BigInteger> {

    public int getTypeId() {
        return 9;
    }

    public void save(DataOutputStream dos, Object value) throws IOException {
        dos.writeUTF(((BigInteger)value).toString());
    }

    public BigInteger load(DataInputStream in) throws IOException {
        return new BigInteger(in.readUTF());
    }

    public Class<BigInteger> getTypeClass() {
        return BigInteger.class;
    }
    
    public boolean accepts(Object object) {
        return (object instanceof BigInteger);
    }


}
