/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.status;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

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
   * Removes all equal status in its direct or indirect children. Using {@link Object#equals(Object)}
   *
   * @param status
   *     the status to remove
   */
  void removeAll(IStatus status);

  /**
   * Remove all children with the given class
   */
  void removeAll(Class<? extends IStatus> clazz);

  /**
   * @param status
   *     not <code>null<code>
   * @return <code>true</code>, if any of the direct or indirect children is equals the the given status,
   * <code>false</code> otherwise.
   */
  boolean containsStatus(IStatus status);

  /**
   * @param clazz
   *     not <code>null<code>
   * @return <code>true</code>, if any of the direct or indirect children is of the given type, <code>false</code>
   * otherwise.
   */
  boolean containsStatus(Class<? extends IStatus> clazz);

  /**
   * @param childPredicate
   *     not <code>null<code>
   * @return all direct or indirect children for the given predicate
   */
  Collection<IStatus> findChildStatuses(Predicate<IStatus> childPredicate);

  /**
   * @return maximum severity of the children or {@link #OK}, if no children available
   */
  @Override
  int getSeverity();

  /**
   * @return the minimum order of the children or {@link IOrdered#DEFAULT_ORDER}, if no children available
   */
  @Override
  double getOrder();
}
