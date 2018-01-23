package org.eclipse.scout.rt.platform.dataobject;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Date;

/**
 * Annotation used to define a custom format pattern for serialization and deserialization the annotated member of a
 * {@link DoEntity} data object.
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD})
public @interface ValueFormat {

  /**
   * Custom format pattern used by specific serializer/deserializer while converting value from/to string.
   * <p>
   * For example a {@link Date} value could specify its custom serialization format pattern:
   *
   * <pre>
   * &#64;ValueFormat(pattern = IValueFormatConstants.TIMESTAMP_PATTERN)
   * public DoValue<BigDecimal> dateAttribute() {
   *   return doValue("dateAttribute");
   * }
   * </pre>
   *
   * @see {@link IValueFormatConstants} for a set of constant definitions
   */
  String pattern();
}
