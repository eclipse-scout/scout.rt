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
package org.eclipse.scout.rt.client.ui.action.menu;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.Range;

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
   * @param original
   * @return a list of all visible menus an eliminated multiple occurrences of separators.
   */
  public static <T extends IActionNode<?>> List<T> consolidateMenus(List<T> original) {
    LinkedList<T> consolidatedMenus = new LinkedList<T>();
    T lastMenu = null;
    for (T m : original) {
      if (isVisible(m)) {
        if (m.isSeparator()) {
          if (lastMenu != null && !lastMenu.isSeparator()) {
            consolidatedMenus.add(m);
          }
        }
        else {
          consolidatedMenus.add(m);
        }
        lastMenu = m;
      }
    }

    // remove tailing separators
    while (!consolidatedMenus.isEmpty() && consolidatedMenus.getLast().isSeparator()) {
      consolidatedMenus.removeLast();
    }
    return consolidatedMenus;
  }

  public static Set<PlannerMenuType> getMenuTypesForPlannerSelection(List<? extends Resource<?>> selectedResources, Activity<?, ?> selectedActivity, Range<Date> selectionRange) {
    if (CollectionUtility.isEmpty(selectedResources)) {
      return CollectionUtility.hashSet(PlannerMenuType.EmptySpace);
    }
    Set<PlannerMenuType> menuTypes = new HashSet<PlannerMenuType>();
    if (CollectionUtility.size(selectedResources) > 0) {
      menuTypes.add(PlannerMenuType.Resource);
    }
    if (selectedActivity != null) {
      menuTypes.add(PlannerMenuType.Activity);
    }
    else if (selectionRange.getFrom() != null || selectionRange.getTo() != null) {
      menuTypes.add(PlannerMenuType.Range);
    }
    return menuTypes;
  }

  public static Set<CalendarMenuType> getMenuTypesForCalendarSelection(CalendarComponent selectedComponent) {
    if (selectedComponent == null) {
      return CollectionUtility.hashSet(CalendarMenuType.EmptySpace);
    }
    else {
      return CollectionUtility.hashSet(CalendarMenuType.CalendarComponent);
    }
  }

  public static Set<TableMenuType> getMenuTypesForTableSelection(List<? extends ITableRow> selection) {
    if (CollectionUtility.isEmpty(selection)) {
      return CollectionUtility.hashSet(TableMenuType.EmptySpace);
    }
    else if (CollectionUtility.size(selection) == 1) {
      return CollectionUtility.hashSet(TableMenuType.SingleSelection);
    }
    else {
      return CollectionUtility.hashSet(TableMenuType.MultiSelection);
    }
  }

  public static Set<ValueFieldMenuType> getMenuTypesForValueFieldValue(Object value) {
    if (value == null) {
      return CollectionUtility.hashSet(ValueFieldMenuType.Null);
    }
    else {
      return CollectionUtility.hashSet(ValueFieldMenuType.NotNull);
    }
  }

  public static Set<TreeMenuType> getMenuTypesForTreeSelection(Set<? extends ITreeNode> selection) {
    if (CollectionUtility.isEmpty(selection)) {
      return CollectionUtility.hashSet(TreeMenuType.EmptySpace);
    }
    else if (CollectionUtility.size(selection) == 1) {
      return CollectionUtility.hashSet(TreeMenuType.SingleSelection);
    }
    else {
      return CollectionUtility.hashSet(TreeMenuType.MultiSelection);
    }
  }

  /**
   * @return the sub-menu of the given context menu owner that implements the given type. If no implementation is found,
   *         <code>null</code> is returned. Note: This method uses instance-of checks, hence the menu replacement
   *         mapping is not required.
   * @throws IllegalArgumentException
   *           when more than one menu implements the given type
   * @throws IllegalArgumentException
   *           when no context menu owner is provided.
   */
  public static <T extends IMenu> T getMenuByClass(IContextMenuOwner contextMenuOwner, final Class<T> menuType) {
    if (contextMenuOwner == null) {
      throw new IllegalArgumentException("Argument 'contextMenuOwner' must not be null");
    }
    IContextMenu contextMenu = contextMenuOwner.getContextMenu();

    final List<T> collectedMenus = new ArrayList<T>();
    if (contextMenu != null && menuType != null) {
      contextMenu.acceptVisitor(new IActionVisitor() {
        @Override
        public int visit(IAction action) {
          if (menuType.isAssignableFrom(action.getClass())) {
            @SuppressWarnings("unchecked")
            T menu = (T) action;
            collectedMenus.add(menu);
          }
          return CONTINUE;
        }
      });
    }

    if (collectedMenus.isEmpty()) {
      return null;
    }
    if (collectedMenus.size() == 1) {
      return collectedMenus.get(0);
    }
    throw new IllegalStateException("Ambiguous menu type " + menuType.getName() + "! More than one implementation was found: " + CollectionUtility.format(collectedMenus));
  }
}
