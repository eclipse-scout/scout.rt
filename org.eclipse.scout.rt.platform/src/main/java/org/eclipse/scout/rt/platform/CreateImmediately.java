package org.eclipse.scout.rt.platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an 'application-scoped' bean should be instantiated immediately upon platform startup. However, the
 * bean must be declared as 'application-scoped', otherwise platform startup fails.
 */
@Bean // do not remove to enforce validation on platform startup.
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CreateImmediately {
}
