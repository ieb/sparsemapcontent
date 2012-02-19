package org.sakaiproject.nakamura.lite;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CheckRowHash {

    private static final String USAGE = "Generate a RowHash\n" +
    		"java "+CheckRowHash.class.getName()+" <keySpace> <columnFamily> <key> [<hashAlg (default:SHA1)]";

    @SuppressWarnings(value="NP_ALWAYS_NULL", justification="How can System.err be null ?")
    public static void main(String[] argv) throws StorageClientException {
    //    argv = new String[] { 
    //            "n", "cn", "a:af1148/contacts",
    //            "n", "cn", "a:ae6782"
    //           };
        if ( argv.length < 3 ) {
            System.err.println(USAGE);
        }
        String hashAlg = "SHA1";
        if ( argv.length%3 == 1) {
            hashAlg = argv[argv.length-1];
        }
        
        for ( int i = 0; i < argv.length; i+=3) {
        String hash = rowHash(argv[0+i], argv[1+i], argv[2+i], hashAlg);
        StringBuilder sb = new StringBuilder();
        sb.append("Hash for ").append(argv[0+i]).append(":").append(argv[1+i]).append(":").append(argv[2+i]).append(" is ").append(hash).append("\n");
        sb.append("To find rows \n");
        if ("cn".equals(argv[1+i])) {
            sb.append("  select * from cn_css_b where rid = '").append(hash).append("'\n");
        } else if ("ac".equals(argv[1+i])) {
            sb.append("  select * from ac_css_b where rid = '").append(hash).append("'\n");
        } else if ("au".equals(argv[1+i])) {
            sb.append("  select * from au_css_b where rid = '").append(hash).append("'\n");
        } else {
            sb.append("  select * from cn_css_b where rid = '").append(hash).append("'\n");
        }
        System.out.println(sb.toString());
    }
    }

    public static String rowHash(String keySpace, String columnFamily, String key, String rowidHash)
            throws StorageClientException {
        MessageDigest hasher;
        try {
            hasher = MessageDigest.getInstance(rowidHash);
        } catch (NoSuchAlgorithmException e1) {
            throw new StorageClientException("Unable to get hash algorithm " + e1.getMessage(), e1);
        }
        String keystring = keySpace + ":" + columnFamily + ":" + key;
        byte[] ridkey;
        try {
            ridkey = keystring.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            ridkey = keystring.getBytes();
        }
        return StorageClientUtils.encode(hasher.digest(ridkey));
    }
}
