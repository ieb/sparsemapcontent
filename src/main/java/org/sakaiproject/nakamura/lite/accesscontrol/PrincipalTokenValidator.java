package org.sakaiproject.nakamura.lite.accesscontrol;

import org.apache.commons.codec.binary.Base64;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
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
    private PrincipalValidatorPlugin defaultPrincipalValidator = new DefaultPrincipalValidator();
    private PrincipalValidatorResolver principalValidatorResolver;

    public PrincipalTokenValidator(PrincipalValidatorResolver principalValidatorResolver) {
        this.principalValidatorResolver = principalValidatorResolver;
    }

    public boolean validatePrincipal(Content proxyPrincipalToken, String sharedKey)  {
        if ( proxyPrincipalToken == null) {
            return false;
        }
        if ( !proxyPrincipalToken.hasProperty("_acltoken")) {
            return false;
        }
        PrincipalValidatorPlugin plugin = null;
        if ( proxyPrincipalToken.hasProperty("validatorplugin") ) {
            plugin = principalValidatorResolver.getPluginByName((String) proxyPrincipalToken.getProperty("validatorplugin"));
        } else {
            plugin =  defaultPrincipalValidator;
        }
        if ( plugin == null ) {
            return false;
        }
        String hmac = signToken(proxyPrincipalToken, sharedKey, plugin);
        if ( hmac == null || !hmac.equals(proxyPrincipalToken.getProperty("_acltoken")) ) {
            return false;
        }
        return plugin.validate(proxyPrincipalToken);
    }

    public void signToken(Content token, String sharedKey ) throws StorageClientException {
        PrincipalValidatorPlugin plugin = null;
        if ( token.hasProperty("validatorplugin") ) {
            plugin = principalValidatorResolver.getPluginByName((String) token.getProperty("validatorplugin"));
        } else {
            plugin = defaultPrincipalValidator;
        }
        if ( plugin == null ) {
            throw new StorageClientException("The property validatorplugin does not specify an active PricipalValidatorPlugin, cant sign");
        }
        token.setProperty("_acltoken", signToken(token, sharedKey, plugin));
    }

    private String signToken(Content token, String sharedKey, PrincipalValidatorPlugin plugin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] input = sharedKey.getBytes("UTF-8");
            byte[] data = md.digest(input);
            SecretKeySpec key = new SecretKeySpec(data, HMAC_SHA512);
            return getHmac(token, plugin.getProtectedFields(), key);
        } catch (InvalidKeyException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.debug(e.getMessage(),e);
            return null;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.debug(e.getMessage(),e);
            return null;
        } catch (IllegalStateException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.debug(e.getMessage(),e);
            return null;
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.debug(e.getMessage(),e);
            return null;
        }
    }


    private String getHmac(Content principalToken, String[] extraFields, SecretKeySpec key) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append(principalToken.getPath()).append("@");
        if ( principalToken.hasProperty("validatorplugin")) {
            sb.append(principalToken.getPath()).append("@");
        }
        for (String f : extraFields) {
            if ( principalToken.hasProperty("validatorplugin")) {
                sb.append(principalToken.getProperty(f)).append("@");
            } else {
                sb.append("null").append("@");
            }
        }
        Mac m = Mac.getInstance(HMAC_SHA512);
        m.init(key);
        String message = sb.toString();
        m.update(message.getBytes("UTF-8"));
        return Base64.encodeBase64URLSafeString(m.doFinal());
    }

}
