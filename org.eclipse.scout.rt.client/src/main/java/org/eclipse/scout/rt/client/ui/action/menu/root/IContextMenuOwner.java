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
