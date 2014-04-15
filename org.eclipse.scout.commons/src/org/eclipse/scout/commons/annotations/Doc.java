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
   * {@link #ACCEPT} type appears in generated doc<br>
   * {@link #REJECT} type does not appear in generated doc<br>
   * {@link #TRANSPARENT} type does not appear in generated doc, but it's children do<br>
   * {@link #ACCEPT_REJECT_CHILDREN} type appears in generated doc, but it's children do not
   */
  enum Filtering {
    ACCEPT,
    REJECT,
    TRANSPARENT,
    ACCEPT_REJECT_CHILDREN
  }

  /**
   * Definition for the default filtering behavior
   */
  Filtering filter() default Filtering.ACCEPT;

  /**
   * @deprecated Will be removed with scout 5.0. Uses {@link #filter()} instead. (Is already with scout 3.10 not
   *             evaluated anymore!)
   */
  @Deprecated
  boolean ignore() default false;
}
