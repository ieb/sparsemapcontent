package org.sakaiproject.nakamura.lite.types;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;

import org.sakaiproject.nakamura.lite.storage.RemoveProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Types {

    
    private static final Type<?>[] ALLTYPES = new Type<?>[]{
        new StringType(),
        new IntegerType(),
        new LongType(),
        new BooleanType(),
        new BigDecimalType(),
        new DoubleType(),
        new CalendarType(),
        new StringArrayType(),
        new IntegerArrayType(),
        new LongArrayType(),
        new BooleanArrayType(),
        new BigDecimalArrayType(),
        new DoubleArrayType(),
        new CalendarArrayType(),
        new RemovePropertyType()
    };
    private static final Type<String> UNKNOWN_TYPE = new StringType();
    private static final Logger LOGGER = LoggerFactory.getLogger(Types.class);
    private static final Type<?> NULL_TYPE = new RemovePropertyType();
    
    private static final Map<Class<?>,Type<?>> typeMap = Types.getTypeMap();
    private static final Map<Integer, Type<?>> typeByIdMap = Types.getTypeByIdMap();


    static Map<Class<?>, Type<?>> getTypeMap() {
        check();
        Builder<Class<?>, Type<?>> builder = ImmutableMap.builder();
        for ( Type<?> t : ALLTYPES) {
            builder.put(t.getTypeClass(), t);
        }
        return builder.build();
    }

    private static void check() {
        Set<Integer> ids = Sets.newHashSet();
        Set<Class<?>> classes = Sets.newHashSet();
        boolean errors = false;
        for ( Type<?> t : ALLTYPES) {
           if ( ids.contains(t.getTypeId())) {
               LOGGER.error("Type ID {} has been reused by {} ",t.getTypeId(),t);
               errors = true;
           } else {
               ids.add(t.getTypeId());
           }
        }
        for ( Type<?> t : ALLTYPES) {
            if ( classes.contains(t.getTypeClass())) {
                LOGGER.error("Type Class {} has been reused by {} ",t.getTypeClass(),t);
                errors = true;
            } else {
                classes.add(t.getTypeClass());
            }
         }
        if ( errors ) {
            throw new IllegalStateException("The Type system in the Sparse Content Store has been incorrectly " +
            		"setup with clashing classes or type IDs, the programmer must correct this error " +
            		"before the sparse content store can startup. Look in the implementation of "+Types.class.getName());
        }
    }


    static Map<Integer, Type<?>> getTypeByIdMap() {
        check();
        Builder<Integer, Type<?>> builder = ImmutableMap.builder();
        for ( Type<?> t : ALLTYPES) {
            builder.put(t.getTypeId(), t);
        }
        return builder.build();
    }

    
    
    




    /**
     * Load a Map from binary stream
     * 
     * @param output
     * @param binaryStream
     * @throws IOException
     */
    public static void loadFromStream(String key, Map<String, Object> output, InputStream binaryStream)
            throws IOException {
        DataInputStream dis = new DataInputStream(binaryStream);
        String ckey = dis.readUTF();
        if (!key.equals(ckey)) {
            throw new IOException("Body Key does not match row key, unable to read");
        }
        int size = dis.readInt();
        LOGGER.debug("Reading {} items",size);
        for (int i = 0; i < size; i++) {            
            String k = dis.readUTF();
            LOGGER.debug("Read key {} ",k);
            output.put(k,lookupTypeById(dis.readInt()).load(dis));
        }
        LOGGER.debug("Finished Reading");
        dis.close();
        binaryStream.close();
    }

    /**
     * Save a map to a binary stream
     * 
     * @param m
     *            expected to contain strings throughout
     * @return
     * @throws IOException
     */
    public static InputStream storeMapToStream(String key, Map<String, Object> m)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(key);
        int size = 0;
        for (Entry<String, ?> e : m.entrySet()) {
            Object o = e.getValue();
            if ( o != null && !(o instanceof RemoveProperty) ) {
                size++;
            }
        }

        dos.writeInt(size);
        LOGGER.debug("Write {} items",size);
        for (Entry<String, ?> e : m.entrySet()) {
            Object o = e.getValue();
            if ( o != null && !(o instanceof RemoveProperty) ) {
                String k = e.getKey();
                LOGGER.debug("Write {} ",k);
                dos.writeUTF(k);
                Type<?> t = getTypeOfObject(o);
                dos.writeInt(t.getTypeId());
                t.save(dos, o);
            }
        }
        LOGGER.debug("Finished Writen {} items",size);
        dos.flush();
        baos.flush();
        byte[] b = baos.toByteArray();
        baos.close();
        dos.close();
        return new ByteArrayInputStream(b);
    }
    
    
    private static Type<?> lookupTypeById(int typeId) {
        Type<?> t = (Type<?>) typeByIdMap.get(typeId);
        if ( t == null ) {
            LOGGER.warn("Unknown Type ID {} found ",typeId);
            t = (Type<?>) UNKNOWN_TYPE;
        }
        return t;
    }


    private static Type<?> getTypeOfObject(Object object) {
        if ( object == null) {
            return (Type<?>) NULL_TYPE;
        }
        Class<?> c = object.getClass();
        if ( typeMap.containsKey(c)) {
            return (Type<?>) typeMap.get(c);
        }
        for ( Entry<Class<?>,Type<?>> e : typeMap.entrySet()) {
            Class<?> tc = e.getKey();
            if ( tc.isAssignableFrom(c) ) {
                return (Type<?>) e.getValue();
            }
        }
        LOGGER.warn("Unknown Type For Object {}, needs to be implemented ",object);
        return (Type<?>) UNKNOWN_TYPE;
    }



}
