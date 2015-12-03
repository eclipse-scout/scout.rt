/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

/**
 * Runtime equivalent of {@link Order} annotation. This interface allows defining an order for instances, so that they
 * can be used in ordered collections.
 * <p/>
 * <b>Note</b>: The {@link Order} annotation is only used to initialize this order property. At runtime always
 * {@link #getOrder()} is used.
 *
 * @since 3.8.1
 * @see Order
 */
public interface IOrdered {

  /**
   * The default order of scout elements.
   * <p>
   * The value of this constant is "rather large, but not <i>that</i> large". For most projects it is the biggest of all
   * orders. But it is not as big that precision is lost during calculations due to the IEEE floating point arithmetic.
   * In particular, subtracting {@link #DEFAULT_ORDER_STEP} from this value must result in a different number (which,
   * for example, would not be the case for {@link Double#MAX_VALUE}). As a rule of thumb, this number should be smaller
   * than {@link Long#MAX_VALUE}.
   *
   * @since 4.2
   */
  double DEFAULT_ORDER = 98765432123456789d;

  /**
   * The default step between two successive elements.
   *
   * @since 4.2
   */
  double DEFAULT_ORDER_STEP = 1000d;

  /**
   * @return Returns the object's order.
   */
  double getOrder();

  /**
   * sets the object's order
   *
   * @param order
   *          the new order value.
   */
  void setOrder(double order);
}
