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
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Handling of table sorting with user sort columns, configured sort columns and ui preferences store columns
 */
@RunWith(ScoutClientTestRunner.class)
public class PageWithTable3Test {

  @Test
  public void testSorting() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    PageWithTableOutline outline = new PageWithTableOutline();
    desktop.setAvailableOutlines(new IOutline[]{outline});
    desktop.setOutline(outline);
    PageWithTable page = (PageWithTable) desktop.getOutline().getActivePage();
    PageWithTable.Table table = page.getTable();
    table.resetDisplayableColumns();
    //
    //load table with configured sort columns
    assertArrayEquals(new Integer[]{7, 6, 5, 4, 3, 2, 1, 0}, table.getValueColumn().getValues());
    //user sorts value column asc (all other columns lost their sort index resp. their sort is now explicit=false)
    table.getUIFacade().fireHeaderSortFromUI(table.getValueColumn(), false);
    assertSortState(table, new Integer[]{0, 1, 2, 3, 4, 5, 6, 7}, new Integer[]{0});
    //
    //reset desktop and re-create new outline
    desktop.setOutline((IOutline) null);
    outline = new PageWithTableOutline();
    desktop.setAvailableOutlines(new IOutline[]{outline});
    desktop.setOutline(outline);
    page = (PageWithTable) desktop.getOutline().getActivePage();
    table = page.getTable();
    assertSortState(table, new Integer[]{0, 1, 2, 3, 4, 5, 6, 7}, new Integer[]{0});
  }

  private static void assertSortState(PageWithTable.Table table, Integer[] expectedValues, Integer[] expectedExplicitSortIndices) {
    /*
    System.out.println("--");
    System.out.println(Arrays.toString(table.getValueColumn().getValues()));
    for (IColumn<?> c : table.getColumns()) {
      System.out.println("col " + c.getClass().getSimpleName() + ": " + c.isSortActive() + ", " + c.isSortExplicit() + ", " + c.getSortIndex() + " " + c.isSortAscending());
    }
    */
    ArrayList<Integer> actualExplicitSortIndices = new ArrayList<Integer>();
    for (IColumn<?> c : table.getColumns()) {
      if (c.isSortActive() && c.isSortExplicit()) {
        actualExplicitSortIndices.add(c.getColumnIndex());
      }
    }
    assertArrayEquals(expectedValues, table.getValueColumn().getValues());
    assertArrayEquals(expectedExplicitSortIndices, actualExplicitSortIndices.toArray());
  }

  public static class PageWithTableOutline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    @Override
    protected Object[][] execLoadTableData(SearchFilter filter) throws ProcessingException {
      return new Object[][]{
          new Object[]{1, 0, 0, 1},
          new Object[]{3, 0, 1, 1},
          new Object[]{5, 1, 0, 1},
          new Object[]{7, 1, 1, 1},
          new Object[]{0, 0, 0, 0},
          new Object[]{2, 0, 1, 0},
          new Object[]{4, 1, 0, 0},
          new Object[]{6, 1, 1, 0}
      };
    }

    public class Table extends AbstractTable {

      public ValueColumn getValueColumn() {
        return getColumnSet().getColumnByClass(ValueColumn.class);
      }

      public Bit2Column getBit2Column() {
        return getColumnSet().getColumnByClass(Bit2Column.class);
      }

      public Bit1Column getBit1Column() {
        return getColumnSet().getColumnByClass(Bit1Column.class);
      }

      public Bit0Column getBit0Column() {
        return getColumnSet().getColumnByClass(Bit0Column.class);
      }

      @Order(10)
      public class ValueColumn extends AbstractIntegerColumn {
        @Override
        protected String getConfiguredHeaderText() {
          return "Value";
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
          return 0;
        }

        @Override
        protected boolean getConfiguredSortAscending() {
          return false;
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
          return 1;
        }

        @Override
        protected boolean getConfiguredSortAscending() {
          return false;
        }
      }

      @Order(40)
      public class Bit0Column extends AbstractIntegerColumn {
        @Override
        protected String getConfiguredHeaderText() {
          return "Bit 0";
        }

        @Override
        protected int getConfiguredSortIndex() {
          return 2;
        }

        @Override
        protected boolean getConfiguredSortAscending() {
          return false;
        }
      }

    }
  }
}
