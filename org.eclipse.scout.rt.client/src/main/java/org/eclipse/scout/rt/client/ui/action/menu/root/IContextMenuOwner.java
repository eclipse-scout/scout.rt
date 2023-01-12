/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu.root;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

public interface IContextMenuOwner {

  /**
   * @return the child list of {@link #getContextMenu()}
   */
  List<IMenu> getMenus();

  /**
   * @return the sub-menu of {@link #getContextMenu()} that implements the given type
   */
  <T extends IMenu> T getMenuByClass(Class<T> menuType);

  /**
   * @return the invisible root menu container of all menus
   */
  IContextMenu getContextMenu();
}
