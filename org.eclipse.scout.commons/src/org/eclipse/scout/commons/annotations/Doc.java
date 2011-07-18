package org.eclipse.scout.commons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Doc {
  /**
   * @return The type's priority.
   */
  boolean ignore() default false;
}
