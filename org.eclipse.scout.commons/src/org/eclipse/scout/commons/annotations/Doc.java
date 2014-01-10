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

  enum Filtering {
    ACCEPT, REJECT, TRANSPARENT, ACCEPT_REJECT_CHILDREN
  }

  // TODO ASA javadoc
  Filtering filter() default Filtering.ACCEPT;

  /**
   * @deprecated Will be removed with scout 3.11. Uses {@link #filter()} instead. (Is already with scout 3.10 not
   *             evaluated anymore!)
   */
  @Deprecated
  boolean ignore() default false;
}
