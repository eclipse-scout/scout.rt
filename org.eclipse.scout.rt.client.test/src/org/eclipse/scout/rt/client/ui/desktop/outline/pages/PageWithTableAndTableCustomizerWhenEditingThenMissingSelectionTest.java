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

import java.util.Collection;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.AbstractTableCustomizer;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A page with a table and {@link ITableCustomizer}.
 * <p>
 * An edit menu changes data and publishes using {@link IPage#dataChanged(Object...)}.
 * <p>
 * This creates a local bookmark and activates it.
 * <p>
 * bug: When doing so the table selection disappears since {@link ITable#resetColumnConfiguration()} discards all rows
 * and selection from BookmarkUtility#activate
 */
@RunWith(ScoutClientTestRunner.class)
public class PageWithTableAndTableCustomizerWhenEditingThenMissingSelectionTest {
  private static boolean tableCustomizerSerialDataIsRandom;

  @Before
  public void setUp() {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    Outline outline = new Outline();
    desktop.setAvailableOutlines(new IOutline[]{outline});
    desktop.setOutline(outline);
  }

  @Test
  public void testWithUnchangedTableCustomizer() throws Exception {
    tableCustomizerSerialDataIsRandom = false;
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    PageWithTable page = (PageWithTable) desktop.getOutline().getActivePage();
    PageWithTable.Table table = page.getTable();
    //
    table.selectRow(0);
    assertSelection(table, new Integer[]{1});
    //
    table.selectRow(1);
    assertSelection(table, new Integer[]{2});
    //
    table.getMenu(PageWithTable.Table.EditAccountMenu.class).doAction();
    assertSelection(table, new Integer[]{2});
  }

  @Test
  public void testWithChangedTableCustomizer() throws Exception {
    tableCustomizerSerialDataIsRandom = true;
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    PageWithTable page = (PageWithTable) desktop.getOutline().getActivePage();
    PageWithTable.Table table = page.getTable();
    //
    table.selectRow(0);
    assertSelection(table, new Integer[]{1});
    //
    table.selectRow(1);
    assertSelection(table, new Integer[]{2});
    //
    table.getMenu(PageWithTable.Table.EditAccountMenu.class).doAction();
    assertSelection(table, new Integer[]{2});
  }

  private static void assertSelection(PageWithTable.Table table, Integer[] expectedIds) {
    Assert.assertArrayEquals(expectedIds, table.getIdColumn().getSelectedValues());
  }

  public static class Outline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    @Override
    protected void execInitPage() throws ProcessingException {
      getTable().setTableCustomizer(new AbstractTableCustomizer() {
        private byte seq;

        @Override
        public void setSerializedData(byte[] data) throws ProcessingException {
          //nop
        }

        @Override
        public byte[] getSerializedData() throws ProcessingException {
          seq++;
          if (tableCustomizerSerialDataIsRandom) {
            return new byte[]{seq, 0};
          }
          return new byte[]{1, 2, 3, 4};
        }
      });
      registerDataChangeListener("Account");
    }

    @Override
    protected Object[][] execLoadTableData(SearchFilter filter) throws ProcessingException {
      return new Object[][]{
          new Object[]{1, "Account 1"},
          new Object[]{2, "Account 2"},
          new Object[]{3, "Account 3"},};
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
        protected void execAction() throws ProcessingException {
          System.out.println("Edit: change data 'Account'");
          dataChanged("Account");
        }
      }

    }
  }
}
