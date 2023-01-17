/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.dimension;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;

/**
 * Interface for components having the capability to store multiple visibility dimensions. All dimensions must be
 * <code>true</code> so that the component is considered to be visible.
 *
 * @since 6.1
 */
public interface IVisibleDimension {

  /**
   * Changes the visibility of the given dimension to the given value.
   *
   * @param visible
   *          The new value of the given dimension.
   * @param dimension
   *          The name of the dimension to change. Must not be <code>null</code>.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. A component supports up to {@link NamedBitMaskHelper#NUM_BITS}
   *           dimensions. Some dimensions may already be used by the component itself. Therefore the number of
   *           dimensions available to developers are component dependent. See the component implementation for details.
   */
  void setVisible(boolean visible, String dimension);

  /**
   * Checks if the given dimension is visible or not.
   *
   * @param dimension
   *          The dimension to check. Must not be <code>null</code>.
   * @return <code>true</code> if the given dimension is visible. <code>false</code> otherwise. By default all
   *         dimensions are visible.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. A component supports up to {@link NamedBitMaskHelper#NUM_BITS}
   *           dimensions. Some dimensions may already be used by the component itself. Therefore the number of
   *           dimensions available to developers are component dependent. See the component implementation for details.
   */
  boolean isVisible(String dimension);
}
