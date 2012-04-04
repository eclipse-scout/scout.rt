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

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.checkbox.ICheckBoxMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
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
  private static IScoutLogger LOG = ScoutLogManager.getLogger(SwtMenuUtility.class);

  private SwtMenuUtility() {
  }

  public static IContributionItem[] getMenuContribution(IActionNode<?>[] scoutActionNodes, ISwtEnvironment environment) {
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
          fillMenuManager(scoutAction.getChildActions().toArray(new IActionNode<?>[scoutAction.getChildActionCount()]), manager, environment);
          contributionItems.add(manager);
        }
        else if (scoutAction instanceof ICheckBoxMenu) {
          contributionItems.add(new ActionContributionItem(new SwtScoutAction(scoutAction, environment, SWT.CHECK).getSwtAction()));
        }
        else {
          contributionItems.add(new ActionContributionItem(new SwtScoutAction(scoutAction, environment).getSwtAction()));
        }
      }
    }
    return contributionItems.toArray(new IContributionItem[contributionItems.size()]);
  }

  public static IContributionItem getMenuContributionItem(IActionNode<?> scoutAction, ISwtEnvironment environment) {
    IContributionItem item = null;
    if (scoutAction.isVisible()) {
      if (scoutAction.isSeparator()) {
        item = new Separator();
      }
      else if (scoutAction.hasChildActions()) {
        IMenuManager manager = new MenuManager(scoutAction.getText(), scoutAction.getActionId());
        fillMenuManager(scoutAction.getChildActions().toArray(new IActionNode<?>[scoutAction.getChildActionCount()]), manager, environment);
        item = manager;
      }
      else if (scoutAction instanceof ICheckBoxMenu) {
        item = new ActionContributionItem(new SwtScoutAction(scoutAction, environment, SWT.CHECK).getSwtAction());
      }
      else {
        item = new ActionContributionItem(new SwtScoutAction(scoutAction, environment).getSwtAction());
      }
    }
    return item;
  }

  private static void fillMenuManager(IActionNode<?>[] scoutActionNodes, IMenuManager manager, ISwtEnvironment environment) {
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
          fillMenuManager(scoutAction.getChildActions().toArray(new IActionNode<?>[scoutAction.getChildActionCount()]), childManager, environment);
          manager.add(childManager);
        }
        else if (scoutAction instanceof ICheckBoxMenu) {
          manager.add(new ActionContributionItem(new SwtScoutAction(scoutAction, environment, SWT.CHECK).getSwtAction()));
        }
        else {
          manager.add(new ActionContributionItem(new SwtScoutAction(scoutAction, environment).getSwtAction()));
        }
      }
    }
  }

  public static void fillContextMenu(IMenu[] scoutMenus, Menu menu, ISwtEnvironment environment) {
    if (scoutMenus != null && scoutMenus.length > 0) {
      int count = scoutMenus.length;
      int index = 0;
      for (IMenu scoutMenu : scoutMenus) {
        fillContextMenuRec(scoutMenu, index, count, menu, environment);
        index++;
      }
    }

  }

  private static void fillContextMenuRec(IActionNode<?> scoutActionNode, int index, int count, Menu menu, ISwtEnvironment environment) {
    if (!scoutActionNode.isVisible()) {
      return;
    }
    if (scoutActionNode.isSeparator()) {
      if (menu.getItemCount() > 0 && (SWT.SEPARATOR & menu.getItem(menu.getItemCount() - 1).getStyle()) == 0) {
        // ignore trailing separator
        if (index + 1 < count) {
          new MenuItem(menu, SWT.SEPARATOR);
        }
      }
    }
    else if (scoutActionNode instanceof ICheckBoxMenu) {
      new SwtScoutCheckboxMenu(menu, (ICheckBoxMenu) scoutActionNode, environment);

    }
    else if (scoutActionNode.getChildActionCount() > 0) {
      SwtScoutMenuGroup group = new SwtScoutMenuGroup(menu, scoutActionNode, environment);
      Menu subMenu = new Menu(menu);
      group.getSwtMenuItem().setMenu(subMenu);
      int subIndex = 0;
      int subCount = scoutActionNode.getChildActions().size();
      for (IActionNode<?> subAction : scoutActionNode.getChildActions()) {
        fillContextMenuRec(subAction, subIndex, subCount, subMenu, environment);
        subIndex++;
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

}
