/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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
import java.util.Collection;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A menu opened in empty space should not consider any selection on a row.
 * Thus the menu should open anyway (not querying the selection status of any row).
 * <p>
 */
@RunWith(ScoutClientTestRunner.class)
public class PageWithTable6Test {

  @Test
  public void testMenus() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    Outline outline = new Outline();
    desktop.setAvailableOutlines(new IOutline[]{outline});
    desktop.setOutline(outline);
    PageWithTable page = (PageWithTable) desktop.getOutline().getActivePage();
    PageWithTable.Table table = page.getTable();
    //
    table.selectRow(null);
    assertEmptySpaceMenus(table, new String[]{"New Account"});
    assertRowMenus(table, new String[]{});
    //
    table.selectRow(0);
    assertEmptySpaceMenus(table, new String[]{"New Account"});
    assertRowMenus(table, new String[]{"Edit Account"});
    //
    table.selectRow(1);
    assertEmptySpaceMenus(table, new String[]{"New Account"});
    assertRowMenus(table, new String[]{});
    //
    table.selectAllRows();
    assertEmptySpaceMenus(table, new String[]{"New Account"});
    assertRowMenus(table, new String[]{});
    //
    table.selectRow(1);
    assertEmptySpaceMenus(table, new String[]{"New Account"});
    assertRowMenus(table, new String[]{});
    //
    table.selectRow(0);
    assertEmptySpaceMenus(table, new String[]{"New Account"});
    assertRowMenus(table, new String[]{"Edit Account"});
    //
    table.deselectAllRows();
    assertEmptySpaceMenus(table, new String[]{"New Account"});
    assertRowMenus(table, new String[]{});
  }

  private static void assertEmptySpaceMenus(PageWithTable.Table table, String[] expectedMenus) {
    ArrayList<String> actualMenus = new ArrayList<String>();
    for (IMenu m : table.getUIFacade().fireEmptySpacePopupFromUI()) {
      if (m.isVisible() && m.isEnabled()) {
        actualMenus.add(m.getText());
      }
    }
    assertArrayEquals(expectedMenus, actualMenus.toArray(new String[0]));
  }

  private static void assertRowMenus(PageWithTable.Table table, String[] expectedMenus) {
    ArrayList<String> actualMenus = new ArrayList<String>();
    for (IMenu m : table.getUIFacade().fireRowPopupFromUI()) {
      if (m.isVisible() && m.isEnabled()) {
        actualMenus.add(m.getText());
      }
    }
    assertArrayEquals(expectedMenus, actualMenus.toArray(new String[0]));
  }

  public static class Outline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    @Override
    protected Object[][] execLoadTableData(SearchFilter filter) throws ProcessingException {
      return new Object[][]{
          new Object[]{1, "Enabled Account"},
          new Object[]{2, "Disabled Account"},};
    }

    @Override
    protected void execPopulateTable() throws ProcessingException {
      super.execPopulateTable();
      getTable().findRowByKey(new Object[]{2}).setEnabled(false);
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
        protected boolean getConfiguredSingleSelectionAction() {
          return false;
        }

        @Override
        protected boolean getConfiguredEmptySpaceAction() {
          return true;
        }
      }

      @Order(10)
      public class EditAccountMenu extends AbstractMenu {
        @Override
        protected String getConfiguredText() {
          return "Edit Account";
        }

        @Override
        protected boolean getConfiguredSingleSelectionAction() {
          return true;
        }

        @Override
        protected boolean getConfiguredEmptySpaceAction() {
          return false;
        }
      }

    }
  }
}
