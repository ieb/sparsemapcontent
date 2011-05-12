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


    public static final String VALIDATORPLUGIN = "validatorplugin";
    public static final String _ACLTOKEN = "_acltoken";
    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalTokenValidator.class);
    private PrincipalValidatorPlugin defaultPrincipalValidator = new DefaultPrincipalValidator();
    private PrincipalValidatorResolver principalValidatorResolver;

    public PrincipalTokenValidator(PrincipalValidatorResolver principalValidatorResolver) {
        this.principalValidatorResolver = principalValidatorResolver;
    }

    public boolean validatePrincipal(Content proxyPrincipalToken, String sharedKey)  {
        if ( proxyPrincipalToken == null) {
            LOGGER.debug("Failed to Validate Token at no content item ");
            return false;
        }
        if ( !proxyPrincipalToken.hasProperty(_ACLTOKEN)) {
            LOGGER.debug("Failed to Validate Token at {} no ACL Token ", proxyPrincipalToken.getPath());
            return false;
        }
        PrincipalValidatorPlugin plugin = null;
        if ( proxyPrincipalToken.hasProperty(VALIDATORPLUGIN) ) {
            plugin = principalValidatorResolver.getPluginByName((String) proxyPrincipalToken.getProperty(VALIDATORPLUGIN));
        } else {
            plugin =  defaultPrincipalValidator;
        }
        if ( plugin == null ) {
            LOGGER.debug("Failed to Validate Token at {} no plugin ");
            return false;
        }
        String hmac = signToken(proxyPrincipalToken, sharedKey, plugin);
        if ( hmac == null || !hmac.equals(proxyPrincipalToken.getProperty(_ACLTOKEN)) ) {
            LOGGER.debug("Failed to Validate Token at {} as {}, does not match ",proxyPrincipalToken.getPath(), hmac);
            return false;
        }
        boolean validate = plugin.validate(proxyPrincipalToken);
        if ( validate ) {
            LOGGER.debug("Validated Token at {} as {}  using plugin {} ",new Object[] { proxyPrincipalToken.getPath(), hmac, plugin});
        } else {
            LOGGER.debug("Invalid Token at {} as {}  using plugin {} ",new Object[] { proxyPrincipalToken.getPath(), hmac, plugin});
        }
        return validate;
    }

    public void signToken(Content token, String sharedKey ) throws StorageClientException {
        PrincipalValidatorPlugin plugin = null;
        if ( token.hasProperty(VALIDATORPLUGIN) ) {
            plugin = principalValidatorResolver.getPluginByName((String) token.getProperty(VALIDATORPLUGIN));
        } else {
            plugin = defaultPrincipalValidator;
        }
        if ( plugin == null ) {
            throw new StorageClientException("The property validatorplugin does not specify an active PricipalValidatorPlugin, cant sign");
        }
        token.setProperty(_ACLTOKEN, signToken(token, sharedKey, plugin));
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
        if ( principalToken.hasProperty(VALIDATORPLUGIN)) {
            sb.append(principalToken.getProperty(VALIDATORPLUGIN)).append("@");
        }
        for (String f : extraFields) {
            if ( principalToken.hasProperty(f)) {
                sb.append(principalToken.getProperty(f)).append("@");
            } else {
                sb.append("null").append("@");
            }
        }
        Mac m = Mac.getInstance(HMAC_SHA512);
        m.init(key);
        String message = sb.toString();
        LOGGER.debug("Signing {} ", message);
        m.update(message.getBytes("UTF-8"));
        return Base64.encodeBase64URLSafeString(m.doFinal());
    }

}
