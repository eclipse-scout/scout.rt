package org.eclipse.scout.rt.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.cdi.BeanInvocationHint;
import org.eclipse.scout.rt.platform.cdi.OBJ;

/**
 * An object (typically a service implementation) with this annotation creates a client transaction whenever an
 * operationis called.
 * <p>
 * Only valid if the object is obtained using {@link OBJ#one(Class)} with an interface argument
 */
@BeanInvocationHint
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Client {
  Class<? extends IClientSession> value() default IClientSession.class;
}
