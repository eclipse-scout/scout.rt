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
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractTable}
 */
@RunWith(ScoutClientTestRunner.class)
public class TableTest {

  /**
   * Test that deleted tableRows can be discarded:
   * New inserted rows are automatically discarded.
   */
  @Test
  public void testDeleteAllNew() throws Exception {
    //Bug 361985
    P_Table table = createTable(ITableRow.STATUS_INSERTED);

    table.deleteAllRows();
    assertRowCount(0, 0, table);
  }

  /**
   * Test that deleted tableRows can be discarded:
   * NON_CHANGED rows goes in deletedRows list. (because the table has AutoDiscardOnDelete = false)
   * discard these rows.
   */
  @Test
  public void testDeleteAllAndDiscardFirst() throws Exception {
    //Bug 361985
    P_Table table = createTable(ITableRow.STATUS_NON_CHANGED);

    table.deleteAllRows();
    assertRowCount(0, 2, table);

    List<ITableRow> deletedRows = table.getDeletedRows();
    assertEquals(2, deletedRows.size());
    asssertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows.get(0));
    asssertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows.get(1));

    table.discardDeletedRow(deletedRows.get(0));
    assertRowCount(0, 1, table);
    asssertNoTable(deletedRows.get(0));
    asssertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows.get(1));
  }

  /**
   * Test that deleted tableRows can be discarded:
   * NON_CHANGED rows goes in deletedRows list. (because the table has AutoDiscardOnDelete = false)
   * delete a row and discard it.
   */
  @Test
  public void testDeleteAndDiscard() throws Exception {
    //Bug 361985
    P_Table table = createTable(ITableRow.STATUS_NON_CHANGED);

    ITableRow row1 = table.getRow(0);
    ITableRow row2 = table.getRow(1);
    table.deleteRow(row1);
    assertRowCount(1, 1, table);

    asssertStatusAndTable(table, ITableRow.STATUS_DELETED, row1);
    asssertStatusAndTable(table, ITableRow.STATUS_NON_CHANGED, row2);

    table.discardDeletedRow(row1);
    assertRowCount(1, 0, table);
    asssertNoTable(row1);
    asssertStatusAndTable(table, ITableRow.STATUS_NON_CHANGED, row2);

  }

  /**
   * Test that deleted tableRows can be discarded:
   * NON_CHANGED rows goes in deletedRows list. (because the table has AutoDiscardOnDelete = false)
   * discard these deletedRows with discardAllDeletedRows.
   */
  @Test
  public void testDeleteAllAndDiscardAll() throws Exception {
    //Bug 361985
    P_Table table = createTable(ITableRow.STATUS_NON_CHANGED);

    table.deleteAllRows();
    assertRowCount(0, 2, table);

    List<ITableRow> deletedRows = table.getDeletedRows();
    assertEquals(2, deletedRows.size());
    asssertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows.get(0));
    asssertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows.get(1));

    table.discardAllDeletedRows();
    assertRowCount(0, 0, table);
    asssertNoTable(deletedRows.get(0));
    asssertNoTable(deletedRows.get(1));
  }

  /**
   * Test that deleted tableRows can be discarded:
   * AutoDiscardOnDelete = true. Rows are automatically discarded (STATUS_INSERTED, or .STATUS_NON_CHANGED)
   */
  @Test
  public void testDeleteAllAutoDiscard() throws Exception {
    //Bug 361985
    //test with STATUS_INSERTED and AutoDiscardOnDelete:
    P_Table table = createTable(ITableRow.STATUS_INSERTED);
    table.setAutoDiscardOnDelete(true);

    table.deleteAllRows();
    assertRowCount(0, 0, table);

    //test with STATUS_NON_CHANGED:
    P_Table table2 = createTable(ITableRow.STATUS_NON_CHANGED);
    table2.setAutoDiscardOnDelete(true);

    table2.deleteAllRows();
    assertRowCount(0, 0, table2);
  }

  /**
   * Test that deleted tableRows can be discarded:
   * discard all rows => no deleted row.
   */
  @Test
  public void testDiscardAll() throws Exception {
    //Bug 361985
    //test with STATUS_INSERTED and AutoDiscardOnDelete:
    P_Table table = createTable(ITableRow.STATUS_INSERTED);

    table.discardAllRows();
    assertRowCount(0, 0, table);

    //test with STATUS_NON_CHANGED:
    P_Table table2 = createTable(ITableRow.STATUS_NON_CHANGED);

    table2.discardAllRows();
    assertRowCount(0, 0, table2);
  }

  /**
   * Test of {@link AbstractTable#sort()}. If {@link AbstractTable#isSortEnabled()} is false (e.g. sort not enabled),
   * rows will stay in the order they were added to the table.
   */
  @Test
  public void testSortNotEnabled() throws Exception {
    P_Table table = new P_Table();
    table.initTable();

    table.setSortEnabled(false);

    //ensure table state:
    assertEquals("SortEnabled", false, table.isSortEnabled());
    assertEquals("FirstColumn - sort index", -1, table.getFirstColumn().getSortIndex());
    assertEquals("SecondColumn - sort index", -1, table.getSecondColumn().getSortIndex());
    assertEquals("ThirdColumn - sort index", 0, table.getThridColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThridColumn().getInitialSortIndex());
    assertEquals("ThirdColumn - initial alwaysIncludeSortAtBegin", true, table.getThridColumn().isInitialAlwaysIncludeSortAtBegin());

    //fill the table and sort:
    fillTable(table);
    table.sort();

    //check the sort order of the rows:
    List<ITableRow> rows = table.getRows();
    assertEquals("rows size", 5, rows.size());
    int i = 0;
    assertRowEquals(10, "Lorem", 1, table, rows, i++);
    assertRowEquals(1, "A Total", 2, table, rows, i++);
    assertRowEquals(30, "Ipsum", 1, table, rows, i++);
    assertRowEquals(25, "Lorem", 1, table, rows, i++);
    assertRowEquals(20, "Ipsum", 1, table, rows, i++);
  }

  /**
   * Test of {@link AbstractTable#sort()}. Sorted by:
   * - 1. ThridColumn (defined with AlwaysIncludeSortAtBegin in the column)
   * - 2. FirstColumn descending.
   */
  @Test
  public void testSortFirstColumn() throws Exception {
    P_Table table = new P_Table();
    table.initTable();

    table.setSortEnabled(true);
    table.getColumnSet().setSortColumn(table.getFirstColumn(), false, 5);

    //ensure table state:
    assertEquals("SortEnabled", true, table.isSortEnabled());
    assertEquals("FirstColumn - sort index", 1, table.getFirstColumn().getSortIndex());
    assertEquals("FirstColumn - sort ascending", false, table.getFirstColumn().isSortAscending());
    assertEquals("SecondColumn - sort index", -1, table.getSecondColumn().getSortIndex());
    assertEquals("ThirdColumn - sort index", 0, table.getThridColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThridColumn().getInitialSortIndex());
    assertEquals("ThirdColumn - initial alwaysIncludeSortAtBegin", true, table.getThridColumn().isInitialAlwaysIncludeSortAtBegin());
    assertEquals("ColumnSet PermanentHeadSortColumns size", 1, table.getColumnSet().getPermanentHeadSortColumns().size());
    assertEquals("ColumnSet SortColumns size", 2, table.getColumnSet().getSortColumns().size());

    //fill the table and sort:
    fillTable(table);
    table.sort();

    //check the sort order of the rows:
    List<ITableRow> rows = table.getRows();
    assertEquals("rows size", 5, rows.size());
    int i = 0;
    assertRowEquals(30, "Ipsum", 1, table, rows, i++);
    assertRowEquals(25, "Lorem", 1, table, rows, i++);
    assertRowEquals(20, "Ipsum", 1, table, rows, i++);
    assertRowEquals(10, "Lorem", 1, table, rows, i++);
    assertRowEquals(1, "A Total", 2, table, rows, i++);
  }

  /**
   * Test of {@link AbstractTable#sort()}. Sorted by:
   * - 1. ThridColumn (defined with AlwaysIncludeSortAtBegin in the column)
   * - 2. SecondColumn descending
   * - 3. FirstColumn ascending.
   */
  @Test
  public void testSortTwoColumns() throws Exception {
    P_Table table = new P_Table();
    table.initTable();

    table.setSortEnabled(true);
    table.getColumnSet().setSortColumn(table.getFirstColumn(), true, 5);
    table.getColumnSet().setSortColumn(table.getSecondColumn(), false, 5);

    //ensure table state:
    assertEquals("SortEnabled", true, table.isSortEnabled());
    assertEquals("FirstColumn - sort index", 2, table.getFirstColumn().getSortIndex());
    assertEquals("FirstColumn - sort ascending", true, table.getFirstColumn().isSortAscending());
    assertEquals("SecondColumn - sort index", 1, table.getSecondColumn().getSortIndex());
    assertEquals("SecondColumn - sort ascending", false, table.getSecondColumn().isSortAscending());
    assertEquals("ThirdColumn - sort index", 0, table.getThridColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThridColumn().getInitialSortIndex());
    assertEquals("ThirdColumn - initial alwaysIncludeSortAtBegin", true, table.getThridColumn().isInitialAlwaysIncludeSortAtBegin());
    assertEquals("ColumnSet PermanentHeadSortColumns size", 1, table.getColumnSet().getPermanentHeadSortColumns().size());
    assertEquals("ColumnSet SortColumns size", 3, table.getColumnSet().getSortColumns().size());

    //fill the table and sort:
    fillTable(table);
    table.sort();

    //check the sort order of the rows:
    List<ITableRow> rows = table.getRows();
    assertEquals("rows size", 5, rows.size());
    int i = 0;
    assertRowEquals(10, "Lorem", 1, table, rows, i++);
    assertRowEquals(25, "Lorem", 1, table, rows, i++);
    assertRowEquals(20, "Ipsum", 1, table, rows, i++);
    assertRowEquals(30, "Ipsum", 1, table, rows, i++);
    assertRowEquals(1, "A Total", 2, table, rows, i++);
  }

  /**
   * Test of {@link AbstractTable#sort()}. Only sorted by ThridColumn (defined with AlwaysIncludeSortAtBegin in the
   * column).
   */
  @Test
  public void testSortDefault() throws Exception {
    P_Table table = new P_Table();
    table.initTable();

    //ensure table state:
    assertEquals("SortEnabled", true, table.isSortEnabled());
    assertEquals("FirstColumn - sort index", -1, table.getFirstColumn().getSortIndex());
    assertEquals("SecondColumn - sort index", -1, table.getSecondColumn().getSortIndex());
    assertEquals("ThirdColumn - sort index", 0, table.getThridColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThridColumn().getInitialSortIndex());
    assertEquals("ThirdColumn - initial alwaysIncludeSortAtBegin", true, table.getThridColumn().isInitialAlwaysIncludeSortAtBegin());
    assertEquals("ColumnSet PermanentHeadSortColumns size", 1, table.getColumnSet().getPermanentHeadSortColumns().size());
    assertEquals("ColumnSet SortColumns size", 1, table.getColumnSet().getSortColumns().size());

    //fill the table and sort:
    table.setSortEnabled(false); //deactivate now, reactivate before the sort() method we want to test.
    fillTable(table);
    table.setSortEnabled(true); //reactivate
    table.sort();

    //check the sort order of the rows:
    List<ITableRow> rows = table.getRows();
    assertEquals("rows size", 5, rows.size());
    int i = 0;
    assertRowEquals(10, "Lorem", 1, table, rows, i++);
    assertRowEquals(30, "Ipsum", 1, table, rows, i++);
    assertRowEquals(25, "Lorem", 1, table, rows, i++);
    assertRowEquals(20, "Ipsum", 1, table, rows, i++);
    assertRowEquals(1, "A Total", 2, table, rows, i++);
  }

  private P_Table createTable(int status) throws ProcessingException {
    P_Table table = new P_Table();
    table.initTable();

    table.addRowsByMatrix(new Object[][]{new Object[]{10, "Lorem"}, new Object[]{11, "Ipsum"}}, status);
    assertRowCount(2, 0, table);
    asssertStatusAndTable(table, status, table.getRow(0));
    asssertStatusAndTable(table, status, table.getRow(1));

    return table;
  }

  private void fillTable(P_Table table) throws ProcessingException {
    addTableRow(table, 10, "Lorem", 1);
    addTableRow(table, 1, "A Total", 2);
    addTableRow(table, 30, "Ipsum", 1);
    addTableRow(table, 25, "Lorem", 1);
    addTableRow(table, 20, "Ipsum", 1);

    assertRowCount(5, 0, table);
  }

  private void addTableRow(P_Table table, Integer first, String second, Integer third) throws ProcessingException {
    ITableRow r = table.addRow(table.createRow());
    table.getFirstColumn().setValue(r, first);
    table.getSecondColumn().setValue(r, second);
    table.getThridColumn().setValue(r, third);
  }

  private static void assertRowCount(int expectedRowCount, int expectedDeletedRowCount, P_Table table) {
    assertEquals(expectedRowCount, table.getRowCount());
    assertEquals(expectedDeletedRowCount, table.getDeletedRowCount());
  }

  private static void asssertStatusAndTable(P_Table expectedTable, int expectedStatus, ITableRow row) {
    assertEquals("status", decodeStatus(expectedStatus), decodeStatus(row.getStatus()));
    assertEquals("table", expectedTable, row.getTable());
  }

  /**
   * @param expectedStatus
   * @return
   */
  private static String decodeStatus(int status) {
    switch (status) {
      case ITableRow.STATUS_DELETED:
        return "ITableRow.STATUS_DELETED";
      case ITableRow.STATUS_INSERTED:
        return "ITableRow.STATUS_INSERTED";
      case ITableRow.STATUS_NON_CHANGED:
        return "ITableRow.STATUS_NON_CHANGED";
      case ITableRow.STATUS_UPDATED:
        return "ITableRow.STATUS_UPDATED";
      default:
        throw new IllegalArgumentException("Unexpected status parameter");
    }
  }

  private static void asssertNoTable(ITableRow row) {
    assertNull(row.getTable());
  }

  private static void assertRowEquals(Integer expectedFirst, String expectedSecond, Integer expectedThird, P_Table table, List<ITableRow> rows, int i) {
    ITableRow row = rows.get(i);
    assertEquals("Row [" + i + "] First Value", expectedFirst, table.getFirstColumn().getValue(row));
    assertEquals("Row [" + i + "] Second Value", expectedSecond, table.getSecondColumn().getValue(row));
    assertEquals("Row [" + i + "] Third Value", expectedThird, table.getThridColumn().getValue(row));
  }

  public static class P_Table extends AbstractTable {

    @Override
    protected boolean getConfiguredAutoDiscardOnDelete() {
      return false;
    }

    public FirstColumn getFirstColumn() {
      return getColumnSet().getColumnByClass(FirstColumn.class);
    }

    public SecondColumn getSecondColumn() {
      return getColumnSet().getColumnByClass(SecondColumn.class);
    }

    public ThirdColumn getThridColumn() {
      return getColumnSet().getColumnByClass(ThirdColumn.class);
    }

    @Order(10)
    public class FirstColumn extends AbstractIntegerColumn {
      @Override
      protected boolean getConfiguredPrimaryKey() {
        return true;
      }
    }

    @Order(20)
    public class SecondColumn extends AbstractStringColumn {
    }

    @Order(30)
    public class ThirdColumn extends AbstractIntegerColumn {

      @Override
      protected int getConfiguredSortIndex() {
        return 20;
      }

      @Override
      protected boolean getConfiguredAlwaysIncludeSortAtBegin() {
        return true;
      }
    }
  }
}
