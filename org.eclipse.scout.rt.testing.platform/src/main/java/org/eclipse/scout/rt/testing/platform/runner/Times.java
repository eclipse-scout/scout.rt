package org.eclipse.scout.rt.testing.platform.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to execute a test multiple times. This annotation can be declared on method or class level. For each test
 * run, the respective {@code Before} and {@code After} methods are invoked accordingly.
 *
 * @since 5.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface Times {

  /**
   * @return number of times the test is to be executed.
   */
  long value();
}
