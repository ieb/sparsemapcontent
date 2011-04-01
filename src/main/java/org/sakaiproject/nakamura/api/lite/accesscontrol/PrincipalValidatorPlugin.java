package org.sakaiproject.nakamura.api.lite.accesscontrol;

import org.sakaiproject.nakamura.api.lite.content.Content;

/**
 * Validates a principal Token.
 */
public interface PrincipalValidatorPlugin {

    /**
     * Validate the token to see if its current. This should not need to consider
     * the user since if the user is relevant they will have access to the token,
     * if not, the token would not have been resolved for the user.
     *
     * @param proxyPrincipalToken
     * @return true if the principal is valid, and the user who resolved it can
     *         have the principal.
     */
    boolean validate(Content proxyPrincipalToken);

}
