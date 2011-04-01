package org.sakaiproject.nakamura.lite.accesscontrol;

import com.google.common.collect.Maps;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorPlugin;
import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorResolver;

import java.util.Map;

@Component(immediate=true, metatype=true, enabled=true)
@Service(value=PrincipalValidatorResolver.class)
public class PrincipalValidatorResolverImpl implements PrincipalValidatorResolver {

    protected Map<String, PrincipalValidatorPlugin> pluginStore = Maps.newConcurrentHashMap();

    public PrincipalValidatorPlugin getPluginByName(String key) {
        return pluginStore.get(key);
    }

    public void registerPlugin(String key, PrincipalValidatorPlugin plugin) {
        pluginStore.put(key, plugin);
    }
    public void unregisterPlugin(String key) {
        pluginStore.remove(key);
    }

}
