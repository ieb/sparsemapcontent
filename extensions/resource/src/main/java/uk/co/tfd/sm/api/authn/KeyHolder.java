package uk.co.tfd.sm.api.authn;

import java.io.Serializable;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

public class KeyHolder implements Serializable {

	public static final String HMAC = "HmacSHA512";
	public static final String SHA = "SHA-512";

	/**
	 * 
	 */
	private static final long serialVersionUID = 7808366141279637770L;
	private long expires;
	private transient Key key;

	private byte[] keySpec;
	
	public KeyHolder() {
	}
	
	public void reset(byte[] seed, long ttl) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(SHA);
		keySpec = md.digest(seed);
		expires = System.currentTimeMillis() + ttl;
	}
	

	public boolean hasExpired() {
		return System.currentTimeMillis() > expires;
	}

	public Key getKey() {
		if ( hasExpired()) {
			return null;
		}
		if ( key == null ) {
			key = new SecretKeySpec(keySpec, HMAC);
		}
		return key;
	}
	
	

}
