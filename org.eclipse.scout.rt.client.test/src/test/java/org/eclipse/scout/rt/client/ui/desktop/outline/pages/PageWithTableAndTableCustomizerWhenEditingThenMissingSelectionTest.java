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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.AbstractTableCustomizer;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
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
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PageWithTableAndTableCustomizerWhenEditingThenMissingSelectionTest {
  private static boolean tableCustomizerSerialDataIsRandom;

  @Before
  public void setUp() {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    Outline outline = new Outline();
    desktop.setAvailableOutlines(Collections.singletonList(outline));
    desktop.activateOutline(outline);
  }

  @Test
  public void testWithUnchangedTableCustomizer() throws Exception {
    tableCustomizerSerialDataIsRandom = false;
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.activateFirstPage();
    PageWithTable page = (PageWithTable) desktop.getOutline().getActivePage();
    PageWithTable.Table table = page.getTable();
    //
    table.selectRow(0);
    assertSelection(table, CollectionUtility.arrayList(1));
    //
    table.selectRow(1);
    assertSelection(table, CollectionUtility.arrayList(2));
    //
    table.getMenuByClass(PageWithTable.Table.EditAccountMenu.class).doAction();
    assertSelection(table, CollectionUtility.arrayList(2));
  }

  @Test
  public void testWithChangedTableCustomizer() throws Exception {
    tableCustomizerSerialDataIsRandom = true;
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.activateFirstPage();
    PageWithTable page = (PageWithTable) desktop.getOutline().getActivePage();
    PageWithTable.Table table = page.getTable();
    //
    table.selectRow(0);
    assertSelection(table, CollectionUtility.arrayList(1));
    //
    table.selectRow(1);
    assertSelection(table, CollectionUtility.arrayList(2));
    //
    table.getMenuByClass(PageWithTable.Table.EditAccountMenu.class).doAction();
    assertSelection(table, CollectionUtility.arrayList(2));
  }

  private static void assertSelection(PageWithTable.Table table, List<Integer> expectedIds) {
    Assert.assertTrue(CollectionUtility.equalsCollection(expectedIds, table.getIdColumn().getSelectedValues()));
  }

  public static class Outline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    @Override
    protected void execInitTable() {
      getTable().setTableCustomizer(new AbstractTableCustomizer() {
        private byte seq;

        @Override
        public void setSerializedData(byte[] data) {
          //nop
        }

        @Override
        public byte[] getSerializedData() {
          seq++;
          if (tableCustomizerSerialDataIsRandom) {
            return new byte[]{seq, 0};
          }
          return new byte[]{1, 2, 3, 4};
        }

        @Override
        public String getPreferencesKey() {
          return null;
        }
      });
      registerDataChangeListener("Account");
    }

    @Override
    protected void execLoadData(SearchFilter filter) {
      importTableData(new Object[][]{
          new Object[]{1, "Account 1"},
          new Object[]{2, "Account 2"},
          new Object[]{3, "Account 3"},});
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
        protected Set<? extends IMenuType> getConfiguredMenuTypes() {
          return CollectionUtility.hashSet(TableMenuType.SingleSelection);
        }

        @Override
        protected void execAction() {
          System.out.println("Edit: change data 'Account'");
          dataChanged("Account");
        }
      }

    }
  }
}
