package uk.co.tfd.sm.api.resource;

/**
 * Represents a resource.
 * @author ieb
 *
 */
public interface Resource extends Adaptable {

	String getResolvedPath();

	String getRequestPath();

	String getPathInfo();

	String[] getRequestSelectors();

	String getRequestExt();

	String getRequestName();

	String getResourceType();

	String getToCreatePath();

}
