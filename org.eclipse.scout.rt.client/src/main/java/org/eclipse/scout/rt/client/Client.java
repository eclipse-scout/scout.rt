package org.eclipse.scout.rt.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.cdi.BeanInvocationHint;
import org.eclipse.scout.rt.platform.cdi.OBJ;

/**
 * An object (typically a service implementation) with this annotation creates a client transaction whenever an
 * operationis called.
 * <p>
 * Only valid if the object is obtained using {@link OBJ#get(Class)} with an interface argument
 */
@Bean
@BeanInvocationHint
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Inherited
public @interface Client {
  Class<? extends IClientSession> value() default IClientSession.class;
}
