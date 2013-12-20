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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.rap.action.MenuFactory;
import org.eclipse.swt.widgets.Menu;

public final class RwtMenuUtility {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtMenuUtility.class);

  private RwtMenuUtility() {
  }

  public static void fillContextMenu(IMenu[] scoutMenus, IRwtEnvironment uiEnvironment, Menu menu) {
    MenuFactory menuFactory = uiEnvironment.getMenuFactory();
    if (menuFactory != null) {
      menuFactory.fillContextMenu(menu, scoutMenus, uiEnvironment);
    }
  }

  public static void fillContextMenu(List<? extends IActionNode> scoutActionNodes, IRwtEnvironment uiEnvironment, Menu menu) {
    MenuFactory menuFactory = uiEnvironment.getMenuFactory();
    if (menuFactory != null) {
      menuFactory.fillContextMenu(menu, scoutActionNodes, uiEnvironment);
    }
  }

  public static IMenu[] collectMenus(final IButton button, IRwtEnvironment uiEnvironment) {
    final List<IMenu> menuList = new LinkedList<IMenu>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        menuList.addAll(Arrays.asList(button.getUIFacade().fireButtonPopupFromUI()));
      }
    };

    JobEx job = uiEnvironment.invokeScoutLater(t, 5000);
    try {
      job.join(1200);
    }
    catch (InterruptedException ex) {
      LOG.warn("Exception occured while collecting menus.", ex);
    }

    return menuList.toArray(new IMenu[menuList.size()]);
  }

  public static IMenu[] collectMenus(final ITree tree, final boolean emptySpaceActions, final boolean nodeActions, IRwtEnvironment uiEnvironment) {
    final List<IMenu> menuList = new LinkedList<IMenu>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        if (emptySpaceActions) {
          menuList.addAll(Arrays.asList(tree.getUIFacade().fireEmptySpacePopupFromUI()));
        }
        if (nodeActions) {
          menuList.addAll(Arrays.asList(tree.getUIFacade().fireNodePopupFromUI()));
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

    return menuList.toArray(new IMenu[menuList.size()]);
  }

  public static IMenu[] collectMenus(final ITable table, final boolean emptySpaceActions, final boolean rowActions, IRwtEnvironment uiEnvironment) {
    final List<IMenu> menuList = new LinkedList<IMenu>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        if (emptySpaceActions) {
          menuList.addAll(Arrays.asList(table.getUIFacade().fireEmptySpacePopupFromUI()));
        }
        if (rowActions) {
          menuList.addAll(Arrays.asList(table.getUIFacade().fireRowPopupFromUI()));
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

    return menuList.toArray(new IMenu[menuList.size()]);
  }

  public static IMenu[] collectMenus(final ICalendar calendar, final boolean emptySpaceActions, final boolean componentActions, IRwtEnvironment uiEnvironment) {
    final List<IMenu> menuList = new LinkedList<IMenu>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        if (emptySpaceActions) {
          menuList.addAll(Arrays.asList(calendar.getUIFacade().fireNewPopupFromUI()));
        }
        if (componentActions) {
          menuList.addAll(Arrays.asList(calendar.getUIFacade().fireComponentPopupFromUI()));
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

    return menuList.toArray(new IMenu[menuList.size()]);
  }

  public static IMenu[] collectRowMenus(final ITable table, IRwtEnvironment uiEnvironment) {
    return collectMenus(table, false, true, uiEnvironment);
  }

  public static IMenu[] collectEmptySpaceMenus(final ITable table, IRwtEnvironment uiEnvironment) {
    return collectMenus(table, true, false, uiEnvironment);
  }

  public static IMenu[] collectNodeMenus(final ITree tree, IRwtEnvironment uiEnvironment) {
    return collectMenus(tree, false, true, uiEnvironment);
  }

  public static IMenu[] collectEmptySpaceMenus(final ITree tree, IRwtEnvironment uiEnvironment) {
    return collectMenus(tree, true, false, uiEnvironment);
  }

  public static IMenu[] collectComponentMenus(final ICalendar calendar, IRwtEnvironment uiEnvironment) {
    return collectMenus(calendar, false, true, uiEnvironment);
  }

  public static IMenu[] collectEmptySpaceMenus(final ICalendar calendar, IRwtEnvironment uiEnvironment) {
    return collectMenus(calendar, true, false, uiEnvironment);
  }

  /**
   * Splits the menus in groups, separated by the {@link IActionNode#isSeparator()}
   */
  public static List<List<IMenu>> split(IMenu[] unseparatedMenus) {
    List<List<IMenu>> separatedMenus = new LinkedList<List<IMenu>>();

    List<IMenu> menus = new LinkedList<IMenu>();
    for (IMenu menu : unseparatedMenus) {
      if (menu.isSeparator()) {
        separatedMenus.add(menus);
        menus = new LinkedList<IMenu>();
      }
      else {
        menus.add(menu);
      }
    }

    if (!separatedMenus.contains(menus)) {
      separatedMenus.add(menus);
    }

    return separatedMenus;
  }

  /**
   * Removes invisible actions. Also removes leading and trailing separators as well as multiple consecutive separators.
   * 
   * @since 3.8.1
   */
  public static List<IActionNode> cleanup(List<? extends IActionNode> scoutActionNodes) {
    if (scoutActionNodes == null) {
      return null;
    }

    List<IActionNode> cleanedActions = new LinkedList<IActionNode>();
    for (int i = 0; i < scoutActionNodes.size(); i++) {
      IActionNode actionNode = scoutActionNodes.get(i);
      //Ignore invisible actions
      if (!actionNode.isVisible()) {
        continue;
      }
      if (actionNode.isSeparator()) {
        //Ignore leading and trailing separators
        if (i == 0 || i == scoutActionNodes.size() - 1) {
          continue;
        }
        //Ignore multiple consecutive separators
        IAction nextVisibleAction = getFirstVisibleAction(scoutActionNodes, i + 1);
        if (nextVisibleAction == null || nextVisibleAction.isSeparator()) {
          continue;
        }
      }

      cleanedActions.add(actionNode);
    }

    return cleanedActions;
  }

  private static IAction getFirstVisibleAction(List<? extends IActionNode> scoutActionNodes, int startIndex) {
    if (scoutActionNodes == null) {
      return null;
    }

    for (int i = startIndex; i < scoutActionNodes.size(); i++) {
      IActionNode action = scoutActionNodes.get(i);
      if (action.isVisible()) {
        return action;
      }
    }

    return null;
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
