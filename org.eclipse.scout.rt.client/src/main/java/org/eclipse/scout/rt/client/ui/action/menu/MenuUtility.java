/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;

/**
 * Utility class for menus
 *
 * @since 3.10.0-M4
 */
public final class MenuUtility {

  private MenuUtility() {
  }

  /**
   * @param menu
   * @return true if the menu is a visible leaf in the menu tree or the menu is a menu group (has child menus) and at
   *         least one of the recursive child menus is a visisble leaf.
   */
  public static <T extends IActionNode<?>> boolean isVisible(T menu) {
    if (!menu.isVisible()) {
      return false;
    }
    if (menu.hasChildActions()) {
      boolean visible = false;
      for (Object o : menu.getChildActions()) {
        if (o instanceof IActionNode<?>) {
          IActionNode<?> m = (IActionNode<?>) o;
          if (!m.isSeparator() && m.isVisible()) {
            visible = true;
            break;
          }
        }
      }
      return visible;
    }
    return true;
  }

  /**
   * @return the sub-menu of the given context menu owner that implements the given type. If no implementation is found,
   *         <code>null</code> is returned. Note: This method uses instance-of checks, hence the menu replacement
   *         mapping is not required.
   * @throws IllegalArgumentException
   *           when no context menu owner is provided.
   */
  public static <T extends IMenu> T getMenuByClass(IContextMenuOwner contextMenuOwner, final Class<T> menuType) {
    if (contextMenuOwner == null) {
      throw new IllegalArgumentException("Argument 'contextMenuOwner' must not be null");
    }

    List<IMenu> rootMenus;
    IContextMenu root = contextMenuOwner.getContextMenu();
    if (root == null) {
      // some components have no root menu but directly contain child menus (e.g. Desktop)
      rootMenus = contextMenuOwner.getMenus();
    }
    else {
      rootMenus = Collections.singletonList(root);
    }
    return new ActionFinder().findAction(rootMenus, menuType);
  }

}
