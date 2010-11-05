package org.sakaiproject.nakamura.lite;

import java.util.Dictionary;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.sakaiproject.nakamura.api.lite.Configuration;

@Component(immediate = true, metatype = true)
@Service(value = Configuration.class)
public class ConfigurationImpl implements Configuration {

    @Property(value = "Acl")
    private static final String ACL_COLUMN_FAMILY = "acl-column-family";
    @Property(value = "Nakamura")
    private static final String KEYSPACE = "keyspace";
    @Property(value = "Authorizable")
    private static final String AUTHORIZABLE_COLUMN_FAMILY = "authorizable-column-family";
    private String aclColumnFamily;
    private String keySpace;
    private String authorizableColumnFamily;

    @Activate
    public void activate(ComponentContext componentContext) {
        @SuppressWarnings("unchecked")
        Dictionary<String, Object> properties = componentContext.getProperties();
        activate(properties);
    }

    public void activate(Dictionary<String, Object> properties) {
        aclColumnFamily = (String) properties.get(ACL_COLUMN_FAMILY);
        keySpace = (String) properties.get(KEYSPACE);
        authorizableColumnFamily = (String) properties.get(AUTHORIZABLE_COLUMN_FAMILY);
    }

    public String getAclColumnFamily() {
        return aclColumnFamily;
    }

    public String getKeySpace() {
        return keySpace;
    }

    public String getAuthorizableColumnFamily() {
        return authorizableColumnFamily;
    }

}
