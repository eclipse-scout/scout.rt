/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.ui.swt.action.AbstractSwtScoutMenu;
import org.eclipse.scout.rt.ui.swt.action.SwtScoutCheckboxMenuItem;
import org.eclipse.scout.rt.ui.swt.action.SwtScoutMenuItem;
import org.eclipse.swt.widgets.Menu;

public final class SwtMenuUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtMenuUtility.class);

  private SwtMenuUtility() {
  }

  public static void fillContextMenu(List<? extends IMenu> scoutMenus, Menu menu, ISwtEnvironment environment) {
    if (CollectionUtility.isEmpty(scoutMenus)) {
      return;
    }
    for (IMenu scoutMenu : ActionUtility.visibleNormalizedActions(scoutMenus)) {
      createMenuItem(scoutMenu, menu, environment);
    }
  }

  /**
   * NEW
   */
  public static AbstractSwtScoutMenu createMenuItem(IMenu scoutMenu, Menu swtMenu, ISwtEnvironment environment) {
    if (!isVisible(scoutMenu)) {
      return null;
    }
    if (scoutMenu.hasChildActions()) {
      return new org.eclipse.scout.rt.ui.swt.action.SwtScoutMenuGroup(scoutMenu, swtMenu, environment);
    }
    if (scoutMenu.isToggleAction()) {
      return new SwtScoutCheckboxMenuItem(scoutMenu, swtMenu, environment);
    }
    return new SwtScoutMenuItem(scoutMenu, swtMenu, environment);
  }

  public static boolean isVisible(IMenu menu) {
    if (!menu.isVisible()) {
      return false;
    }
    if (menu.hasChildActions()) {
      boolean visible = false;
      for (IMenu m : menu.getChildActions()) {

        if (!m.isSeparator() && m.isVisible()) {
          visible = true;
          break;
        }
      }
      return visible;
    }
    return true;
  }

  public static List<IMenu> consolidateMenus(List<IMenu> original) {
    List<IMenu> consolidatedMenus = new ArrayList<IMenu>(original.size());
    IMenu lastMenu = null;
    for (IMenu m : original) {
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

    for (int i = consolidatedMenus.size() - 1; i > -1; i--) {
      if (consolidatedMenus.get(i).isSeparator()) {
        consolidatedMenus.remove(i);
      }
      else {
        break;
      }
    }
    return consolidatedMenus;
  }

  public static List<IMenu> collectMenus(final ITree tree, final boolean emptySpaceActions, final boolean nodeActions, ISwtEnvironment uiEnvironment) {
    final List<IMenu> menuList = new LinkedList<IMenu>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        if (emptySpaceActions) {
          menuList.addAll(tree.getUIFacade().fireEmptySpacePopupFromUI());
        }
        if (nodeActions) {
          menuList.addAll(tree.getUIFacade().fireNodePopupFromUI());
        }
      }
    };

    JobEx job = uiEnvironment.invokeScoutLater(t, 5000);
    try {
      job.join(1200);
    }
    catch (InterruptedException ex) {
      LOG.warn("Exception occured while collecting menus.", ex);
    }

    return menuList;
  }

  public static List<IMenu> collectMenus(final ITable table, final boolean emptySpaceActions, final boolean rowActions, ISwtEnvironment uiEnvironment) {
    final List<IMenu> menuList = new LinkedList<IMenu>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        if (emptySpaceActions) {
          menuList.addAll(table.getUIFacade().fireEmptySpacePopupFromUI());
        }
        if (rowActions) {
          menuList.addAll(table.getUIFacade().fireRowPopupFromUI());
        }
      }
    };

    JobEx job = uiEnvironment.invokeScoutLater(t, 5000);
    try {
      job.join(1200);
    }
    catch (InterruptedException ex) {
      LOG.warn("Exception occured while collecting menus.", ex);
    }

    return menuList;
  }

  public static List<IMenu> collectMenus(final ICalendar calendar, final boolean emptySpaceActions, final boolean componentActions, ISwtEnvironment swtEnvironment) {
    final List<IMenu> menuList = new LinkedList<IMenu>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        if (emptySpaceActions) {
          menuList.addAll(calendar.getUIFacade().fireNewPopupFromUI());
        }
        if (componentActions) {
          menuList.addAll(calendar.getUIFacade().fireComponentPopupFromUI());
        }
      }
    };

    JobEx job = swtEnvironment.invokeScoutLater(t, 5000);
    try {
      job.join(1200);
    }
    catch (InterruptedException ex) {
      LOG.warn("Exception occured while collecting menus.", ex);
    }

    return menuList;
  }

  public static List<IMenu> collectRowMenus(final ITable table, ISwtEnvironment uiEnvironment) {
    return collectMenus(table, false, true, uiEnvironment);
  }

  public static List<IMenu> collectEmptySpaceMenus(final ITable table, ISwtEnvironment uiEnvironment) {
    return collectMenus(table, true, false, uiEnvironment);
  }

  public static List<IMenu> collectNodeMenus(final ITree tree, ISwtEnvironment uiEnvironment) {
    return collectMenus(tree, false, true, uiEnvironment);
  }

  public static List<IMenu> collectEmptySpaceMenus(final ITree tree, ISwtEnvironment uiEnvironment) {
    return collectMenus(tree, true, false, uiEnvironment);
  }

  public static List<IMenu> collectComponentMenus(final ICalendar calendar, ISwtEnvironment swtEnvironment) {
    return collectMenus(calendar, false, true, swtEnvironment);
  }

  public static List<IMenu> collectEmptySpaceMenus(final ICalendar calendar, ISwtEnvironment swtEnvironment) {
    return collectMenus(calendar, true, false, swtEnvironment);
  }
}
