package uk.co.tfd.sm.proxy;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class Signature {
	
	  private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	  /**
	   * Calculate an RFC2104 compliant HMAC (Hash-based Message Authentication Code)
	   * 
	   * @param data
	   *          The data to be signed. This data is pushed through a hex converter in this
	   *          method, so there is no need to do this before generating the HMAC.
	   * @param key
	   *          The signing key.
	   * @return The Base64-encoded RFC 2104-compliant HMAC signature.
	   * @throws java.security.SignatureException
	   *           when signature generation fails
	   */
	  
	  public static String calculateRFC2104HMAC(String data, String key) 
	  	throws java.security.SignatureException {
	    if (data == null) {
	      throw new IllegalArgumentException("String data == null");
	    }
	    if (key == null) {
	      throw new IllegalArgumentException("String key == null");
	    }
		  try {
			  // Get an hmac_sha1 key from the raw key bytes
			  byte[] keyBytes = key.getBytes("UTF-8");
			  SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);

			  // Get an hmac_sha1 Mac instance and initialize with the signing key
			  Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			  mac.init(signingKey);

			  // Compute the hmac on input data bytes
			  byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));

			  // Convert raw bytes to encoding
			  return Base64.encodeBase64URLSafeString(rawHmac);

		  } catch (NoSuchAlgorithmException e) {
			  throw new SignatureException("Failed to generate HMAC : " + e.getMessage(), e);
		  } catch (InvalidKeyException e) {
			  throw new SignatureException("Failed to generate HMAC : " + e.getMessage(), e);
		  } catch (UnsupportedEncodingException e) {
			  throw new SignatureException("Failed to generate HMAC : " + e.getMessage(), e);
		  }
	  }

}
