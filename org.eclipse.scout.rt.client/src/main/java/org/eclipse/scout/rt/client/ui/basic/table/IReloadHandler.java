/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IReloadReason;

/**
 * Interface for classes supporting a reload function.
 *
 * @since 5.1
 */
@FunctionalInterface
public interface IReloadHandler {

  /**
   * @param reloadReason
   *     {@link IReloadReason}
   */
  void reload(String reloadReason);
}
