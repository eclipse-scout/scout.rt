/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.Collection;
import java.util.function.Predicate;

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
   *
   * @param menu
   *     Menu to wrap
   * @return Newly wrapped menu if the the menu is not already wrapped, same menu otherwise.
   */
  public static IMenu wrapMenuIfNotWrapped(IMenu menu) {
    return BEANS.get(MenuWrapper.class).doWrapMenuIfNotWrapped(menu, OutlineMenuWrapper.AUTO_MENU_TYPE_MAPPER, OutlineMenuWrapper.ACCEPT_ALL_FILTER);
  }

  /**
   * Returns a wrapper for the given menu. If the menu is already a wrapper instance, the wrapped menu will be wrapped
   * again.
   *
   * @param menu
   *     Menu to wrap
   * @param menuTypeMapper
   *     function to map one menu type to another. The mapper is applied to child menus too.
   * @return Wrapped menu
   */
  public static IMenu wrapMenu(IMenu menu, IMenuTypeMapper menuTypeMapper) {
    return wrapMenu(menu, menuTypeMapper, OutlineMenuWrapper.ACCEPT_ALL_FILTER);
  }

  /**
   * Returns a wrapper for the given menu. If the menu is already a wrapper instance, the wrapped menu will be wrapped
   * again.
   *
   * @param menu
   *     Menu to wrap
   * @param menuTypeMapper
   *     function to map one menu type to another. The mapper is applied to child menus too.
   * @param menuFilter
   *     Filter used when wrapping child menus.
   * @return Wrapped menu
   */
  public static IMenu wrapMenu(IMenu menu, IMenuTypeMapper menuTypeMapper, Predicate<IMenu> menuFilter) {
    return BEANS.get(MenuWrapper.class).doWrapMenu(menu, menuTypeMapper, menuFilter);
  }

  protected IReadOnlyMenu doWrapMenuIfNotWrapped(IMenu menu, IMenuTypeMapper menuTypeMapper, Predicate<IMenu> menuFilter) {
    if (menu instanceof IReadOnlyMenu) {
      return (IReadOnlyMenu) menu; // already wrapped - don't wrap again
    }
    return doWrapMenu(menu, menuTypeMapper, menuFilter);
  }

  protected IReadOnlyMenu doWrapMenu(IMenu menu, IMenuTypeMapper menuTypeMapper, Predicate<IMenu> menuFilter) {
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
      return menus.contains(wrapper.getWrappedMenu()) || containsWrappedMenu(menus, wrapper.getWrappedMenu());
    }
    return false;
  }
}
