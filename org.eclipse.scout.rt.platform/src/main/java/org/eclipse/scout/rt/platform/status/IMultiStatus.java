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
package org.eclipse.scout.rt.platform.status;

import java.util.List;

import org.eclipse.scout.rt.platform.IOrdered;

/**
 * A multi-status may have other status objects as children.
 */
public interface IMultiStatus extends IStatus {

  /**
   * Returns a list of status objects immediately contained in this multi-status, or an empty list if there are no
   * children available.
   * <p>
   * The list of children is sorted from highest to lowest severity ({@link IStatus#ERROR} to {@link IStatus#OK}), then
   * by lowest to highest order (see {@link IOrdered}), then by message.
   * </p>
   * *
   * <p>
   * The severity of a multi-status is defined to be the maximum severity of any of its children, or <code>OK</code> if
   * it has no children.
   * </p>
   *
   * @return SortedSet children
   * @see #isMultiStatus()
   */
  List<IStatus> getChildren();

  /**
   * Remove all children with the given class
   */
  void removeAll(Class<? extends IStatus> clazz);

  /**
   * @param clazz
   *          not <code>null<code>
   * @return <code>true</code>, if any of the direct or indirect children is of the given type, <code>false</code>
   *         otherwise.
   */
  boolean containsStatus(Class<? extends IStatus> clazz);

  /**
   * @return maximum severity of the children or {@link #DEFAULT_SEVERITY}, if no children available
   */
  @Override
  int getSeverity();

  /**
   * @return the minimum order of the children or {@link IOrdered#DEFAULT_ORDER}, if no children available
   */
  @Override
  double getOrder();

}
