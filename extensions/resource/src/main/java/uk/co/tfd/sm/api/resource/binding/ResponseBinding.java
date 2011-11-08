package uk.co.tfd.sm.api.resource.binding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Inherited;

import uk.co.tfd.sm.resource.BindingSearchKey;


/**
 * Annotation to define how a Response is bound.
 * @author ieb
 *
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseBinding {
	
	String ANY = BindingSearchKey.ANY;
	
	/**
	 * @return array of methods that the binding is bound to, if missing bound to ANY methods
	 */
	String[] method();
	/**
	 * @return array of types that the binding is bound to, if missing, ANY.
	 */
	String[] type();
	/**
	 * @return the selectors, if missing, none, any one of the selectors will match.
	 */
	String[] selectors();
	/**
	 * @return the extensions, one must match, if missing none.
	 */
	String[] extension();
}
