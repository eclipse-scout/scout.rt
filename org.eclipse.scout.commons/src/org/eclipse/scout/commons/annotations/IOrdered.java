/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.annotations;

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
   *
   * @since 4.2
   */
  double DEFAULT_ORDER = Double.MAX_VALUE;

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
