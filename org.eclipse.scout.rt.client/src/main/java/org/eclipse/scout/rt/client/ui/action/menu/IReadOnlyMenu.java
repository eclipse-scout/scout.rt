/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu;

/**
 * Represents a read-only menu that wraps an existing normal menu.
 * <p>
 * All write operations on such a menu have either no effect or throw {@link UnsupportedOperationException}s.
 */
public interface IReadOnlyMenu extends IMenu {

  /**
   * @return The wrapped original menu.
   */
  IMenu getWrappedMenu();
}
