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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.MenuWrapper;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMenuWrapper;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMenuWrapper.IMenuTypeMapper;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.visitor.DepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.shared.dimension.IDimensions;

/**
 * Utility class for menus
 *
 * @since 3.10.0-M4
 */
public final class MenuUtility {

  private MenuUtility() {
  }

  /**
   * @return true if the menu is a visible leaf in the menu tree or the menu is a menu group (has child menus) and at
   *         least one of the recursive child menus is a visible leaf.
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

  /**
   * Removes invisible menus. Also removes leading and trailing separators as well as multiple consecutive separators.
   *
   * @since 3.8.1
   */
  public static List<IMenu> visibleNormalizedMenus(List<IMenu> menus) {
    return normalizedMenus(menus, createVisibleFilter());
  }

  public static List<IMenu> normalizedMenus(List<IMenu> menus, final Predicate<IMenu> filter) {
    if (menus == null) {
      return CollectionUtility.emptyArrayList();
    }

    // only visible
    List<IMenu> cleanedMenus = filterMenusRec(menus, filter);

    ActionUtility.normalizeSeparators(cleanedMenus);
    return cleanedMenus;
  }

  /**
   * Filters the given list of menus. If a menu has child menus, the menu is wrapped (see
   * {@link MenuWrapper#wrapMenu(IMenu, IMenuTypeMapper, Predicate)}) and its child menus are filtered as well.
   */
  public static List<IMenu> filterMenusRec(List<IMenu> menus, final Predicate<IMenu> filter) {
    return filterMenus(menus, filter, m -> m.hasChildActions() ? MenuWrapper.wrapMenu(m, OutlineMenuWrapper.AUTO_MENU_TYPE_MAPPER, filter) : m);
  }

  /**
   * Filters the given list of menus.
   */
  public static List<IMenu> filterMenus(List<IMenu> menus, final Predicate<IMenu> filter) {
    return filterMenus(menus, filter, UnaryOperator.identity());
  }

  private static List<IMenu> filterMenus(List<IMenu> menus, final Predicate<IMenu> filter, final UnaryOperator<IMenu> menuMapper) {
    if (menus != null) {
      List<IMenu> result = new ArrayList<>(menus.size());
      for (IMenu m : menus) {
        if (m.isSeparator()) {
          result.add(m);
        }
        else if (filter.test(m)) {
          m = menuMapper.apply(m);
          result.add(m);
        }
      }
      return result;
    }
    return CollectionUtility.emptyArrayList();
  }

  public static Predicate<IMenu> createVisibleFilter() {
    return new Predicate<>() {
      @Override
      public boolean test(IMenu menu) {
        if (menu != null && menu.isVisible()) {
          // remove menu groups with no visible child menu
          if (menu.hasChildActions()) {
            List<?> visibleChildActions = filterMenusRec(menu.getChildActions(), this);
            return !visibleChildActions.isEmpty();
          }
          return true;
        }
        return false;
      }
    };
  }

  /**
   * Updates the enabled state of the given {@link AbstractContextMenu} to the enabled state of its containing
   * widget.<br>
   * If the widget is enabled, the given selectionEnabledStateSupplier is evaluated and all child {@link IMenu menus}
   * (having at least one of the given menu types) are updated according to the value of the supplier.
   *
   * @param contextMenu
   *          The {@link AbstractContextMenu} to update. Must not be {@code null}.
   * @param selectionEnabledStateSupplier
   *          Only invoked if the container of the context menu itself is enabled. Returns if all selected elements of
   *          the container are enabled ({@code true}) or not.
   * @see #updateEnabledStateOfMenus(IWidget, boolean, IMenuType...)
   * @see UpdateMenuEnabledStateVisitor
   */
  public static void updateContextMenuEnabledState(AbstractContextMenu<? extends IWidget> contextMenu, BooleanSupplier selectionEnabledStateSupplier, IMenuType... menuTypes) {
    boolean containerEnabled = contextMenu.getContainer().isEnabled();
    contextMenu.setEnabled(containerEnabled);
    if (containerEnabled) {
      updateEnabledStateOfMenus(contextMenu, selectionEnabledStateSupplier.getAsBoolean(), menuTypes);
    }
  }

  /**
   * Recursively updates the {@link IDimensions#ENABLED_SLAVE} for all {@link IMenu} instances (having at least one of
   * the given menu types) of the given {@link IWidget}.
   *
   * @param widget
   *          The {@link IWidget} whose {@link IMenu menus} should be changed (recursively). Must not be {@code null}.
   * @param enabled
   *          The new enabled state of the {@link IMenu menus} found.
   * @param menuTypes
   *          The menu types to update
   */
  public static void updateEnabledStateOfMenus(IWidget widget, boolean enabled, IMenuType... menuTypes) {
    Predicate<IMenu> menusToUpdate = createMenuFilterMenuTypes(false, menuTypes);
    widget.visit(new UpdateMenuEnabledStateVisitor<>(enabled, menusToUpdate), IMenu.class);
  }

  /**
   * @see #createMenuFilterMenuTypes(Set, boolean)
   */
  public static Predicate<IMenu> createMenuFilterMenuTypes(boolean visibleOnly, IMenuType... menuTypes) {
    return createMenuFilterMenuTypes(CollectionUtility.hashSet(menuTypes), visibleOnly);
  }

  /**
   * <ul>
   * <li>If the menu is a leaf (menu without any child menus) the filter accepts a menu when it is visible (depending on
   * visibleOnly) and it has one of the passed menu types.</li>
   * <li>If the menu has child menus (is a menu group) then the filter accepts a menu if it is visible (depending on
   * visibleOnly) and at least one of its leaf children (recursively) is visible (depending on visible only) and and has
   * one of the passed menu types.</li>
   * </ul>
   */
  public static Predicate<IMenu> createMenuFilterMenuTypes(Set<? extends IMenuType> menuTypes, boolean visibleOnly) {
    return new MenuTypeFilter(menuTypes, visibleOnly);
  }

  public static class MenuTypeFilter implements Predicate<IMenu> {

    private final boolean m_visibleOnly;
    private final Set<? extends IMenuType> m_menuTypes;

    public MenuTypeFilter(Set<? extends IMenuType> menuTypes, boolean visibleOnly) {
      m_menuTypes = menuTypes;
      m_visibleOnly = visibleOnly;
    }

    @Override
    public boolean test(IMenu menu) {
      if (menu == null) {
        return false;
      }
      if (isVisibleOnly() && !menu.isVisible()) {
        return false;
      }
      else {
        if (menu.hasChildActions()) {
          // check for filter matching child menus
          return !normalizedMenus(menu.getChildActions(), this).isEmpty();
        }
        if (getMenuTypes() != null) {
          for (IMenuType t : getMenuTypes()) {
            if (menu.getMenuTypes().contains(t)) {
              return true;
            }
          }
          return false;
        }
        else {
          return true;
        }
      }
    }

    public Set<? extends IMenuType> getMenuTypes() {
      return m_menuTypes;
    }

    public boolean isVisibleOnly() {
      return m_visibleOnly;
    }
  }

  /**
   * Updates the {@link IDimensions#ENABLED_SLAVE} state to the given value for all {@link IMenu} instances accepting
   * the given {@link Predicate}.<br>
   * If a menu contains children it is disabled if all the child menus are disabled (according to
   * {@link IDimensions#ENABLED_SLAVE}) as well.<br>
   * Separators (see {@link IAction#isSeparator()}) are always ignored.<br>
   * {@link IMenu} instances not matching the given filter are not touched.
   */
  public static class UpdateMenuEnabledStateVisitor<T extends IMenu> extends DepthFirstTreeVisitor<T> {

    private final boolean m_enabled;
    private final Predicate<? super T> m_filter;

    public UpdateMenuEnabledStateVisitor(boolean enabled, Predicate<? super T> filter) {
      m_enabled = enabled;
      m_filter = filter;
    }

    @Override
    public TreeVisitResult preVisit(T element, int level, int index) {
      if (isSkipElement(element)) {
        return TreeVisitResult.SKIP_SUBTREE;
      }
      return TreeVisitResult.CONTINUE;
    }

    protected boolean isSkipElement(T element) {
      return element.isSeparator() || !element.isInheritAccessibility();
    }

    @Override
    public boolean postVisit(T element, int level, int index) {
      if (isSkipElement(element)) {
        return true;
      }
      if (!element.hasChildActions() && m_filter.test(element)) {
        element.setEnabled(m_enabled, IDimensions.ENABLED_SLAVE);
      }
      return true;
    }
  }
}
