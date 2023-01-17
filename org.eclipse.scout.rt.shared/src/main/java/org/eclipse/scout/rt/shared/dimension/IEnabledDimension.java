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
 * Interface for components having the capability to store multiple enabled-state dimensions. All dimensions must be
 * <code>true</code> so that the component is considered to be enabled.
 *
 * @since 6.1
 */
public interface IEnabledDimension {
  /**
   * Changes the enabled-state of the given dimension to the given value.
   *
   * @param enabled
   *          The new value of the given dimension.
   * @param dimension
   *          The name of the dimension to change. Must not be <code>null</code>.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. A component supports up to {@link NamedBitMaskHelper#NUM_BITS}
   *           dimensions. Some dimensions may already be used by the component itself. Therefore the number dimensions
   *           available to developers are component dependent. See the component implementation for details.
   */
  void setEnabled(boolean enabled, String dimension);

  /**
   * Checks if the given dimension is enabled or not.
   *
   * @param dimension
   *          The dimension to check. Must not be <code>null</code>.
   * @return <code>true</code> if the given dimension is enabled. <code>false</code> otherwise. By default all
   *         dimensions are enabled.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. A component supports up to {@link NamedBitMaskHelper#NUM_BITS}
   *           dimensions. Some dimensions may already be used by the component itself. Therefore the number dimensions
   *           available to developers are component dependent.
   */
  boolean isEnabled(String dimension);
}
