package uk.co.tfd.sm.authn.token;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

public class KeyHolder {

	public static final String HMAC = "HmacSHA512";
	public static final String SHA = "SHA-512";

	private long expires;
	private Key key;

	private byte[] keySpec;
	
	public KeyHolder() {
	}
	
	public KeyHolder(String spec) {
		String[] specParts = StringUtils.split(spec,":");
		keySpec = Base64.decodeBase64(specParts[0]);
		expires = Long.parseLong(specParts[1]);
	}
	
	@Override
	public String toString() {
		return  Base64.encodeBase64URLSafeString(keySpec)+":"+expires;
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
