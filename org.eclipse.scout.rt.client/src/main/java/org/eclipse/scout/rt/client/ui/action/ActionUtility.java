/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.visitor.DepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.shared.dimension.IDimensions;

public final class ActionUtility {

  private ActionUtility() {
  }

  public static final Predicate<IAction> FALSE_FILTER = action -> false;

  public static final Predicate<IAction> TRUE_FILTER = action -> true;

  /**
   * Removes invisible actions. Also removes leading and trailing separators as well as multiple consecutive separators.
   *
   * @since 3.8.1
   */
  public static <T extends IAction> List<T> visibleNormalizedActions(List<T> actionNodes) {
    return normalizedActions(actionNodes, createVisibleFilter());
  }

  public static <T extends IAction> List<T> normalizedActions(List<T> actionNodes, Predicate<IAction> filter) {
    if (actionNodes == null) {
      return CollectionUtility.emptyArrayList();
    }

    // only visible
    List<T> cleanedActions = getActions(actionNodes, filter);

    normalizeSeparators(cleanedActions);
    return cleanedActions;
  }

  public static <T extends IAction> void normalizeSeparators(List<T> actions) {
    // remove multiple and leading separators
    T prevAction = null;
    ListIterator<T> it = actions.listIterator();
    while (it.hasNext()) {
      T currentAction = it.next();
      if (currentAction.isSeparator() && (prevAction == null || prevAction.isSeparator())) {
        it.remove();
        continue;
      }
      prevAction = currentAction;
    }
    // remove trailing separators
    while (it.hasPrevious()) {
      T previous = it.previous();
      if (previous.isSeparator()) {
        it.remove();
      }
      else {
        break;
      }
    }
  }

  public static <T extends IAction> List<T> getActions(List<T> actions, final Predicate<IAction> filter) {
    if (actions != null) {
      List<T> result = new ArrayList<>(actions.size());
      for (T a : actions) {
        if (a.isSeparator()) {
          result.add(a);
        }
        else if (filter.test(a)) {
          result.add(a);
        }
      }
      return result;
    }
    return CollectionUtility.emptyArrayList();
  }

  public static Predicate<IAction> createVisibleFilter() {
    return new Predicate<IAction>() {
      @Override
      public boolean test(IAction action) {
        if (action.isVisible()) {
          // remove menu groups with no visible child action
          if (action instanceof IActionNode<?> && ((IActionNode<?>) action).hasChildActions()) {
            List<?> visibleChildActions = getActions(((IActionNode<?>) action).getChildActions(), this);
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
   * If the widget is enabled, the given selectionEnabledStateSupplier is evaluated and all child {@link IActionNode
   * action-nodes} (having at least one of the given menu types) are updated according to the value of the supplier.
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
   * Recursively updates the {@link IDimensions#ENABLED_SLAVE} for all {@link IActionNode} instances (having at least
   * one of the given menu types) of the given {@link IWidget}.
   *
   * @param widget
   *          The {@link IWidget} whose {@link IActionNode actions} should be changed (recursively). Must not be
   *          {@code null}.
   * @param enabled
   *          The new enabled state of the {@link IActionNode actions} found.
   * @param menuTypes
   *          The menu types to update
   */
  public static void updateEnabledStateOfMenus(IWidget widget, boolean enabled, IMenuType... menuTypes) {
    Predicate<IAction> menusToUpdate = createMenuFilterMenuTypes(false, menuTypes);
    widget.visit(new UpdateMenuEnabledStateVisitor<>(enabled, menusToUpdate), IActionNode.class);
  }

  /**
   * @see #createMenuFilterMenuTypes(Set, boolean)
   */
  public static Predicate<IAction> createMenuFilterMenuTypes(boolean visibleOnly, IMenuType... menuTypes) {
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
  public static Predicate<IAction> createMenuFilterMenuTypes(Set<? extends IMenuType> menuTypes, boolean visibleOnly) {
    return new MenuTypeFilter(menuTypes, visibleOnly);
  }

  @SafeVarargs
  public static Predicate<IAction> createCombinedFilter(final Predicate<IAction>... actionFilters) {
    if (actionFilters != null) {
      return action -> {
        for (Predicate<IAction> f : actionFilters) {
          if (!f.test(action)) {
            return false;
          }
        }
        return true;
      };
    }
    return TRUE_FILTER;
  }

  public static class MenuTypeFilter implements Predicate<IAction> {

    private final boolean m_visibleOnly;
    private final Set<? extends IMenuType> m_menuTypes;

    public MenuTypeFilter(Set<? extends IMenuType> menuTypes, boolean visibleOnly) {
      m_menuTypes = menuTypes;
      m_visibleOnly = visibleOnly;
    }

    @Override
    public boolean test(IAction action) {
      if (action instanceof IMenu) {
        IMenu menu = (IMenu) action;
        if (isVisibleOnly() && !menu.isVisible()) {
          return false;
        }
        else {
          if (menu.hasChildActions()) {
            // check for filter matching child menus
            return !normalizedActions(menu.getChildActions(), this).isEmpty();
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
      return false;
    }

    public Set<? extends IMenuType> getMenuTypes() {
      return m_menuTypes;
    }

    public boolean isVisibleOnly() {
      return m_visibleOnly;
    }
  }

  /**
   * Updates the {@link IDimensions#ENABLED_SLAVE} state to the given value for all {@link IActionNode} instances
   * accepting the given {@link Predicate}.<br>
   * If a menu contains children it is disabled if all the child menus are disabled (according to
   * {@link IDimensions#ENABLED_SLAVE}) as well.<br>
   * Separators (see {@link IAction#isSeparator()}) are always ignored.<br>
   * {@link IActionNode} instances not matching the given filter are not touched.
   */
  public static class UpdateMenuEnabledStateVisitor<T extends IActionNode<?>> extends DepthFirstTreeVisitor<T> {

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
