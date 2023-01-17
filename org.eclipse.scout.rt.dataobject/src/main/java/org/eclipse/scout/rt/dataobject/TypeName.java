/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to define the unique type name for a data object entity class, used when serializing and
 * deserializing an instance of the annotated class.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;TypeName("ExampleEntity")
 * &#64;TypeVersion("scout-8.0.0")
 * public class ExampleEntityDo extends DoEntity {
 *   ...
 * }
 * </pre>
 *
 * @see DoEntity
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
@Inherited
public @interface TypeName {

  /**
   * Unique type name used when serializing an instance of the annotated class. Do not use an empty string as value.
   */
  String value();
}
