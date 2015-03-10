package org.eclipse.scout.testing.client.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.testing.platform.PlatformTestRunner;

/**
 * Annotation to execute test-methods under a particular user. This is used in conjunction with
 * {@link PlatformTestRunner} and its subclasses.
 *
 * @since 5.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RunWithClientSession {
  Class<? extends IClientSession> value();

  boolean useCache() default true;
}
