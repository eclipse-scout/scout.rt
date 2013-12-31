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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.checkbox.ICheckBoxMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.ui.swt.action.SwtScoutAction;
import org.eclipse.scout.rt.ui.swt.action.SwtScoutCheckboxMenu;
import org.eclipse.scout.rt.ui.swt.action.SwtScoutMenuAction;
import org.eclipse.scout.rt.ui.swt.action.SwtScoutMenuGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public final class SwtMenuUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtMenuUtility.class);

  private SwtMenuUtility() {
  }

  public static IContributionItem[] getMenuContribution(IActionNode<?>[] scoutActionNodes, ISwtEnvironment environment) {
    ArrayList<IContributionItem> contributionItems = new ArrayList<IContributionItem>();
    for (IActionNode<?> scoutAction : scoutActionNodes) {
      if (!scoutAction.isVisible()) {
        continue;
      }
      if (scoutAction.isSeparator()
          //ignore trailing separator
          && contributionItems.size() > 0 && contributionItems.get(contributionItems.size() - 1).isSeparator()) {
        continue;
      }

      contributionItems.add(getMenuContributionItem(scoutAction, environment));
    }
    return contributionItems.toArray(new IContributionItem[contributionItems.size()]);
  }

  private static void fillMenuManager(IActionNode<?>[] scoutActionNodes, IMenuManager manager, ISwtEnvironment environment, boolean disableChildren) {
    for (IActionNode<?> scoutAction : scoutActionNodes) {
      if (!scoutAction.isVisible()) {
        continue;
      }
      if (scoutAction.isSeparator()
          //ignore trailing separator
          && manager.getItems().length > 0 && manager.getItems()[manager.getItems().length - 1].isSeparator()) {
        continue;
      }

      manager.add(getMenuContributionItem(scoutAction, environment, disableChildren));
    }
  }

  public static IContributionItem getMenuContributionItem(IActionNode<?> scoutAction, ISwtEnvironment environment) {
    return getMenuContributionItem(scoutAction, environment, false);
  }

  public static IContributionItem getMenuContributionItem(IActionNode<?> scoutAction, ISwtEnvironment environment, boolean disableItem) {
    if (!scoutAction.isVisible()) {
      return null;
    }

    if (scoutAction.isSeparator()) {
      return new Separator();
    }

    if (scoutAction.hasChildActions()) {
      IMenuManager manager = new MenuManager(scoutAction.getTextWithMnemonic(), scoutAction.getActionId());
      //Disable children since menuManager itself can't be disabled
      boolean disableChilds = !scoutAction.isEnabled() || disableItem;
      fillMenuManager(scoutAction.getChildActions().toArray(new IActionNode<?>[scoutAction.getChildActionCount()]), manager, environment, disableChilds);
      return manager;
    }

    if (scoutAction instanceof ICheckBoxMenu) {
      Action swtAction = new SwtScoutAction(scoutAction, environment, SWT.CHECK).getSwtAction();
      if (disableItem) {
        swtAction.setEnabled(false);
      }
      return new ActionContributionItem(swtAction);
    }

    Action swtAction = new SwtScoutAction(scoutAction, environment).getSwtAction();
    if (disableItem) {
      swtAction.setEnabled(false);
    }
    return new ActionContributionItem(swtAction);
  }

  public static void fillContextMenu(IMenu[] scoutMenus, Menu menu, ISwtEnvironment environment) {
    if (scoutMenus == null || scoutMenus.length == 0) {
      return;
    }

    List<IActionNode> scoutActionNodes = new LinkedList<IActionNode>();
    for (IMenu scoutMenu : scoutMenus) {
      scoutActionNodes.add(scoutMenu);
    }

    fillContextMenu(scoutActionNodes, menu, environment);
  }

  public static void fillContextMenu(List<? extends IActionNode> scoutActionNodes, Menu menu, ISwtEnvironment environment) {
    if (scoutActionNodes == null || scoutActionNodes.size() == 0) {
      return;
    }

    List<IActionNode> cleanedScoutActions = cleanup(scoutActionNodes);
    for (IActionNode scoutActionNode : cleanedScoutActions) {
      fillContextMenuRec(scoutActionNode, menu, environment);
    }
  }

  private static void fillContextMenuRec(IActionNode<?> scoutActionNode, Menu menu, ISwtEnvironment environment) {
    if (!scoutActionNode.isVisible()) {
      return;
    }
    if (scoutActionNode.isSeparator()) {
      new MenuItem(menu, SWT.SEPARATOR);
    }
    else if (scoutActionNode instanceof ICheckBoxMenu) {
      new SwtScoutCheckboxMenu(menu, (ICheckBoxMenu) scoutActionNode, environment);

    }
    else if (scoutActionNode.getChildActionCount() > 0) {
      SwtScoutMenuGroup group = new SwtScoutMenuGroup(menu, scoutActionNode, environment);
      Menu subMenu = new Menu(menu);
      group.getSwtMenuItem().setMenu(subMenu);
      List<IActionNode> childActions = cleanup(scoutActionNode.getChildActions());
      for (IActionNode<?> subAction : childActions) {
        fillContextMenuRec(subAction, subMenu, environment);
      }
    }
    else {
      new SwtScoutMenuAction(menu, scoutActionNode, environment);
    }
  }

  public static IMenu[] collectMenus(final ITree tree, final boolean emptySpaceActions, final boolean nodeActions, ISwtEnvironment uiEnvironment) {
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

  public static IMenu[] collectMenus(final ITable table, final boolean emptySpaceActions, final boolean rowActions, ISwtEnvironment uiEnvironment) {
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

  public static IMenu[] collectMenus(final ICalendar calendar, final boolean emptySpaceActions, final boolean componentActions, ISwtEnvironment swtEnvironment) {
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

    JobEx job = swtEnvironment.invokeScoutLater(t, 5000);
    try {
      job.join(1200);
    }
    catch (InterruptedException ex) {
      LOG.warn("Exception occured while collecting menus.", ex);
    }

    return menuList.toArray(new IMenu[menuList.size()]);
  }

  public static IMenu[] collectRowMenus(final ITable table, ISwtEnvironment uiEnvironment) {
    return collectMenus(table, false, true, uiEnvironment);
  }

  public static IMenu[] collectEmptySpaceMenus(final ITable table, ISwtEnvironment uiEnvironment) {
    return collectMenus(table, true, false, uiEnvironment);
  }

  public static IMenu[] collectNodeMenus(final ITree tree, ISwtEnvironment uiEnvironment) {
    return collectMenus(tree, false, true, uiEnvironment);
  }

  public static IMenu[] collectEmptySpaceMenus(final ITree tree, ISwtEnvironment uiEnvironment) {
    return collectMenus(tree, true, false, uiEnvironment);
  }

  public static IMenu[] collectComponentMenus(final ICalendar calendar, ISwtEnvironment swtEnvironment) {
    return collectMenus(calendar, false, true, swtEnvironment);
  }

  public static IMenu[] collectEmptySpaceMenus(final ICalendar calendar, ISwtEnvironment swtEnvironment) {
    return collectMenus(calendar, true, false, swtEnvironment);
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

}
