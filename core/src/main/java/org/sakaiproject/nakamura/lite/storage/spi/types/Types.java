package org.sakaiproject.nakamura.lite.storage.spi.types;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;

import org.sakaiproject.nakamura.api.lite.RemoveProperty;
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
        new ISO8601DateType(),
        new CalendarType(),
        new StringArrayType(),
        new IntegerArrayType(),
        new LongArrayType(),
        new BooleanArrayType(),
        new BigDecimalArrayType(),
        new DoubleArrayType(),
        new ISO8601DateArrayType(),
        new CalendarArrayType(),
        new RemovePropertyType(),
        new LongStringArrayType(),
        new LongStringType(),
        new BigIntegerType(),
        new BigIntegerArrayType()
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
    public static void loadFromStream(String key, Map<String, Object> output, InputStream binaryStream, String type)
            throws IOException {
        DataInputStream dis = new DataInputStream(binaryStream);
        String ckey = dis.readUTF();
        if (!key.equals(ckey)) {
            throw new IOException("Body Key does not match row key, unable to read");
        }
        readMapFromStream(output, dis);
        String cftype = null;
        try {
            cftype = dis.readUTF();
        } catch (IOException e) {
            LOGGER.debug("No type specified");
        }
        if (cftype != null && !cftype.equals(type)) {
            throw new IOException(
                    "Object is not of expected column family, unable to read expected [" + type
                            + "] was [" + cftype + "]");
        }
        LOGGER.debug("Finished Reading");
        dis.close();
        binaryStream.close();
    }

    public static void readMapFromStream(Map<String, Object> output, DataInputStream dis) throws IOException {
        int size = dis.readInt();
        LOGGER.debug("Reading {} items", size);
        for (int i = 0; i < size; i++) {
            String k = dis.readUTF();
            LOGGER.debug("Read key {} ", k);
            output.put(k, lookupTypeById(dis.readInt()).load(dis));
        }
    }

    /**
     * Save a map to a binary stream
     * 
     *
     * @param m
     *            expected to be keyed by string, can contain any object that
     *            has a type.
     * @return
     * @throws IOException
     */
    // IF you change this function you will have to change it in a way that
    // either is self healing for all the data out there
    // or write a migration script. Be warned, there could be billions of
    // records out there, so be very careful
    // Appending to record is possible, if you make the loader fail safe when
    // the data isnt there. See the last writeUTF for an example.
    public static InputStream storeMapToStream(String key, Map<String, Object> m, String type)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(key);
        writeMapToStream(m, dos);
        // add the type in
        dos.writeUTF(type);
        dos.flush();
        baos.flush();
        byte[] b = baos.toByteArray();
        baos.close();
        dos.close();
        return new ByteArrayInputStream(b);
    }
    
    
    // IF you change this function you will have to change it in a way that
    // either is self healing for all the data out there
    // or write a migration script. Be warned, there could be billions of
    // records out there, so be very careful
    // Appending to record is possible, if you make the loader fail safe when
    // the data isnt there. See the last writeUTF for an example.
    public static void writeMapToStream(Map<String, Object> m,
            DataOutputStream dos) throws IOException {
        int size = 0;
        for (Entry<String, ?> e : m.entrySet()) {
            Object o = e.getValue();
            if (o != null && !(o instanceof RemoveProperty)) {
                size++;
            }
        }

        dos.writeInt(size);
        LOGGER.debug("Write {} items", size);
        for (Entry<String, ?> e : m.entrySet()) {
            Object o = e.getValue();
            if (o != null && !(o instanceof RemoveProperty)) {
                String k = e.getKey();
                LOGGER.debug("Write {} ", k);
                dos.writeUTF(k);
                Type<?> t = getTypeOfObject(o);
                dos.writeInt(t.getTypeId());
                t.save(dos, o);
            }
        }
        LOGGER.debug("Finished Writen {} items", size);

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
            Type<?> t = typeMap.get(c);
            if ( t.accepts(object) ) {
                return (Type<?>) t;
            }
        }
        for ( Entry<Class<?>,Type<?>> e : typeMap.entrySet()) {
            Type<?> t = e.getValue();
            if ( t.accepts(object) ) {
                return (Type<?>) t;
            }
        }
        LOGGER.warn("Unknown Type For Object {}, needs to be implemented ",object.getClass());
        return (Type<?>) UNKNOWN_TYPE;
    }
    
    public static byte[] toByteArray(Object o)throws IOException{
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
                
        if ( o != null && !(o instanceof RemoveProperty) ) {             
                Type<?> t = getTypeOfObject(o);
                dos.writeInt(t.getTypeId());
                t.save(dos, o);
            }
        
        dos.flush();
        baos.flush();
        byte[] b = baos.toByteArray();
        baos.close();
        dos.close();
        
        return b;
    }
    
    public static Object toObject(byte[] columnValue)throws IOException{
    	DataInputStream dis = new DataInputStream(new ByteArrayInputStream(columnValue));        
    	return lookupTypeById(dis.readInt()).load(dis);
    	
    }




}
