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
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public final class ActionUtility {

  private ActionUtility() {
  }

  public static final Predicate<IAction> FALSE_FILTER = action -> false;

  public static final Predicate<IAction> TRUE_FILTER = action -> true;

  /**
   * @deprecated will be removed in 24.1, use {@link MenuUtility#visibleNormalizedMenus(List)}.
   */
  @SuppressWarnings("DeprecatedIsStillUsed")
  @Deprecated
  public static <T extends IAction> List<T> visibleNormalizedActions(List<T> actionNodes) {
    return normalizedActions(actionNodes, createVisibleFilter());
  }

  /**
   * @deprecated will be removed in 24.1, use {@link MenuUtility#normalizedMenus(List, Predicate)}.
   */
  @Deprecated
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

  /**
   * @deprecated will be removed in 24.1, use {@link MenuUtility#filterMenusRec(List, Predicate)}.
   */
  @Deprecated
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

  /**
   * @deprecated will be removed in 24.1, use {@link MenuUtility#createVisibleFilter()}.
   */
  @Deprecated
  public static Predicate<IAction> createVisibleFilter() {
    return new Predicate<>() {
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
   * @deprecated will be removed in 24.1, use
   *             {@link MenuUtility#updateContextMenuEnabledState(AbstractContextMenu, BooleanSupplier, IMenuType...)}.
   */
  @Deprecated
  public static void updateContextMenuEnabledState(AbstractContextMenu<? extends IWidget> contextMenu, BooleanSupplier selectionEnabledStateSupplier, IMenuType... menuTypes) {
    MenuUtility.updateContextMenuEnabledState(contextMenu, selectionEnabledStateSupplier, menuTypes);
  }

  /**
   * @deprecated will be removed in 24.1, use
   *             {@link MenuUtility#updateEnabledStateOfMenus(IWidget, boolean, IMenuType...)}.
   */
  @Deprecated
  public static void updateEnabledStateOfMenus(IWidget widget, boolean enabled, IMenuType... menuTypes) {
    MenuUtility.updateEnabledStateOfMenus(widget, enabled, menuTypes);
  }

  /**
   * @deprecated will be removed in 24.1, use {@link MenuUtility#createMenuFilterMenuTypes(boolean, IMenuType...)}.
   */
  @Deprecated
  public static Predicate<IAction> createMenuFilterMenuTypes(boolean visibleOnly, IMenuType... menuTypes) {
    return action -> Optional.ofNullable(action)
        .filter(IMenu.class::isInstance)
        .map(IMenu.class::cast)
        .filter(MenuUtility.createMenuFilterMenuTypes(visibleOnly, menuTypes))
        .isPresent();
  }

  /**
   * @deprecated will be removed in 24.1, use {@link MenuUtility#createMenuFilterMenuTypes(Set, boolean)}.
   */
  @Deprecated
  public static Predicate<IAction> createMenuFilterMenuTypes(Set<? extends IMenuType> menuTypes, boolean visibleOnly) {
    return action -> Optional.ofNullable(action)
        .filter(IMenu.class::isInstance)
        .map(IMenu.class::cast)
        .filter(MenuUtility.createMenuFilterMenuTypes(menuTypes, visibleOnly))
        .isPresent();
  }

  /**
   * @deprecated will be removed in 24.1, use {@link Predicate#and(Predicate)}.
   */
  @Deprecated
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
}
