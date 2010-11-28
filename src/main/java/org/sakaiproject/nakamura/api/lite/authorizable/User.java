package org.sakaiproject.nakamura.api.lite.authorizable;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

public class User extends Authorizable {

    public static final String ADMIN_USER = "admin";
    public static final String ANON_USER = "anonymous";
    public static final String SYSTEM_USER = "system";
    public static final String IMPERSONATORS_FIELD = "impersonators";

    public User(Map<String, Object> userMap) {
        super(userMap);
    }

    public boolean isAdmin() {
        return SYSTEM_USER.equals(id) || ADMIN_USER.equals(id)
                || principals.contains(ADMINISTRATORS_GROUP);
    }

    public boolean allowImpersonate(Subject impersSubject) {

        String impersonators = StorageClientUtils.toString(getProperty(IMPERSONATORS_FIELD));
        if (impersonators == null) {
            return false;
        }
        Set<String> impersonatorSet = ImmutableSet.of(StringUtils.split(impersonators, ';'));
        for (Principal p : impersSubject.getPrincipals()) {

            if (ADMIN_USER.equals(p.getName()) || SYSTEM_USER.equals(p.getName())
                    || impersonatorSet.contains(p.getName())) {
                return true;
            }
        }
        return false;
    }

}
