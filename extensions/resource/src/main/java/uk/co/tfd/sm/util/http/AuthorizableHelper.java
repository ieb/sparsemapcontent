package uk.co.tfd.sm.util.http;

import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class AuthorizableHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizableHelper.class);
	private Map<String, Authorizable> toSave;
	private AuthorizableManager authorizableManager;
	private boolean debug;
	
	public AuthorizableHelper(AuthorizableManager authorizableManager) {
		this.authorizableManager = authorizableManager;
		toSave = Maps.newHashMap();
		this.debug = LOGGER.isDebugEnabled();
	}

	public void applyProperties(Authorizable authorizable, ModificationRequest modificationRequest) {
		for (Entry<String, Object> e : modificationRequest.getParameterSet(ParameterType.REMOVE).entrySet()) {
			authorizable.removeProperty(e.getKey());
		}

		for (Entry<String, Object> e : modificationRequest.getParameterSet(ParameterType.ADD).entrySet()) {
			authorizable.setProperty(e.getKey(), e.getValue());
		}
		modificationRequest.resetProperties();
		
		
		toSave.put(authorizable.getId(), authorizable);
	}
	
	public Authorizable getOrCreateAuthorizable(String authorizableId, String authorizableType)
			throws StorageClientException, AccessDeniedException {
		Authorizable authorizable = toSave.get(authorizableId);
		if (authorizable == null) {
			authorizable = authorizableManager.findAuthorizable(authorizableId);
			if (authorizable == null) {
				if (debug) {
					LOGGER.debug("Created A New Unsaved Authorizable object {} ",
							authorizableId);
				}
				if ("user".equals(authorizableType)) {
					if (!authorizableManager.createUser(authorizableId, authorizableId, null, null) ) {
						throw new IllegalArgumentException("Unable to create user "+authorizableId);
					}
				} else {
					if (!authorizableManager.createGroup(authorizableId, authorizableId, null) ) {
						throw new IllegalArgumentException("Unable to create user "+authorizableId);
					}
				}
				authorizable = authorizableManager.findAuthorizable(authorizableId);
			} else if (debug) {
				LOGGER.debug("Content Existed at {} ", authorizable);
			}
			toSave.put(authorizableId, authorizable);
		}
		return authorizable;
	}
	
	public void save() throws AccessDeniedException, StorageClientException {
		for (Authorizable a : toSave.values()) {
			authorizableManager.updateAuthorizable(a);
			if (debug) {
				LOGGER.debug("Updated {} ", a);
			}
		}
	}

}
