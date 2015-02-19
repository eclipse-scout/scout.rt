package javax.interceptor;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines an interceptor method. The method must have the signature:
 * public Object <METHOD>(InvocationContext) throws Exception
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 44679 $
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface AroundInvoke {
}
