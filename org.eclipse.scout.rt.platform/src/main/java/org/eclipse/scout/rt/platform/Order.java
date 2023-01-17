/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assigns an absolute order to a type or method. This annotation can be used to bring elements into an order.<br>
 * The value is interpreted in ascending order meaning a low order value is equivalent with a low position in a list
 * (come first).<br>
 * The Annotation can be applied to several scout objects types like columns, menus, form fields and beans.<br>
 * <br>
 * Most of these objects implement {@link IOrdered} where the initial value of {@link IOrdered#getOrder()} is the value
 * of this annotation. For all those objects the default order (if no annotation is present) is defined as
 * {@link IOrdered#DEFAULT_ORDER}.<br>
 * <br>
 * For all other objects (e.g. Scout beans) the order describes which beans win against others in the Scout bean manager
 * or in which order they are returned when querying multiple beans. See the scout bean manager for more details. On a
 * bean the default order is <code>5000</code>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Order {
  /**
   * @return The type's absolute order.
   */
  double value();
}
