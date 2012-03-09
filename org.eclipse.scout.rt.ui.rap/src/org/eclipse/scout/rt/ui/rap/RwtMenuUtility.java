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

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.checkbox.ICheckBoxMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
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
    if (scoutMenus != null && scoutMenus.length > 0) {
      int count = scoutMenus.length;
      int index = 0;
      for (IMenu scoutMenu : scoutMenus) {
        fillContextMenuRec(scoutMenu, uiEnvironment, index, count, menu);
        index++;
      }
    }
    else {
      menu.setVisible(false);
    }
  }

  private static void fillContextMenuRec(IActionNode<?> scoutActionNode, IRwtEnvironment uiEnvironment, int index, int count, Menu menu) {
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
      new RwtScoutCheckboxMenu(menu, (ICheckBoxMenu) scoutActionNode, uiEnvironment);

    }
    else if (scoutActionNode.getChildActionCount() > 0) {
      RwtScoutMenuGroup group = new RwtScoutMenuGroup(menu, scoutActionNode, uiEnvironment);
      Menu subMenu = new Menu(menu);
      group.getUiMenuItem().setMenu(subMenu);
      int subIndex = 0;
      int subCount = scoutActionNode.getChildActions().size();
      for (IActionNode<?> subAction : scoutActionNode.getChildActions()) {
        fillContextMenuRec(subAction, uiEnvironment, subIndex, subCount, subMenu);
        subIndex++;
      }
    }
    else {
      new RwtScoutMenuAction(menu, scoutActionNode, uiEnvironment);
    }
  }

  public static IMenu[] collectMenus(final ITree tree, IRwtEnvironment uiEnvironment) {
    final Holder<IMenu[]> menusHolder = new Holder<IMenu[]>(IMenu[].class);
    Runnable t = new Runnable() {
      @Override
      public void run() {
        menusHolder.setValue(tree.getUIFacade().fireNodePopupFromUI());
      }
    };

    JobEx job = uiEnvironment.invokeScoutLater(t, 1200);
    try {
      job.join(1200);
    }
    catch (InterruptedException ex) {
      LOG.warn("Exception occured while collecting menus.", ex);
    }

    return menusHolder.getValue();
  }

  public static IMenu[] collectMenus(final ITable table, IRwtEnvironment uiEnvironment) {
    final Holder<IMenu[]> menusHolder = new Holder<IMenu[]>(IMenu[].class);
    Runnable t = new Runnable() {
      @Override
      public void run() {
        menusHolder.setValue(table.getUIFacade().fireRowPopupFromUI());
      }
    };

    JobEx job = uiEnvironment.invokeScoutLater(t, 1200);
    try {
      job.join(1200);
    }
    catch (InterruptedException ex) {
      LOG.warn("Exception occured while collecting menus.", ex);
    }

    return menusHolder.getValue();
  }

}
