package org.eclipse.scout.rt.rest.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.text.TEXTS;

/**
 * API documentation for REST resources and methods.
 * <p>
 * If this annotation is present, the specified string is emitted by {@link ApiDocGenerator}. The string may be a static
 * {@link #text()} or a NLS {@link #textKey()}. If both values are set, {@link #text()} takes precedence.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDocDescription {

  /**
   * Static text. The default is empty.
   */
  String text() default "";

  /**
   * NLS text resolved via {@link TEXTS#get(String)}. This is only emitted if {@link #text()} is empty.
   */
  String textKey() default "";

  /**
   * Set this to <code>true</code> if the text specified contains raw HTML code. The default is <code>false</code>.
   * <b>Use with caution!</b>
   */
  boolean htmlEnabled() default false;
}
