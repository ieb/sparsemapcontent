package org.sakaiproject.nakamura.lite.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Type<T> {

    int getTypeId();

    void save(DataOutputStream dos, Object o) throws IOException;

    T load(DataInputStream in) throws IOException;

    Class<T> getTypeClass();

}
