package org.sakaiproject.nakamura.lite.accesscontrol;

import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorPlugin;
import org.sakaiproject.nakamura.api.lite.content.Content;

public class DefaultPrincipalValidator implements PrincipalValidatorPlugin {

    public boolean validate(Content proxyPrincipalToken) {
        // TODO add some standard validation steps like date
        return true;
    }

    public String[] getProtectedFields() {
        return new String[0];
    }

}
