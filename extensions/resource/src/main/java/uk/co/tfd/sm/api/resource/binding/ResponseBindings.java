package uk.co.tfd.sm.api.resource.binding;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Container for a list of response bindings.
 * @author ieb
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ResponseBindings {
	/**
	 * @return list of response bindings to be applied to the output of the ResponseFactory
	 */
	ResponseBinding[] value();
}
