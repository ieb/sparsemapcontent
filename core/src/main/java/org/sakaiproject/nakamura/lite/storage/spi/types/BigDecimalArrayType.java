package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalArrayType implements Type<BigDecimal[]> {

    public int getTypeId() {
        return 1006;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        BigDecimal[] values = (BigDecimal[]) object;
        dos.writeInt(values.length);
        for ( BigDecimal s : values) {
            dos.writeUTF(s.toString());
        }
    }

    public BigDecimal[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        BigDecimal[] values = new BigDecimal[l];
        for ( int i = 0; i < l; i++ ) {
            values[i] = new BigDecimal(in.readUTF());
        }
        return values;
    }

    public Class<BigDecimal[]> getTypeClass() {
        return BigDecimal[].class;
    }

    public boolean accepts(Object object) {
        return (object instanceof BigDecimal[]);
    }

}
