/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to declare a field name for a Scout element. May be used if the element should be dynamically written to a
 * field of a container object.
 */
@Inherited
@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface FieldName {
  /**
   * @return The name of the field.
   */
  String value();
}
