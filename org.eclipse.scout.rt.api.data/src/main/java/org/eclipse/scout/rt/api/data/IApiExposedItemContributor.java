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

import java.util.Set;

/**
 * Root interface to contribute Scout elements that are exposed by the built-in Scout REST api.
 */
@FunctionalInterface
public interface IApiExposedItemContributor<T> {
  /**
   * Callback to contribute to the items that should be exposed.
   *
   * @param itemsToExpose
   *     Live {@link Set} of elements that should be exposed to the UI.
   */
  void contribute(Set<T> itemsToExpose);
}
