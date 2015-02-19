package javax.interceptor;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that an annotation type is an interceptor binding.
 */

@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
public @interface InterceptorBinding {
}
