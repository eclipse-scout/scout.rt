package org.eclipse.scout.commons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes how a type should be documented.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Doc {
  /**
   * @return If <code>true</code> the type is ignored for documentation.
   *         <p>
   *         If a type is ignored and contains other types that are documented, these are ignored as well. E.g. If a
   *         <code>IGroupBox</code> is annotated with <code>@Doc(ignre=true)</code>, all fields within the group box are
   *         ignored as well.
   *         </p>
   */
  boolean ignore() default false;
}
