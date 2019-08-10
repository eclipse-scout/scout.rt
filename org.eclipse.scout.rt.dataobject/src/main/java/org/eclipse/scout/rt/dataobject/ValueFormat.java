/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

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
