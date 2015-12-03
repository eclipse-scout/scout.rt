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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A menu opened in empty space should not consider any selection on a row. Thus the menu should open anyway (not
 * querying the selection status of any row).
 * <p>
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PageWithTable6Test {

  @Test
  public void testMenus() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    Outline outline = new Outline();
    desktop.setAvailableOutlines(Collections.singletonList(outline));
    desktop.activateOutline(outline);
    desktop.activateFirstPage();
    PageWithTable page = (PageWithTable) desktop.getOutline().getActivePage();
    PageWithTable.Table table = page.getTable();
    //
    table.selectRow(null);
    assertMenus(table, new String[]{"New Account"});
    //
    table.selectRow(0);
    assertMenus(table, new String[]{"Edit Account"});
    //
    table.selectRow(1);
    assertMenus(table, new String[]{});
    //
    table.selectAllRows();
    assertMenus(table, new String[]{});
    //
    table.selectRow(1);
    assertMenus(table, new String[]{});

    table.selectRow(0);
    assertMenus(table, new String[]{"Edit Account"});
    //
    table.deselectAllRows();
    assertMenus(table, new String[]{"New Account"});
  }

  private static void assertMenus(PageWithTable.Table table, String[] expectedMenus) {
    List<String> actualMenus = new ArrayList<String>();
    for (IMenu m : ActionUtility.normalizedActions(table.getContextMenu().getChildActions(), ActionUtility.createMenuFilterMenuTypes(table.getContextMenu().getCurrentMenuTypes(), true))) {
      if (m.isEnabled()) {
        actualMenus.add(m.getText());
      }
    }
    assertArrayEquals(expectedMenus, actualMenus.toArray(new String[0]));
  }

  public static class Outline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    @Override
    protected void execLoadData(SearchFilter filter) {
      importTableData(new Object[][]{
          new Object[]{1, "Enabled Account"},
          new Object[]{2, "Disabled Account"},});
    }

    @Override
    protected void execPopulateTable() {
      super.execPopulateTable();
      getTable().findRowByKey(CollectionUtility.arrayList((Object) Integer.valueOf(2))).setEnabled(false);
    }

    public class Table extends AbstractTable {

      public IdColumn getIdColumn() {
        return getColumnSet().getColumnByClass(IdColumn.class);
      }

      public AccountColumn getAccountColumn() {
        return getColumnSet().getColumnByClass(AccountColumn.class);
      }

      @Order(10)
      public class IdColumn extends AbstractIntegerColumn {
        @Override
        protected boolean getConfiguredPrimaryKey() {
          return true;
        }
      }

      @Order(20)
      public class AccountColumn extends AbstractStringColumn {
        @Override
        protected String getConfiguredHeaderText() {
          return "Account";
        }

        @Override
        protected int getConfiguredWidth() {
          return 300;
        }
      }

      @Order(10)
      public class NewAccountMenu extends AbstractMenu {
        @Override
        protected String getConfiguredText() {
          return "New Account";
        }

        @Override
        protected Set<? extends IMenuType> getConfiguredMenuTypes() {
          return CollectionUtility.hashSet(TableMenuType.EmptySpace);
        }
      }

      @Order(10)
      public class EditAccountMenu extends AbstractMenu {
        @Override
        protected String getConfiguredText() {
          return "Edit Account";
        }

        @Override
        protected Set<? extends IMenuType> getConfiguredMenuTypes() {
          return CollectionUtility.hashSet(TableMenuType.SingleSelection);
        }
      }
    }
  }
}
