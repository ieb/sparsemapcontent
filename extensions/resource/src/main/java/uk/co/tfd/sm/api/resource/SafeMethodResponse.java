package uk.co.tfd.sm.api.resource;

import java.util.Set;

import com.google.common.collect.ImmutableSet;


/**
 * Indicates the adapatable does not make modifications.
 * @author ieb
 *
 */
public interface SafeMethodResponse extends Adaptable {

	public static Set<String> COMPATABLE_METHODS = ImmutableSet.of("GET", "HEAD");
}
