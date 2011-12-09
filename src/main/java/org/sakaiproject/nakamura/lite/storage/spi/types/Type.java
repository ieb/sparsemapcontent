package org.sakaiproject.nakamura.lite.storage.spi.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Type<T> {

    /**
     * @return the type ID of this type. Once a type ID has been assigned to an
     *         object type it can never be reused.
     */
    int getTypeId();

    /**
     * Safe the type to a data output stream
     * @param dos
     * @param o
     * @throws IOException
     */
    void save(DataOutputStream dos, Object o) throws IOException;

    /**
     * Load the type from a data output stream
     * @param in
     * @return
     * @throws IOException
     */
    T load(DataInputStream in) throws IOException;

    /**
     * @return get the class of the type
     */
    Class<T> getTypeClass();

    /**
     * return true if the Type can save the object.
     * @param object
     * @return
     */
    boolean accepts(Object object);

}
