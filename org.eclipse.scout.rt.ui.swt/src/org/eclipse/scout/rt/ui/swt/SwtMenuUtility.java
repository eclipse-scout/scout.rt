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
import java.util.Collections;
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

//  /**
//   * @param scoutActionNodes
//   * @param environment
//   * @return
//   * @deprecated since 4.0.0 use {@link org.eclipse.scout.rt.ui.swt.menu.SwtScoutMenuContributionItem} instead
//   *
//   *             <pre>
//   * IMenu[] menus = env.getClientSession().getDesktop().getMenus();
//   * List&lt;IMenu&gt; consolidatedMenus = SwtMenuUtility.consolidateMenus(Arrays.asList(menus));
//   * List&lt;IContributionItem&gt; swtContributionItems = new ArrayList&lt;IContributionItem&gt;();
//   * for (IMenu menu : consolidatedMenus) {
//   *   swtContributionItems.add(new SwtScoutMenuContributionItem(menu, env));
//   * }
//   * return swtContributionItems.toArray(new IContributionItem[swtContributionItems.size()]);
//   * </pre>
//   */
//  @Deprecated
//  public static IContributionItem[] getMenuContribution(List<? extends IActionNode<?>> scoutActionNodes, ISwtEnvironment environment) {
//    List<IContributionItem> contributionItems = new ArrayList<IContributionItem>();
//    for (IActionNode<?> scoutAction : scoutActionNodes) {
//      if (!scoutAction.isVisible()) {
//        continue;
//      }
//      if (scoutAction.isSeparator()
//          //ignore trailing separator
//          && contributionItems.size() > 0 && contributionItems.get(contributionItems.size() - 1).isSeparator()) {
//        continue;
//      }
//
//      contributionItems.add(getMenuContributionItem(scoutAction, environment));
//    }
//    return contributionItems.toArray(new IContributionItem[contributionItems.size()]);
//  }

//  private static void fillMenuManager(IActionNode<?>[] scoutActionNodes, IMenuManager manager, ISwtEnvironment environment, boolean disableChildren) {
//    for (IActionNode<?> scoutAction : scoutActionNodes) {
//      if (!scoutAction.isVisible()) {
//        continue;
//      }
//      if (scoutAction.isSeparator()
//          //ignore trailing separator
//          && manager.getItems().length > 0 && manager.getItems()[manager.getItems().length - 1].isSeparator()) {
//        continue;
//      }
//
//      manager.add(getMenuContributionItem(scoutAction, environment, disableChildren));
//    }
//  }

//  /**
//   * @param scoutAction
//   * @param environment
//   * @return
//   * @deprecated since 4.0.0
//   */
//  @Deprecated
//  public static IContributionItem getMenuContributionItem(IActionNode<?> scoutAction, ISwtEnvironment environment) {
//    return getMenuContributionItem(scoutAction, environment, false);
//  }

//  public static IContributionItem getMenuContributionItem(IActionNode<?> scoutAction, ISwtEnvironment environment, boolean disableItem) {
//
//    if (!scoutAction.isVisible()) {
//      return null;
//    }
//
//    if (scoutAction.isSeparator()) {
//      return new Separator();
//    }
//
//    if (scoutAction.hasChildActions()) {
//      IMenuManager manager = new MenuManager(scoutAction.getTextWithMnemonic(), scoutAction.getActionId());
//      //Disable children since menuManager itself can't be disabled
//      boolean disableChilds = !scoutAction.isEnabled() || disableItem;
//      fillMenuManager(scoutAction.getChildActions().toArray(new IActionNode<?>[scoutAction.getChildActionCount()]), manager, environment, disableChilds);
//      return manager;
//    }
//
//    if (scoutAction instanceof IMenu) {
//      return new SwtScoutMenuContributionItem((IMenu) scoutAction, environment);
//    }
//
//    if (scoutAction instanceof ICheckBoxMenu) {
//      Action swtAction = new SwtScoutAction(scoutAction, environment, SWT.CHECK).getSwtAction();
//      if (disableItem) {
//        swtAction.setEnabled(false);
//      }
//      return new ActionContributionItem(swtAction) {
//        @Override
//        public void fill(Menu parent, int index) {
//          super.fill(parent, index);
//        }
//
//        @Override
//        public boolean isDynamic() {
//          return true;
//        }
//
//        @Override
//        public boolean isDirty() {
//          System.out.println(getAction().getText());
//          return super.isDirty();
//        }
//
//        @Override
//        public void update(String propertyName) {
//          super.update(propertyName);
//        }
//      };
//    }
//
//    Action swtAction = new SwtScoutAction(scoutAction, environment).getSwtAction();
//    if (disableItem) {
//      swtAction.setEnabled(false);
//    }
//    return new ActionContributionItem(swtAction) {
//      @Override
//      public boolean isDynamic() {
//        return true;
//      }
//
//      @Override
//      public void update(String propertyName) {
//        MenuItem widget = (MenuItem) getWidget();
//        super.update(propertyName);
//        widget.setText(widget.getText() + "a");
//      }
//    };
//  }

//  public static void fillContextMenu(IMenu[] scoutMenus, Menu menu, ISwtEnvironment environment) {
//    if (scoutMenus == null || scoutMenus.length == 0) {
//      return;
//    }
//
//    List<IActionNode> scoutActionNodes = new LinkedList<IActionNode>();
//    for (IMenu scoutMenu : scoutMenus) {
//      scoutActionNodes.add(scoutMenu);
//    }
//
//    fillContextMenu(scoutActionNodes, menu, environment);
//  }

//  private static void fillContextMenuRec(IMenu scoutActionNode, Menu menu, ISwtEnvironment environment) {
//    if (!scoutActionNode.isVisible()) {
//      return;
//    }
//    if (scoutActionNode.isSeparator()) {
//      new MenuItem(menu, SWT.SEPARATOR);
//    }
//    else if (scoutActionNode.isToggleAction()) {
//      new SwtScoutCheckboxMenu(menu, (ICheckBoxMenu) scoutActionNode, environment);
//
//    }
//    else if (scoutActionNode.getChildActionCount() > 0) {
//      SwtScoutMenuGroup group = new SwtScoutMenuGroup(menu, scoutActionNode, environment);
//      Menu subMenu = new Menu(menu);
//      group.getSwtMenuItem().setMenu(subMenu);
//      List<IMenu> childActions = ActionUtility.visibleNormalizedActions(scoutActionNode.getChildActions());
//      for (IMenu subAction : childActions) {
//        fillContextMenuRec(subAction, subMenu, environment);
//      }
//    }
//    else {
//      new SwtScoutMenuAction(menu, scoutActionNode, environment);
//    }
//  }

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

    return Collections.unmodifiableList(menuList);
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

    return Collections.unmodifiableList(menuList);
  }

  public static List<IMenu> collectMenus(final ICalendar calendar, final boolean emptySpaceActions, final boolean componentActions, ISwtEnvironment swtEnvironment) {
    final List<IMenu> menuList = new ArrayList<IMenu>();
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

    return Collections.unmodifiableList(menuList);
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

//  /**
//   * Removes invisible actions. Also removes leading and trailing separators as well as multiple consecutive separators.
//   *
//   * @since 3.8.1
//   */
//  public static <T extends IActionNode> List<T> cleanup(List<T> scoutActionNodes) {
//    if (scoutActionNodes == null) {
//      return null;
//    }
//
//    List<T> cleanedActions = new ArrayList<T>(scoutActionNodes.size());
//    for (int i = 0; i < scoutActionNodes.size(); i++) {
//      T actionNode = scoutActionNodes.get(i);
//      //Ignore invisible actions
//      if (!actionNode.isVisible()) {
//        continue;
//      }
//      if (actionNode.isSeparator()) {
//        //Ignore leading and trailing separators
//        if (i == 0 || i == scoutActionNodes.size() - 1) {
//          continue;
//        }
//        //Ignore multiple consecutive separators
//        IAction nextVisibleAction = getFirstVisibleAction(scoutActionNodes, i + 1);
//        if (nextVisibleAction == null || nextVisibleAction.isSeparator()) {
//          continue;
//        }
//      }
//
//      cleanedActions.add(actionNode);
//    }
//
//    return cleanedActions;
//  }

//  private static IAction getFirstVisibleAction(List<? extends IActionNode> scoutActionNodes, int startIndex) {
//    if (scoutActionNodes == null) {
//      return null;
//    }
//
//    for (int i = startIndex; i < scoutActionNodes.size(); i++) {
//      IActionNode action = scoutActionNodes.get(i);
//      if (action.isVisible()) {
//        return action;
//      }
//    }
//
//    return null;
//  }

}
