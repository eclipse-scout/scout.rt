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
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractTable}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TableTest {

  @Test
  public void testAddRows_StatusNonChanged() {
    P_Table table = createTestTable(ITableRow.STATUS_NON_CHANGED);
    assertValidTestTable(table, ITableRow.STATUS_NON_CHANGED);
  }

  @Test
  public void testAddRows_StatusInserted() {
    P_Table table = createTestTable(ITableRow.STATUS_INSERTED);
    assertValidTestTable(table, ITableRow.STATUS_INSERTED);
  }

  @Test
  public void testAddRows_StatusUpdated() {
    P_Table table = createTestTable(ITableRow.STATUS_UPDATED);
    assertValidTestTable(table, ITableRow.STATUS_UPDATED);
  }

  @Test
  public void testAddRows_StatusDeleted() {
    P_Table table = createTestTable(ITableRow.STATUS_DELETED);
    assertValidTestTable(table, ITableRow.STATUS_DELETED);
  }

  @Test
  public void testAddRow() {
    P_Table table = new P_Table();
    final ITableRow row = table.addRow();
    assertEquals(ITableRow.STATUS_INSERTED, row.getStatus());
  }

  /**
   * Test that new inserted rows are automatically discarded.
   */
  @Test
  public void testDeleteAllNew() {
    //Bug 361985
    P_Table table = createTestTable(ITableRow.STATUS_INSERTED);

    final CapturingTableAdapter ta = new CapturingTableAdapter();
    table.addTableListener(ta);
    table.deleteAllRows();
    assertRowCount(0, 0, table);
    assertEquals(1, ta.getEvents().size());
    assertEquals(TableEvent.TYPE_ALL_ROWS_DELETED, ta.getEvents().get(0).getType());
  }

  /**
   * Test that deleted tableRows can be discarded: NON_CHANGED rows goes in deletedRows list. (because the table has
   * AutoDiscardOnDelete = false) discard these rows.
   */
  @Test
  public void testDeleteAllAndDiscardFirst() {
    //Bug 361985
    P_Table table = createTestTable(ITableRow.STATUS_NON_CHANGED);
    final CapturingTableAdapter ta = new CapturingTableAdapter();
    table.addTableListener(ta);

    table.deleteAllRows();
    assertRowCount(0, 2, table);

    List<ITableRow> deletedRows = table.getDeletedRows();
    assertEquals(2, deletedRows.size());
    assertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows.get(0));
    assertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows.get(1));

    table.discardDeletedRow(deletedRows.get(0));
    assertRowCount(0, 1, table);
    asssertNoTable(deletedRows.get(0));
    assertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows.get(1));
    assertEquals(1, ta.getEvents().size());
    assertEquals(TableEvent.TYPE_ALL_ROWS_DELETED, ta.getEvents().get(0).getType());
  }

  /**
   * Test that deleted tableRows can be discarded: NON_CHANGED rows goes in deletedRows list. (because the table has
   * AutoDiscardOnDelete = false) delete a row and discard it.
   */
  @Test
  public void testDeleteAndDiscard() throws Exception {
    //Bug 361985
    P_Table table = createTestTable(ITableRow.STATUS_NON_CHANGED);
    final CapturingTableAdapter ta = new CapturingTableAdapter();
    table.addTableListener(ta);

    ITableRow row1 = table.getRow(0);
    ITableRow row2 = table.getRow(1);
    table.deleteRow(row1);
    assertRowCount(1, 1, table);

    assertStatusAndTable(table, ITableRow.STATUS_DELETED, row1);
    assertStatusAndTable(table, ITableRow.STATUS_NON_CHANGED, row2);

    table.discardDeletedRow(row1);
    assertRowCount(1, 0, table);
    asssertNoTable(row1);
    assertStatusAndTable(table, ITableRow.STATUS_NON_CHANGED, row2);

  }

  /**
   * Test that deleted tableRows can be discarded: NON_CHANGED rows goes in deletedRows list. (because the table has
   * AutoDiscardOnDelete = false) discard these deletedRows with discardAllDeletedRows.
   */
  @Test
  public void testDeleteAllAndDiscardAll() throws Exception {
    //Bug 361985
    P_Table table = createTestTable(ITableRow.STATUS_NON_CHANGED);

    table.deleteAllRows();
    assertRowCount(0, 2, table);

    List<ITableRow> deletedRows = table.getDeletedRows();
    assertEquals(2, deletedRows.size());
    assertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows.get(0));
    assertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows.get(1));

    table.discardAllDeletedRows();
    assertRowCount(0, 0, table);
    asssertNoTable(deletedRows.get(0));
    asssertNoTable(deletedRows.get(1));
  }

  /**
   * Test that deleted tableRows can be discarded: AutoDiscardOnDelete = true. Rows are automatically discarded
   * (STATUS_INSERTED, or .STATUS_NON_CHANGED)
   */
  @Test
  public void testDeleteAllAutoDiscard() throws Exception {
    //Bug 361985
    //test with STATUS_INSERTED and AutoDiscardOnDelete:
    P_Table table = createTestTable(ITableRow.STATUS_INSERTED);
    table.setAutoDiscardOnDelete(true);

    table.deleteAllRows();
    assertRowCount(0, 0, table);

    //test with STATUS_NON_CHANGED:
    P_Table table2 = createTestTable(ITableRow.STATUS_NON_CHANGED);
    table2.setAutoDiscardOnDelete(true);

    table2.deleteAllRows();
    assertRowCount(0, 0, table2);
  }

  /**
   * Test that deleted tableRows can be discarded (with table rows with status STATUS_INSERTED):<br>
   * discard all rows => no deleted row.
   */
  @Test
  public void testDiscardAll_StatusInserted() throws Exception {
    //Bug 361985
    //test with STATUS_INSERTED and AutoDiscardOnDelete:
    P_Table table = createTestTable(ITableRow.STATUS_INSERTED);

    table.discardAllRows();
    assertRowCount(0, 0, table);

  }

  /**
   * Tests discarding rows with status STATUS_NON_CHANGED.<br>
   * discard all rows => no deleted row.
   */
  @Test
  public void testDiscardAll_StatusNonChanged() throws Exception {
    P_Table table2 = createTestTable(ITableRow.STATUS_NON_CHANGED);

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
    assertEquals("ThirdColumn - sort index", 0, table.getThirdColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThirdColumn().getInitialSortIndex());
    assertEquals("ThirdColumn - initial alwaysIncludeSortAtBegin", true, table.getThirdColumn().isInitialAlwaysIncludeSortAtBegin());

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
   * <ol>
   * <li>ThridColumn (defined with AlwaysIncludeSortAtBegin in the column)
   * <li>FirstColumn descending.
   * </ol>
   */
  @Test
  public void testSortFirstColumn() throws Exception {
    P_Table table = new P_Table();
    table.initTable();

    table.setSortEnabled(true);
    table.getColumnSet().setSortColumn(table.getFirstColumn(), false);

    //ensure table state:
    assertEquals("SortEnabled", true, table.isSortEnabled());
    assertEquals("FirstColumn - sort index", 1, table.getFirstColumn().getSortIndex());
    assertEquals("FirstColumn - sort ascending", false, table.getFirstColumn().isSortAscending());
    assertEquals("SecondColumn - sort index", -1, table.getSecondColumn().getSortIndex());
    assertEquals("ThirdColumn - sort index", 0, table.getThirdColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThirdColumn().getInitialSortIndex());
    assertEquals("ThirdColumn - initial alwaysIncludeSortAtBegin", true, table.getThirdColumn().isInitialAlwaysIncludeSortAtBegin());
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
   * Test of {@link AbstractTable#sort()}. Sorted by: - 1. ThridColumn (defined with AlwaysIncludeSortAtBegin in the
   * column) - 2. SecondColumn descending - 3. FirstColumn ascending.
   */
  @Test
  public void testSortTwoColumns() throws Exception {
    P_Table table = new P_Table();
    table.initTable();

    table.setSortEnabled(true);
    table.getColumnSet().setSortColumn(table.getSecondColumn(), false);
    table.getColumnSet().addSortColumn(table.getFirstColumn(), true);

    //ensure table state:
    assertEquals("SortEnabled", true, table.isSortEnabled());
    assertEquals("FirstColumn - sort index", 2, table.getFirstColumn().getSortIndex());
    assertEquals("FirstColumn - sort ascending", true, table.getFirstColumn().isSortAscending());
    assertEquals("SecondColumn - sort index", 1, table.getSecondColumn().getSortIndex());
    assertEquals("SecondColumn - sort ascending", false, table.getSecondColumn().isSortAscending());
    assertEquals("ThirdColumn - sort index", 0, table.getThirdColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThirdColumn().getInitialSortIndex());
    assertEquals("ThirdColumn - initial alwaysIncludeSortAtBegin", true, table.getThirdColumn().isInitialAlwaysIncludeSortAtBegin());
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

  @Test
  public void testFindRowByKey() {
    P_Table table = createTestTable(ITableRow.STATUS_NON_CHANGED);

    Assert.assertNull(table.findRowByKey(null));
    Assert.assertNull(table.findRowByKey(Collections.singletonList(null)));
    Assert.assertNull(table.findRowByKey(Collections.singletonList(13)));
    Assert.assertNull(table.findRowByKey(Collections.singletonList("13")));
    Assert.assertNull(table.findRowByKey(Collections.singletonList("10")));
    Assert.assertEquals("Lorem", table.getSecondColumn().getValue(table.findRowByKey(Collections.singletonList(10))));
    Assert.assertEquals("Ipsum", table.getSecondColumn().getValue(table.findRowByKey(Arrays.asList(11, 12, 13))));
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
    assertEquals("ThirdColumn - sort index", 0, table.getThirdColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThirdColumn().getInitialSortIndex());
    assertEquals("ThirdColumn - initial alwaysIncludeSortAtBegin", true, table.getThirdColumn().isInitialAlwaysIncludeSortAtBegin());
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

  @Test
  public void testReplaceRows() {
    P_Table table = new P_Table();
    table.initTable();
  }

  @Test
  public void testInitConfig_DefaultValues() throws Exception {
    P_Table table = new P_Table();
    table.initConfig();
    assertTrue(table.isEnabled());
  }

  private void assertValidTestTable(P_Table table, int status) {
    assertRowCount(2, 0, table);
    assertStatusAndTable(table, status, table.getRow(0));
    assertStatusAndTable(table, status, table.getRow(1));
  }

  /**
   * Creates a table with 2 rows. with given status.
   */
  private P_Table createTestTable(int status) {
    P_Table table = new P_Table();
    table.initTable();
    table.addRowsByMatrix(new Object[][]{new Object[]{10, "Lorem"}, new Object[]{11, "Ipsum"}}, status);
    return table;
  }

  private void fillTable(P_Table table) {
    addTableRow(table, 10, "Lorem", 1);
    addTableRow(table, 1, "A Total", 2);
    addTableRow(table, 30, "Ipsum", 1);
    addTableRow(table, 25, "Lorem", 1);
    addTableRow(table, 20, "Ipsum", 1);

    assertRowCount(5, 0, table);
  }

  private void addTableRow(P_Table table, Integer first, String second, Integer third) {
    ITableRow r = table.addRow();
    table.getFirstColumn().setValue(r, first);
    table.getSecondColumn().setValue(r, second);
    table.getThirdColumn().setValue(r, third);
  }

  private static void assertRowCount(int expectedRowCount, int expectedDeletedRowCount, P_Table table) {
    assertEquals(expectedRowCount, table.getRowCount());
    assertEquals(expectedDeletedRowCount, table.getDeletedRowCount());
  }

  private static void assertStatusAndTable(P_Table expectedTable, int expectedStatus, ITableRow row) {
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
    assertEquals("Row [" + i + "] Third Value", expectedThird, table.getThirdColumn().getValue(row));
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

    public ThirdColumn getThirdColumn() {
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

  class CapturingTableAdapter extends TableAdapter {
    private List<TableEvent> m_events = new ArrayList<>();

    protected List<TableEvent> getEvents() {
      return m_events;
    }

    @Override
    public void tableChanged(TableEvent e) {
      m_events.add(e);
    }
  }
}
