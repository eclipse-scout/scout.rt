/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.reflect.FastBeanInfo;

/**
 * This annotation is used to mark bean properties to be ignored in certain translations. This annotation must be placed
 * on the getter method of the bean property.
 * <p>
 * Note that this annotation is <b>not</b> inherited, i.e. it has no effect when used in interfaces. A subclass
 * overriding a getter method bearing this annotation must add the annotation again on the redeclared method if it wants
 * to still ignore the property.
 * <p>
 * Classes like {@link FastBeanInfo} do not have any knowledge about this annotation. The user of {@link FastBeanInfo}
 * and similar must check the annotation on the reader (getter) method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IgnoreProperty {

  enum Context {
    GUI
  }

  Context value() default Context.GUI;
}
