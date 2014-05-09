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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public final class SwtMenuUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtMenuUtility.class);

  private SwtMenuUtility() {
  }

  /**
   * @param parentMenu
   * @return
   */
  public static MenuItem createSwtMenuItem(Menu parentMenu, IMenu scoutMenu, ISwtEnvironment environment) {
    MenuItem swtMenuItem = null;
    if (scoutMenu.isSeparator()) {
      swtMenuItem = new MenuItem(parentMenu, SWT.SEPARATOR);
    }
    else if (scoutMenu.hasChildActions()) {
      swtMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
      createChildMenu(swtMenuItem, scoutMenu.getChildActions(), environment);
    }
    else if (scoutMenu.isToggleAction()) {
      swtMenuItem = new MenuItem(parentMenu, SWT.CHECK);
    }
    else {
      swtMenuItem = new MenuItem(parentMenu, SWT.PUSH);
    }
    return swtMenuItem;
  }

  /**
   * @param swtMenuItem
   * @param childActions
   */
  public static Menu createChildMenu(MenuItem swtMenuItem, List<IMenu> childActions, ISwtEnvironment environment) {
    Menu menu = new Menu(swtMenuItem);
    fillMenu(menu, childActions, environment);
    swtMenuItem.setMenu(menu);
    return menu;
  }

  /**
   * @param swtMenuItem
   * @param childActions
   */
  public static void fillMenu(Menu menu, List<IMenu> childActions, ISwtEnvironment environment) {
    fillMenu(menu, childActions, environment, false);
  }

  public static void fillMenu(Menu menu, List<IMenu> childActions, ISwtEnvironment environment, boolean separatorFirstIfHasMenus) {
    List<IMenu> visibleNormalizedActions = ActionUtility.visibleNormalizedActions(childActions);
    if (separatorFirstIfHasMenus && visibleNormalizedActions.size() > 0) {
      new MenuItem(menu, SWT.SEPARATOR);
    }
    for (IMenu childMenu : visibleNormalizedActions) {
      environment.createMenuItem(menu, childMenu);
    }

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
