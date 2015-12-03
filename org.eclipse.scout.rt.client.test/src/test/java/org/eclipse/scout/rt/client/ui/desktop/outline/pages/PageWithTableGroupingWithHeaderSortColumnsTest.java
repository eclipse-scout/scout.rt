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

import static org.eclipse.scout.rt.client.ui.desktop.outline.pages.PageWithTableGroupingTestHelper.assertGroupingState;

import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
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
public class PageWithTableGroupingWithHeaderSortColumnsTest {

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
  public void testGroupingWithHeaderSortColumn() throws Exception {

    //reset columns
    table.resetColumns();

    //initially, no column should be grouped,
    //since the configured grouping column is invalid.
    assertGroupingState(table, CollectionUtility.<IColumn> emptyArrayList(), CollectionUtility.arrayList(b3, b2, b1));

    //group by b2 not possible, it cannot be made first sort column
    table.getUIFacade().fireHeaderGroupFromUI(b2, false, true);
    assertGroupingState(table, CollectionUtility.<IColumn> emptyArrayList(), CollectionUtility.arrayList(b3, b2, b1));

    //group by b3 is possible:
    table.getUIFacade().fireHeaderGroupFromUI(b3, false, true);
    assertGroupingState(table, CollectionUtility.arrayList(b3), CollectionUtility.arrayList(b2, b1));

    //now group by b2 is also possible.
    table.getUIFacade().fireHeaderGroupFromUI(b2, true, true);
    assertGroupingState(table, CollectionUtility.arrayList(b3, b2), CollectionUtility.arrayList(b1));

  }

  @Test
  public void testGroupingInvisibleHeaderSortColumn() throws Exception {

    //reset columns
    table.resetColumns();

    //group by all columns
    table.getUIFacade().fireHeaderGroupFromUI(b3, false, true);
    table.getUIFacade().fireHeaderGroupFromUI(b2, true, true);
    table.getUIFacade().fireHeaderGroupFromUI(b1, true, true);

    //make b2 not-displayable.
    b2.setDisplayable(false);
    assertGroupingState(table, CollectionUtility.arrayList(b3), CollectionUtility.arrayList(b2));

    //invisible column gannot be grouped!
    table.getUIFacade().fireHeaderGroupFromUI(b2, false, true);
    assertGroupingState(table, CollectionUtility.arrayList(b3), CollectionUtility.arrayList(b2));
  }

  @Test
  public void testGroupingInvisibleFirstHeaderSortColumn() throws Exception {

    //reset columns
    table.resetColumns();

    //group by all columns
    table.getUIFacade().fireHeaderGroupFromUI(b3, false, true);
    table.getUIFacade().fireHeaderGroupFromUI(b2, true, true);
    table.getUIFacade().fireHeaderGroupFromUI(b1, true, true);

    //make b3 not-displayable.
    b3.setDisplayable(false);
    assertGroupingState(table, CollectionUtility.<IColumn> emptyArrayList(), CollectionUtility.arrayList(b3, b2));

    //invisible column gannot be grouped!
    table.getUIFacade().fireHeaderGroupFromUI(b3, false, true);
    assertGroupingState(table, CollectionUtility.<IColumn> emptyArrayList(), CollectionUtility.arrayList(b3, b2));
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
        protected boolean getConfiguredAlwaysIncludeSortAtBegin() {
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
        protected boolean getConfiguredAlwaysIncludeSortAtBegin() {
          return true;
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

        @Override
        protected boolean getConfiguredGrouped() {
          //by choice! this is an invalid config.
          return true;
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
