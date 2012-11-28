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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.rap.action.RwtScoutAction;
import org.eclipse.scout.rt.ui.rap.action.RwtScoutCheckboxMenu;
import org.eclipse.scout.rt.ui.rap.action.RwtScoutMenuAction;
import org.eclipse.scout.rt.ui.rap.action.RwtScoutMenuGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public final class RwtMenuUtility {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtMenuUtility.class);

  private RwtMenuUtility() {
  }

  public static IContributionItem[] getMenuContribution(IActionNode<?>[] scoutActionNodes, IRwtEnvironment uiEnvironment) {
    ArrayList<IContributionItem> contributionItems = new ArrayList<IContributionItem>();
    for (IActionNode<?> scoutAction : scoutActionNodes) {
      if (scoutAction.isVisible()) {
        if (scoutAction.isSeparator()) {
          if (!(contributionItems.size() > 0 && contributionItems.get(contributionItems.size() - 1).isSeparator())) {
            contributionItems.add(new Separator());
          }
        }
        else if (scoutAction.hasChildActions()) {
          IMenuManager manager = new MenuManager(scoutAction.getText(), scoutAction.getActionId());
          fillMenuManager(scoutAction.getChildActions().toArray(new IActionNode<?>[scoutAction.getChildActionCount()]), uiEnvironment, manager);
          contributionItems.add(manager);
        }
        else if (scoutAction instanceof ICheckBoxMenu) {
          contributionItems.add(new ActionContributionItem(new RwtScoutAction(scoutAction, uiEnvironment, SWT.CHECK).getUiAction()));
        }
        else {
          contributionItems.add(new ActionContributionItem(new RwtScoutAction(scoutAction, uiEnvironment).getUiAction()));
        }
      }
    }
    return contributionItems.toArray(new IContributionItem[contributionItems.size()]);
  }

  public static IContributionItem getMenuContributionItem(IActionNode<?> scoutAction, IRwtEnvironment uiEnvironment) {
    IContributionItem item = null;
    if (scoutAction.isVisible()) {
      if (scoutAction.isSeparator()) {
        item = new Separator();
      }
      else if (scoutAction.hasChildActions()) {
        IMenuManager manager = new MenuManager(scoutAction.getText(), scoutAction.getActionId());
        fillMenuManager(scoutAction.getChildActions().toArray(new IActionNode<?>[scoutAction.getChildActionCount()]), uiEnvironment, manager);
        item = manager;
      }
      else if (scoutAction instanceof ICheckBoxMenu) {
        item = new ActionContributionItem(new RwtScoutAction(scoutAction, uiEnvironment, SWT.CHECK).getUiAction());
      }
      else {
        item = new ActionContributionItem(new RwtScoutAction(scoutAction, uiEnvironment).getUiAction());
      }
    }
    return item;
  }

  private static void fillMenuManager(IActionNode<?>[] scoutActionNodes, IRwtEnvironment uiEnvironment, IMenuManager manager) {
    for (IActionNode<?> scoutAction : scoutActionNodes) {
      if (scoutAction.isVisible()) {
        if (scoutAction.isSeparator()) {
          if (manager.getItems().length > 0 && manager.getItems()[manager.getItems().length - 1].isSeparator()) {

          }
          else {
            manager.add(new Separator());
          }
        }
        else if (scoutAction.hasChildActions()) {
          IMenuManager childManager = new MenuManager(scoutAction.getText(), scoutAction.getActionId());
          fillMenuManager(scoutAction.getChildActions().toArray(new IActionNode<?>[scoutAction.getChildActionCount()]), uiEnvironment, childManager);
          manager.add(childManager);
        }
        else if (scoutAction instanceof ICheckBoxMenu) {
          manager.add(new ActionContributionItem(new RwtScoutAction(scoutAction, uiEnvironment, SWT.CHECK).getUiAction()));
        }
        else {
          manager.add(new ActionContributionItem(new RwtScoutAction(scoutAction, uiEnvironment).getUiAction()));
        }
      }
    }
  }

  public static void fillContextMenu(IMenu[] scoutMenus, IRwtEnvironment uiEnvironment, Menu menu) {
    if (scoutMenus == null || scoutMenus.length == 0) {
      menu.setVisible(false);
      return;
    }

    List<IActionNode> scoutActionNodes = new LinkedList<IActionNode>();
    for (IMenu scoutMenu : scoutMenus) {
      scoutActionNodes.add(scoutMenu);
    }

    fillContextMenu(scoutActionNodes, uiEnvironment, menu);
  }

  public static void fillContextMenu(List<? extends IActionNode> scoutActionNodes, IRwtEnvironment uiEnvironment, Menu menu) {
    if (scoutActionNodes == null || scoutActionNodes.size() == 0) {
      menu.setVisible(false);
      return;
    }

    List<IActionNode> cleanedScoutActions = cleanup(scoutActionNodes);
    for (IActionNode scoutActionNode : cleanedScoutActions) {
      fillContextMenuRec(scoutActionNode, uiEnvironment, menu);
    }

  }

  private static void fillContextMenuRec(IActionNode<?> scoutActionNode, IRwtEnvironment uiEnvironment, Menu menu) {
    if (!scoutActionNode.isVisible()) {
      return;
    }
    if (scoutActionNode.isSeparator()) {
      new MenuItem(menu, SWT.SEPARATOR);
    }
    else if (scoutActionNode instanceof ICheckBoxMenu) {
      new RwtScoutCheckboxMenu(menu, (ICheckBoxMenu) scoutActionNode, uiEnvironment);

    }
    else if (scoutActionNode.getChildActionCount() > 0) {
      RwtScoutMenuGroup group = new RwtScoutMenuGroup(menu, scoutActionNode, uiEnvironment);
      Menu subMenu = new Menu(menu);
      group.getUiMenuItem().setMenu(subMenu);
      List<IActionNode> childActions = cleanup(scoutActionNode.getChildActions());
      for (IActionNode<?> subAction : childActions) {
        fillContextMenuRec(subAction, uiEnvironment, subMenu);
      }
    }
    else {
      new RwtScoutMenuAction(menu, scoutActionNode, uiEnvironment);
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
