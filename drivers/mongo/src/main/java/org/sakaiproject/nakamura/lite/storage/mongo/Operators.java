package org.sakaiproject.nakamura.lite.storage.mongo;

/**
 * MongoDB query operators.
 * http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries
 * http://www.mongodb.org/display/DOCS/Updating
 */
public class Operators {
	public static final String OR = "$or";
	public static final String SET = "$set";
	public static final String UNSET = "$unset";
	public static final String ALL = "$all";
}
