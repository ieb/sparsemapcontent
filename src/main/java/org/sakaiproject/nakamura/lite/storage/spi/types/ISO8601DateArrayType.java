package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.TimeZone;

import org.sakaiproject.nakamura.api.lite.util.ISO8601Date;

public class ISO8601DateArrayType implements Type<ISO8601Date[]> {

    public int getTypeId() {
        return 1008;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
        ISO8601Date[] values = (ISO8601Date[]) object;
        dos.writeInt(values.length);
        for ( ISO8601Date calendar : values) {
            dos.writeLong(calendar.getTimeInMillis());
            dos.writeUTF(calendar.getTimeZone().getID());
            dos.writeBoolean(calendar.isDate());
        }
    }

    public ISO8601Date[] load(DataInputStream in) throws IOException {
        int l = in.readInt();
        ISO8601Date[] values = new ISO8601Date[l];
        for ( int i = 0; i < l; i++ ) {
            long millis = in.readLong();
            TimeZone zone = TimeZone.getTimeZone(in.readUTF());
            boolean date = in.readBoolean();
            values[i] = new ISO8601Date();
            values[i].setTimeInMillis(millis);
            values[i].setTimeZone(zone);
            values[i].setDate(date);
        }
        return values;
    }

    public Class<ISO8601Date[]> getTypeClass() {
        return ISO8601Date[].class;
    }

    public boolean accepts(Object object) {
        return (object instanceof ISO8601Date[]);
    }

}
