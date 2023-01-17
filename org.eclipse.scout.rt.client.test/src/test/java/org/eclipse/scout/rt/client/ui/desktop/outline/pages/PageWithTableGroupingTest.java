/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.eclipse.scout.rt.client.ui.desktop.outline.pages.PageWithTableGroupingTestHelper.assertGroupingState;

import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Handling of table column grouping
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PageWithTableGroupingTest {

  private static PageWithTable.Table table;
  private static IColumn b1, b2, b3;

  @Before
  public void setup() {
    table = (PageWithTable.Table) PageWithTableGroupingTestHelper.setupDesktop(new PageWithTableOutline());
    b1 = table.getBit1Column();
    b2 = table.getBit2Column();
    b3 = table.getBit3Column();
  }

  @Test
  public void testGroupingBasic() {

    //reset columns
    table.resetColumns();

    //initially, only the configured columns are grouped.
    assertGroupingState(table, CollectionUtility.arrayList(b3), CollectionUtility.arrayList(b2, b1));

    //additional grouping
    table.getUIFacade().fireHeaderGroupFromUI(b2, true, true);
    assertGroupingState(table, CollectionUtility.arrayList(b3, b2), CollectionUtility.arrayList(b1));

    //initial configured grouping column is removed:
    //this also removes it as a sort column.
    table.getUIFacade().fireGroupColumnRemovedFromUI(b3);
    assertGroupingState(table, CollectionUtility.arrayList(b2), CollectionUtility.arrayList(b1));

    //add another grouping column:
    table.getUIFacade().fireHeaderGroupFromUI(b1, true, true);
    assertGroupingState(table, CollectionUtility.arrayList(b2, b1), CollectionUtility.<IColumn> emptyArrayList());

  }

  @Test
  public void testGroupingVisibility() {

    //reset columns
    table.resetColumns();

    //additional grouping
    table.getUIFacade().fireHeaderGroupFromUI(b2, true, true);

    //make a grouped column invisible:
    //this removes grouping and sorting
    b3.setVisible(false);
    assertGroupingState(table, CollectionUtility.arrayList(b2), CollectionUtility.arrayList(b1));

    //an invisible column cannot be grouped:
    table.getUIFacade().fireHeaderGroupFromUI(b3, true, true);
    assertGroupingState(table, CollectionUtility.arrayList(b2), CollectionUtility.arrayList(b1));
  }

  @Test
  public void testGroupingPreferences() {

    //reset columns
    table.resetColumns();

    //additional grouping
    table.getUIFacade().fireHeaderGroupFromUI(b2, true, true);
    assertGroupingState(table, CollectionUtility.arrayList(b3, b2), CollectionUtility.arrayList(b1));

    //reset desktop and re-create new outline
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.activateOutline((IOutline) null);
    setup();

    assertGroupingState(table, CollectionUtility.arrayList(b3, b2), CollectionUtility.arrayList(b1));

  }

  public static class PageWithTableOutline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    @Override
    protected void execLoadData(SearchFilter filter) {
      importTableData(new Object[][]{
          new Object[]{0, 0, 0, 0},
          new Object[]{1, 1, 0, 6},
          new Object[]{0, 1, 1, 3},
          new Object[]{1, 0, 0, 4},
          new Object[]{1, 1, 1, 7},
          new Object[]{1, 0, 1, 5},
          new Object[]{0, 1, 0, 2},
          new Object[]{0, 0, 1, 1}
      });
    }

    public class Table extends AbstractTable {

      public Bit3Column getBit3Column() {
        return getColumnSet().getColumnByClass(Bit3Column.class);
      }

      public Bit2Column getBit2Column() {
        return getColumnSet().getColumnByClass(Bit2Column.class);
      }

      public Bit1Column getBit1Column() {
        return getColumnSet().getColumnByClass(Bit1Column.class);
      }

      public ValueColumn getValueColumn() {
        return getColumnSet().getColumnByClass(ValueColumn.class);
      }

      @Order(10)
      public class Bit3Column extends AbstractIntegerColumn {
        @Override
        protected String getConfiguredHeaderText() {
          return "Bit 3";
        }

        @Override
        protected int getConfiguredSortIndex() {
          return 1;
        }

        @Override
        protected boolean getConfiguredGrouped() {
          return true;
        }

      }

      @Order(20)
      public class Bit2Column extends AbstractIntegerColumn {
        @Override
        protected String getConfiguredHeaderText() {
          return "Bit 2";
        }

        @Override
        protected int getConfiguredSortIndex() {
          return 2;
        }
      }

      @Order(30)
      public class Bit1Column extends AbstractIntegerColumn {
        @Override
        protected String getConfiguredHeaderText() {
          return "Bit 1";
        }

        @Override
        protected int getConfiguredSortIndex() {
          return 3;
        }

      }

      @Order(40)
      public class ValueColumn extends AbstractIntegerColumn {
        @Override
        protected String getConfiguredHeaderText() {
          return "Value";
        }
      }

    }

  }

}
