package org.sakaiproject.nakamura.lite.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Type<T> {

    /**
     * @return the type ID of this type. Once a type ID has been assigned to an
     *         object type it can never be reused.
     */
    int getTypeId();

    void save(DataOutputStream dos, Object o) throws IOException;

    T load(DataInputStream in) throws IOException;

    Class<T> getTypeClass();

}
