/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.provider.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a <code>class</code> to be referenced, either by its {@link Class} itself, or by its qualified name if not
 * visible at compile-time.
 *
 * @since 5.1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Clazz {

  /**
   * The {@link Class} to be referenced. By default, this property is not set.
   */
  Class<?> value() default NullClazz.class;

  /**
   * The qualified name of the class to be referenced. By default, this property is empty. If set, the <i>qualified
   * name</i> takes precedence over the <i>class</i> attribute.
   */
  String qualifiedName() default "";

  class NullClazz {
  }
}
