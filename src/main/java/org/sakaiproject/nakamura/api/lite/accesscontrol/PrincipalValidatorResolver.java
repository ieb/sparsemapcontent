package org.sakaiproject.nakamura.api.lite.accesscontrol;

/**
 * Resolves a Key to a PrincipalValidatorPlugin, and provides a location for
 * Plugins to register. Plugins should depend on this service so they can
 * register.
 */
public interface PrincipalValidatorResolver {

    /**
     * @param key
     *            the name of the plugin
     * @return the plugin, or null if not found.
     */
    PrincipalValidatorPlugin getPluginByName(String key);

    /**
     * Register a plugin.
     *
     * @param key
     * @param plugin
     */
    void registerPlugin(String key, PrincipalValidatorPlugin plugin);

    /**
     * De-register a plugin.
     *
     * @param key
     */
    void unregisterPlugin(String key);

}
