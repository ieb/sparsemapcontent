package uk.co.tfd.sm.authn.token;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;

import javax.crypto.Mac;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.memory.Cache;
import org.sakaiproject.nakamura.api.memory.CacheManagerService;
import org.sakaiproject.nakamura.api.memory.CacheScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;
import uk.co.tfd.sm.api.cluster.ClusterService;
import uk.co.tfd.sm.authn.TrustedCredentials;

@Component(immediate = true, metatype = true)
@Service(value = TokenAuthenticationService.class)
public class CookieTokenAuthenticationServiceImpl implements
		TokenAuthenticationService {

	@Property(value="sm-auth")
	private static final String COOKIE_NAME = "cookie-name";
	
	@Property(intValue=10)
	private static final String KEYS_PER_SERVER = "keys-per-server";
	@Property(longValue=1800000L)
	private static final String KEY_TTL = "key-ttl";
	@Property(intValue=2000)
	private static final String COOKIE_AGE = "cookie-age";
	@Property(boolValue=false)
	private static final String SECURE_TRANSPORT = "secure-transport";

	private static final Logger LOGGER = LoggerFactory.getLogger(CookieTokenAuthenticationServiceImpl.class);

	public class Token {

		private static final String UTF_8 = "UTF-8";
		private String keyId;
		private String user;
		private long expires;
		private String requestHash;
		private String message;
		private String cookieValue;

		public Token(String value) {
			cookieValue = value;
			String decoded;
			try {
				decoded = new String(Base64.decodeBase64(value), UTF_8);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			String[] parts = StringUtils.split(decoded, ";");
			if (parts == null || parts.length != 4) {
				throw new IllegalArgumentException();
			}
			keyId = parts[0];
			user = parts[1];
			expires = Long.parseLong(parts[2]);
			requestHash = parts[3];
			message = user + ";" + expires;
			try {
				if (!requestHash.equals(hash())) {
					throw new IllegalArgumentException("Token is invlaid");
				}
			} catch (InvalidKeyException e) {
				throw new RuntimeException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (IllegalStateException e) {
				throw new RuntimeException(e);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

		}

		public Token(AuthenticationServiceCredentials credentials) {
			keyId = getCurrentKeyId();
			user = credentials.getUserName();
			expires = System.currentTimeMillis() + getTtl();
			message = user + ";" + expires;
			try {
				requestHash = hash();
				cookieValue = Base64
						.encodeBase64URLSafeString(StringUtils.join(
								new Object[] { keyId, user, expires,
										requestHash }, ";").getBytes(UTF_8));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public String getMessage() {
			return message;
		}

		public boolean willExpire() {
			return ((expires - System.currentTimeMillis()) < (getTtl() / 2));
		}

		private String hash() throws NoSuchAlgorithmException,
				InvalidKeyException, IllegalStateException,
				UnsupportedEncodingException {
			Mac m = Mac.getInstance(KeyHolder.HMAC);
			m.init(getKey(keyId));
			m.update(message.getBytes(UTF_8));
			return new String(Base64.encodeBase64URLSafe(m.doFinal()));
		}

		public String getUser() {
			return user;
		}

		@Override
		public String toString() {
			return cookieValue;
		}

	}

	private String cookieName;
	private boolean secure;
	private int cookieAge;
	private long ttl;
	private String serverId;
	private int nKeysPerServer;

	@Reference
	protected CacheManagerService cacheManager;

	@Reference
	protected ClusterService clusterService;
	private Cache<String> keyCache;
	private SecureRandom random = new SecureRandom();
	private Object keyLock = new Object();

	@Activate
	public void activate(Map<String, Object> properites) {
		modified(properites);
	}

	@Modified
	public void modified(Map<String, Object> properties) {
		// this cache should be configured to have an eviction time about 2x the
		// ttl
		keyCache = cacheManager.getCache(this.getClass().getName(),
				CacheScope.CLUSTERREPLICATED);
		secure = (Boolean) properties.get(SECURE_TRANSPORT);
		cookieAge = (Integer) properties.get(COOKIE_AGE);
		ttl = (Long) properties.get(KEY_TTL);
		nKeysPerServer = (Integer) properties.get(KEYS_PER_SERVER);
		cookieName = (String) properties.get(COOKIE_NAME);

		serverId = clusterService.getServerId();

	}

	@Override
	public AuthenticationServiceCredentials getCredentials(
			HttpServletRequest request) {
		Token t = getToken(request);
		if (t != null) {
			return new TrustedCredentials(t.getUser());
		}
		return null;
	}

	public long getTtl() {
		return ttl / 2;
	}

	public String getCurrentKeyId() {
		return serverId + ":"
				+ ((System.currentTimeMillis() / ttl) % nKeysPerServer);
	}

	public Key getKey(String keyId) {
		KeyHolder kh = getKeyHolder(keyId);
		if (kh == null || kh.hasExpired()) {
			synchronized (keyLock) {
				// get it out again just in case it was replaced by the blocker.
				kh = getKeyHolder(keyId);
				if (kh == null || kh.hasExpired()) {
					kh = new KeyHolder();
					byte[] b = new byte[1024];
					random.nextBytes(b);
					try {
						kh.reset(b, ttl);
					} catch (NoSuchAlgorithmException e) {
						throw new RuntimeException(e);
					}
					keyCache.put(keyId, kh.toString());
				}
			}
		}
		return kh.getKey();
	}

	private KeyHolder getKeyHolder(String keyId) {
		String keySpec = keyCache.get(keyId);
		if ( keySpec != null ) {
			return new KeyHolder(keySpec);
		}
		return null;
	}

	@Override
	public AuthenticationServiceCredentials refreshCredentials(
			AuthenticationServiceCredentials credentials,
			HttpServletRequest request, HttpServletResponse response) {
		LOGGER.debug("Refresh Credentials on {} ", credentials);
		if ( credentials != null ) {
			Token t = getToken(request);
			if (t == null || t.willExpire() || !credentials.getUserName().equals(t.getUser())) {
				Token newToken = new Token(credentials);
				if ( t == null ) {
					LOGGER.info("No Cookie ");
				} else if ( t.willExpire() ) {
					LOGGER.info("Cookie Will expire ");					
				} else if (!credentials.getUserName().equals(t.getUser())) {
					LOGGER.info("Change User ");
				}
				LOGGER.info("Setting Token Cookie to user {} ", newToken.getUser());
				Cookie cookie = new Cookie(cookieName, newToken.toString());
				// FIXME: make this httpOnly.
				cookie.setMaxAge(cookieAge);
				cookie.setPath("/");
				cookie.setSecure(secure);
				response.addCookie(cookie);
			}
		}
		return credentials;
	}
	

	private Token getToken(HttpServletRequest request) {
		Cookie[] cs = request.getCookies();
		if (cs != null) {
			for (Cookie c : cs) {
				if (cookieName.equals(c.getName())) {
					try {
						return new Token(c.getValue());
					} catch ( IllegalArgumentException  e) {
						LOGGER.debug(e.getMessage(),e);
					}
				}
			}
		}
		return null;
	}

	

}
