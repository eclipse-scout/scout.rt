package org.eclipse.scout.rt.platform.cdi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * All annotations marked with this annotation are typically interpreted in the {@link IBeanInstanceFactory}
 * <p>
 * These annotations specify requirements about the execution context of the annotated bean or service.
 * <p>
 * This is similar to interceptors.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Documented
public @interface BeanInvocationHint {
}
