package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalType implements Type<BigDecimal> {

    public int getTypeId() {
        return 6;
    }

    public void save(DataOutputStream dos, Object value) throws IOException {
        dos.writeUTF(((BigDecimal)value).toString());
    }

    public BigDecimal load(DataInputStream in) throws IOException {
        return new BigDecimal(in.readUTF());
    }

    public Class<BigDecimal> getTypeClass() {
        return BigDecimal.class;
    }
    
    public boolean accepts(Object object) {
        return (object instanceof BigDecimal);
    }


}
