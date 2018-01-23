package org.eclipse.scout.rt.platform.dataobject;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to define the unique type name for a class, used when serializing and deserializing an instance of
 * the annotated class.
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
@Inherited
public @interface TypeName {

  /**
   * Unique type name used when serializing an instance of the annotated class
   * <p>
   * If value is left empty then the type name is derived from the simple class name.
   */
  String value() default "";
}
