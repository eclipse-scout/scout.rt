package org.eclipse.scout.rt.testing.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.PlatformTestRunner;

/**
 * Annotation to execute test-methods under a particular user. This is used in conjunction with
 * {@link PlatformTestRunner} and its subclasses.
 *
 * @since 5.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RunWithSession {
  Class<? extends ISession> value();

  /**
   * @return true if session caching is allowed. This re-uses the session for the given type and the given user in the
   *         global jvm cache.
   */
  boolean useCache() default true;
}
