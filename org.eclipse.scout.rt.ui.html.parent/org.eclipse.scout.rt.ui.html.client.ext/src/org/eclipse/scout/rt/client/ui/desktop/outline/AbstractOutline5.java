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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.AbstractExtensibleOutline;

public abstract class AbstractOutline5 extends AbstractExtensibleOutline {

  protected AbstractOutline5() {
    super();
  }

  protected AbstractOutline5(boolean callInitialzier) {
    super(callInitialzier);
  }

  @Override
  protected List<IMenu> computeInheritedMenusOfPage(IPage activePage) {
    List<IMenu> menus = new ArrayList<IMenu>();
    if (activePage instanceof IPageWithTable<?>) {
      // in case of a page with table the empty space actions of the table will be added to the context menu of the tree.
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) activePage;
      if (pageWithTable.isShowEmptySpaceMenus()) {
        ITable table = pageWithTable.getTable();
        List<IMenu> emptySpaceMenus = ActionUtility.getActions(table.getMenus(),
            ActionUtility.createMenuFilterMenuTypes(CollectionUtility.<IMenuType> hashSet(TableMenuType.EmptySpace), false));
        if (emptySpaceMenus.size() > 0) {
//          menus.add(new MenuSeparator());
          for (IMenu menu : emptySpaceMenus) {
            menus.add(menu);
          }
        }
      }
    }

    // in case of a page with nodes add the single selection menus of its parent table for the current node/row.
    IPage parentPage = activePage.getParentPage();
    if (parentPage instanceof IPageWithTable<?>) {
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) parentPage;
      ITableRow row = pageWithTable.getTableRowFor(activePage);
      ITable table = pageWithTable.getTable();
      if (row != null) {
        table.getUIFacade().setSelectedRowsFromUI(CollectionUtility.arrayList(row));
        List<IMenu> parentTableMenus = ActionUtility.getActions(table.getContextMenu().getChildActions(),
            ActionUtility.createMenuFilterMenuTypes(CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection), false));
        if (parentTableMenus.size() > 0) {
//          menus.add(new MenuSeparator());
          for (IMenu menu : parentTableMenus) {
            menus.add(menu);
          }
        }
      }
    }

    return menus;
  }

}
