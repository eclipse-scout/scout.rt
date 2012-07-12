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
package org.eclipse.scout.rt.client.mobile.ui.action;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.table.autotable.MainBoxActionButton;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

/**
 * @since 3.9.0
 */
public class ActionButtonBarUtility {

  public static List<IMenu> convertButtonsToActions(IButton[] buttons) {
    List<IMenu> menuList = new LinkedList<IMenu>();
    for (IButton button : buttons) {
      IMenu action = convertButtonToAction(button);
      if (action != null) {
        menuList.add(action);
      }
    }

    return menuList;
  }

  public static IMenu convertButtonToAction(IButton button) {
    if (button == null) {
      return null;
    }

    return new ButtonWrappingAction(button);
  }

  /**
   * If there are empty space menus distribute the row menus so that the menus alternate and the most important are on
   * top, starting with a empty space menu
   */
  public static void distributeRowActions(List<IMenu> menuList, IMenu[] emptySpaceMenus, List<IMenu> rowMenuList) {
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

  public static List<IButton> convertyTableRowActionsToButtons(ITable table) {
    List<IButton> buttons = new LinkedList<IButton>();
    if (table == null) {
      return buttons;
    }

    IMenu[] actions = table.getUIFacade().fireRowPopupFromUI();
    for (IMenu action : actions) {
      if (!action.isSeparator()) {
        MainBoxActionButton button = new MainBoxActionButton(action);
        buttons.add(button);
      }
    }

    return buttons;
  }

}
