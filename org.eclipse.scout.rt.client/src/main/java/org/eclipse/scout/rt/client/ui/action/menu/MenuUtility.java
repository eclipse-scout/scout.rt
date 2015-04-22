/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;

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

  public static Set<ActivityMapMenuType> getMenuTypesForActivityMapSelection(ActivityCell<?, ?> selectedCell) {
    if (selectedCell == null) {
      return CollectionUtility.hashSet(ActivityMapMenuType.Selection);
    }
    else {
      return CollectionUtility.hashSet(ActivityMapMenuType.Activity);
    }
  }

  public static Set<PlannerMenuType> getMenuTypesForPlannerSelection(org.eclipse.scout.rt.client.ui.basic.planner.Activity<?, ?> selectedCell) {
    if (selectedCell == null) {
      return CollectionUtility.hashSet(PlannerMenuType.Selection);
    }
    else {
      return CollectionUtility.hashSet(PlannerMenuType.Activity);
    }
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

}
