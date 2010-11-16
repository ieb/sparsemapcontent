package org.sakaiproject.nakamura.api.lite.accesscontrol;

import org.sakaiproject.nakamura.api.lite.authorizable.User;

public interface Authenticator {

    User authenticate(String userid, String password);

}
