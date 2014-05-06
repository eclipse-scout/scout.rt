/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.ui.rap.action.MenuSizeEstimator;
import org.eclipse.scout.rt.ui.rap.action.menu.RwtScoutMenuItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.presentations.util.ISystemMenu;

public final class RwtMenuUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtMenuUtility.class);

  private RwtMenuUtility() {
  }

  public static Point getMenuLocation(List<? extends IMenu> scoutActions, Menu menu, Point proposedLocation, IRwtEnvironment env) {
    int menuHeight = new MenuSizeEstimator(menu).estimateMenuHeight(scoutActions);
    if (shouldMenuOpenOnTop(proposedLocation, env, menuHeight)) {
      return computeMenuPositionForTop(proposedLocation, menuHeight);
    }
    else {
      return proposedLocation;
    }
  }

  private static boolean shouldMenuOpenOnTop(Point proposedLocation, IRwtEnvironment env, int menuHeight) {
    int displayHeight = env.getDisplay().getBounds().height;
    return proposedLocation.y + menuHeight > displayHeight;
  }

  private static Point computeMenuPositionForTop(Point proposedLocation, int menuHeight) {
    return new Point(proposedLocation.x, proposedLocation.y - menuHeight);
  }

  public static List<IMenu> collectMenus(final ICalendar calendar, final boolean emptySpaceActions, final boolean componentActions, IRwtEnvironment uiEnvironment) {
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

    JobEx job = uiEnvironment.invokeScoutLater(t, 5000);
    try {
      job.join(1200);
    }
    catch (InterruptedException ex) {
      LOG.warn("Exception occured while collecting menus.", ex);
    }

    return menuList;
  }

  /**
   * NEW
   * 
   * @param swtMenuItem
   * @param childActions
   */
  public static void fillMenu(Menu menu, List<IMenu> childActions, IRwtEnvironment environment) {
    for (IMenu childMenu : ActionUtility.visibleNormalizedActions(childActions)) {
      new RwtScoutMenuItem(childMenu, menu, environment);
    }
  }

  /**
   * NEW
   * 
   * @param parentMenu
   * @return
   */
  public static MenuItem createRwtMenuItem(Menu parentMenu, IMenu scoutMenu, IRwtEnvironment environment) {
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
   * NEW
   * 
   * @param swtMenuItem
   * @param childActions
   */
  public static Menu createChildMenu(MenuItem swtMenuItem, List<IMenu> childActions, IRwtEnvironment environment) {
    Menu menu = new Menu(swtMenuItem);
    fillMenu(menu, childActions, environment);
    swtMenuItem.setMenu(menu);
    return menu;
  }

  public static List<IMenu> collectMenus(final ITree tree, final boolean emptySpaceActions, final boolean nodeActions, IRwtEnvironment uiEnvironment) {
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

  public static List<IMenu> collectMenus(final ITable table, final boolean emptySpaceActions, final boolean rowActions, IRwtEnvironment uiEnvironment) {
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

  public static List<IMenu> collectRowMenus(final ITable table, IRwtEnvironment uiEnvironment) {
    return collectMenus(table, false, true, uiEnvironment);
  }

  public static List<IMenu> collectEmptySpaceMenus(final ITable table, IRwtEnvironment uiEnvironment) {
    return collectMenus(table, true, false, uiEnvironment);
  }

  public static List<IMenu> collectNodeMenus(final ITree tree, IRwtEnvironment uiEnvironment) {
    return collectMenus(tree, false, true, uiEnvironment);
  }

  public static List<IMenu> collectEmptySpaceMenus(final ITree tree, IRwtEnvironment uiEnvironment) {
    return collectMenus(tree, true, false, uiEnvironment);
  }

  /**
   * Removes invisible actions. Also removes leading and trailing separators as well as multiple consecutive separators.
   * 
   * @since 3.8.1
   */
  public static <T extends IActionNode<?>> List<T> cleanup(List<T> scoutActionNodes) {
    if (scoutActionNodes == null) {
      return null;
    }

    List<T> cleanedActions = new LinkedList<T>(scoutActionNodes);
    Iterator<T> it = cleanedActions.iterator();
    while (it.hasNext()) {
      if (isExcludedAction(it.next())) {
        it.remove();
      }
    }
    return MenuUtility.consolidateMenus(cleanedActions);
  }

  private static boolean isExcludedAction(IActionNode action) {
    return action instanceof ISystemMenu;
  }

  public static boolean hasChildActions(IAction action) {
    if (!(action instanceof IActionNode<?>)) {
      return false;
    }

    IActionNode<? extends IActionNode> actionNode = (IActionNode<?>) action;
    return actionNode.hasChildActions();
  }

  public static boolean hasVisibleChildActions(IAction action) {
    if (!(action instanceof IActionNode<?>)) {
      return false;
    }

    IActionNode<? extends IActionNode> actionNode = (IActionNode<?>) action;
    for (IActionNode child : actionNode.getChildActions()) {
      if (child.isVisible()) {
        return true;
      }

      if (child.hasChildActions()) {
        if (hasVisibleChildActions(child)) {
          return true;
        }
      }
    }

    return false;
  }

  public static List<? extends IActionNode> getChildActions(IAction action) {
    if (!(action instanceof IActionNode<?>)) {
      return null;
    }

    IActionNode<? extends IActionNode> actionNode = (IActionNode<?>) action;
    if (!actionNode.hasChildActions()) {
      return null;
    }

    return actionNode.getChildActions();
  }
}
