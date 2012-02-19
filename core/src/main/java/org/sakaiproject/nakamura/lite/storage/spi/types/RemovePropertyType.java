package org.sakaiproject.nakamura.lite.storage.spi.types;

import org.sakaiproject.nakamura.api.lite.RemoveProperty;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RemovePropertyType implements Type<RemoveProperty> {

    public int getTypeId() {
        return 7;
    }

    public void save(DataOutputStream dos, Object object) throws IOException {
    }

    public RemoveProperty load(DataInputStream in) throws IOException {
        return new RemoveProperty();
    }

    public Class<RemoveProperty> getTypeClass() {
        return RemoveProperty.class;
    }

    public boolean accepts(Object object) {
        return (object instanceof RemoveProperty);
    }
}
