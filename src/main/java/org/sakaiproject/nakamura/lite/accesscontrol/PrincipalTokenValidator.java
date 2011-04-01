package org.sakaiproject.nakamura.lite.accesscontrol;

import org.apache.commons.codec.binary.Base64;
import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorPlugin;
import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorResolver;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PrincipalTokenValidator {


    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalTokenValidator.class);
    private SecretKeySpec key;
    private PrincipalValidatorPlugin defaultPrincipalValidator = new DefaultPrincipalValidator();
    private PrincipalValidatorResolver principalValidatorResolver;

    public PrincipalTokenValidator(String sharedKey, PrincipalValidatorResolver principalValidatorResolver) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] input = sharedKey.getBytes("UTF-8");
        byte[] data = md.digest(input);
        key = new SecretKeySpec(data, HMAC_SHA512);
    }

    public boolean validatePrincipal(Content proxyPrincipalToken)  {
        if ( proxyPrincipalToken == null) {
            return false;
        }
        if ( !proxyPrincipalToken.hasProperty("_acltoken")) {
            return false;
        }
        String hmac = null;
        try {
            hmac = getHmac(proxyPrincipalToken);
        } catch (InvalidKeyException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.debug(e.getMessage(),e);
            return false;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.debug(e.getMessage(),e);
            return false;
        } catch (IllegalStateException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.debug(e.getMessage(),e);
            return false;
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.debug(e.getMessage(),e);
            return false;
        }
        if ( !hmac.equals(proxyPrincipalToken.getProperty("_acltoken")) ) {
            return false;
        }
        if ( proxyPrincipalToken.hasProperty("validatorplugin") ) {
            PrincipalValidatorPlugin plugin = principalValidatorResolver.getPluginByName((String) proxyPrincipalToken.getProperty("validatorplugin"));
            if ( plugin == null ) {
                return false;
            } else {
                return plugin.validate(proxyPrincipalToken);
            }
        } else {
            return defaultPrincipalValidator.validate(proxyPrincipalToken);
        }
    }

    private String getHmac(Content proxyPrincipalToken) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append(proxyPrincipalToken.getPath()).append("@");
        if ( proxyPrincipalToken.hasProperty("validatorplugin")) {
            sb.append(proxyPrincipalToken.getPath()).append("@");
        }

        Mac m = Mac.getInstance(HMAC_SHA512);
        m.init(key);

        String message = sb.toString();
        m.update(message.getBytes("UTF-8"));
        return Base64.encodeBase64URLSafeString(m.doFinal());
    }

}
