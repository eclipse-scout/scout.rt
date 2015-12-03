/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.action;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.mobile.ui.form.IMobileAction;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.AutoLeafPageWithNodes;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IActionFilter;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * @since 3.9.0
 */
public class ActionButtonBarUtility {

  public static List<IMobileAction> convertButtonsToActions(List<IButton> buttons) {
    List<IMobileAction> menuList = new ArrayList<IMobileAction>(buttons.size());
    for (IButton button : buttons) {
      IMobileAction action = convertButtonToAction(button);
      if (action != null) {
        menuList.add(action);
      }
    }

    return menuList;
  }

  public static IMobileAction convertButtonToAction(IButton button) {
    if (button == null) {
      return null;
    }

    ButtonWrappingAction mAction = new ButtonWrappingAction(button);
    mAction.initAction();
    return mAction;
  }

  /**
   * If there are empty space menus distribute the row menus so that the menus alternate and the most important are on
   * top, starting with a empty space menu
   */
  public static void distributeRowActions(List<IMenu> menuList, List<IMenu> emptySpaceMenus, List<IMenu> rowMenuList) {
    if (emptySpaceMenus == null) {
      return;
    }

    for (IMenu emptySpaceMenu : emptySpaceMenus) {
      if (rowMenuList.size() == 0) {
        break;
      }

      int index = menuList.indexOf(emptySpaceMenu) + 1;
      IMenu rowMenu = rowMenuList.get(0);
      menuList.add(index, rowMenu);
      rowMenuList.remove(rowMenu);
    }
  }

  /**
   * Fetches the actions of the given page (tree node and table row menus).
   */
  public static List<IMenu> fetchPageActions(IPage<?> page) {
    List<IMenu> pageActions = new LinkedList<IMenu>();
    if (page.getTree() != null) {
      List<IMenu> menusForPage = page.getOutline().getMenusForPage(page);
      pageActions.addAll(menusForPage);
      if (page instanceof AutoLeafPageWithNodes) {
        //AutoLeafPage has no parent so the table row actions are not fetched by the regular way (see AbstractOutline#P_OutlineListener).
        //Instead we directly fetch the table row actions
        IActionFilter actionFilter = ActionUtility.createMenuFilterMenuTypes(CollectionUtility.hashSet(TableMenuType.SingleSelection), true);
        pageActions.addAll(ActionUtility.getActions(((AutoLeafPageWithNodes) page).getTableRow().getTable().getMenus(), actionFilter));
      }
    }

    return pageActions;
  }
}
