package org.eclipse.scout.rt.testing.platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.security.SimplePrincipal;

/**
 * Annotation to execute test-methods under a particular user. This is used in conjunction with
 * {@link PlatformTestRunner} and its subclasses.
 * <p>
 * It runs the tests inside {@link Subject#doAs(Subject, java.security.PrivilegedAction)} with a {@link SimplePrincipal}
 *
 * @since 5.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RunWithSubject {
  String value();
}
