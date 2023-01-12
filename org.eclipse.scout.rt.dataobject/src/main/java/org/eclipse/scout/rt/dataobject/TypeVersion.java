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
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to define the version of an data object entity class used when serializing and deserializing an
 * instance of the annotated class.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;TypeName("scout.ExampleEntity")
 * &#64;TypeVersion(Scout_8_0_0.class)
 * public class ExampleEntityDo extends DoEntity {
 *   ...
 * }
 * </pre>
 *
 * @see DoEntity
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface TypeVersion {

  /**
   * Version used when serializing an instance of the annotated class
   */
  Class<? extends ITypeVersion> value();
}
