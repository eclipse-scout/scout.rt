/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.Collection;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IReadOnlyMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMenuWrapper.IMenuTypeMapper;
import org.eclipse.scout.rt.client.ui.form.IFormMenu;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

@ApplicationScoped
public class MenuWrapper {

  /**
   * Returns a wrapper for the given menu, or if the menu is already a wrapper instance, the same menu-instance.
   */
  public static IMenu wrapMenu(IMenu menu) {
    return wrapMenu(menu, OutlineMenuWrapper.AUTO_MENU_TYPE_MAPPER, OutlineMenuWrapper.ACCEPT_ALL_FILTER);
  }

  /**
   * Returns a wrapper for the given menu, or if the menu is already a wrapper instance, the same menu-instance.
   */
  public static IMenu wrapMenu(IMenu menu, IMenuTypeMapper menuTypeMapper) {
    return wrapMenu(menu, menuTypeMapper, OutlineMenuWrapper.ACCEPT_ALL_FILTER);
  }

  /**
   * Returns a wrapper for the given menu, or if the menu is already a wrapper instance, the same menu-instance.
   */
  public static IMenu wrapMenu(IMenu menu, IMenuTypeMapper menuTypeMapper, Predicate<IAction> menuFilter) {
    return BEANS.get(MenuWrapper.class).doWrapMenu(menu, menuTypeMapper, menuFilter);
  }

  protected IReadOnlyMenu doWrapMenu(IMenu menu, IMenuTypeMapper menuTypeMapper, Predicate<IAction> menuFilter) {
    if (menu instanceof IReadOnlyMenu) {
      return (IReadOnlyMenu) menu; // already wrapped - don't wrap again
    }
    if (menu instanceof IFormMenu<?>) {
      return new OutlineFormMenuWrapper((IFormMenu<?>) menu, menuTypeMapper, menuFilter);
    }
    return new OutlineMenuWrapper(menu, menuTypeMapper, menuFilter);
  }

  /**
   * Returns the wrapped menu if the given menu is a wrapper instance or the same menu instance otherwise.
   */
  public static IMenu unwrapMenu(IMenu menu) {
    if (menu instanceof IReadOnlyMenu) {
      return unwrapMenu(((IReadOnlyMenu) menu).getWrappedMenu());
    }
    return menu;
  }

  /**
   * Returns true if the given menu is a wrapper menu and the wrapped menu is contained in the given collection of
   * menus.
   */
  public static boolean containsWrappedMenu(Collection<IMenu> menus, IMenu menu) {
    if (menu instanceof IReadOnlyMenu) {
      IReadOnlyMenu wrapper = (IReadOnlyMenu) menu;
      return menus.contains(wrapper.getWrappedMenu());
    }
    return false;
  }
}
