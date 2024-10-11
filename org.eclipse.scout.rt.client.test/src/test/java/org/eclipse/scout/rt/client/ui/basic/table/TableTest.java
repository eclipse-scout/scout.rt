/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.basic.table.TableTest.P_Table.FirstColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.UserTableRowFilter;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.tile.AbstractHtmlTile;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

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

  @Test
  public void testAddRowPreservesProperties() {
    P_Table table = new P_Table();

    ColumnSet cols = table.getColumnSet();
    ITableRow row = new TableRow(cols);

    final ITableRow insertedRow = table.addRow(row, true);

    assertFalse(insertedRow.isChecked());
    assertNull(insertedRow.getCssClass());
    assertNull(insertedRow.getIconId());

    row = new TableRow(cols);

    row.setChecked(true);
    row.setCssClass("abc");
    FontSpec fontSpec = new FontSpec("Arial", 0, 24);
    row.setFont(fontSpec);
    row.setIconId("iconId");
    row.setBackgroundColor("AAAAAA");
    row.setForegroundColor("000000");

    final ITableRow insertedRow2 = table.addRow(row, true);

    assertTrue(insertedRow2.isChecked());
    assertEquals("abc", insertedRow2.getCssClass());
    assertEquals(fontSpec, insertedRow2.getCell(0).getFont());
    assertEquals("iconId", insertedRow2.getIconId());
    assertEquals("AAAAAA", insertedRow2.getCell(0).getBackgroundColor());
    assertEquals("000000", insertedRow2.getCell(0).getForegroundColor());
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
    assertNoTable(deletedRows.get(0));
    assertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows.get(1));
    assertEquals(1, ta.getEvents().size());
    assertEquals(TableEvent.TYPE_ALL_ROWS_DELETED, ta.getEvents().get(0).getType());
  }

  /**
   * Test that deleted tableRows can be discarded: NON_CHANGED rows goes in deletedRows list. (because the table has
   * AutoDiscardOnDelete = false) delete a row and discard it.
   */
  @Test
  public void testDeleteAndDiscard() {
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
    assertNoTable(row1);
    assertStatusAndTable(table, ITableRow.STATUS_NON_CHANGED, row2);

  }

  /**
   * Test that deleted tableRows can be discarded: NON_CHANGED rows goes in deletedRows list. (because the table has
   * AutoDiscardOnDelete = false) discard these deletedRows with discardAllDeletedRows.
   */
  @Test
  public void testDeleteAllAndDiscardAll() {
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
    assertNoTable(deletedRows.get(0));
    assertNoTable(deletedRows.get(1));
  }

  /**
   * Test that deleted tableRows can be discarded: AutoDiscardOnDelete = true. Rows are automatically discarded
   * (STATUS_INSERTED, or .STATUS_NON_CHANGED)
   */
  @Test
  public void testDeleteAllAutoDiscard() {
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
  public void testDiscardAll_StatusInserted() {
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
  public void testDiscardAll_StatusNonChanged() {
    P_Table table2 = createTestTable(ITableRow.STATUS_NON_CHANGED);

    table2.discardAllRows();
    assertRowCount(0, 0, table2);
  }

  /**
   * Test of {@link AbstractTable#sort()}. If {@link AbstractTable#isSortEnabled()} is false (e.g. sort not enabled),
   * rows will stay in the order they were added to the table.
   */
  @Test
  public void testSortNotEnabled() {
    P_Table table = new P_Table();
    table.init();

    table.setSortEnabled(false);

    //ensure table state:
    assertFalse("SortEnabled", table.isSortEnabled());
    assertEquals("FirstColumn - sort index", -1, table.getFirstColumn().getSortIndex());
    assertEquals("SecondColumn - sort index", -1, table.getSecondColumn().getSortIndex());
    assertEquals("ThirdColumn - sort index", 0, table.getThirdColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThirdColumn().getInitialSortIndex());
    assertTrue("ThirdColumn - initial alwaysIncludeSortAtBegin", table.getThirdColumn().isInitialAlwaysIncludeSortAtBegin());

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
   * <li>ThirdColumn (defined with AlwaysIncludeSortAtBegin in the column)
   * <li>FirstColumn descending.
   * </ol>
   */
  @Test
  public void testSortFirstColumn() {
    P_Table table = new P_Table();
    table.init();

    table.setSortEnabled(true);
    table.getColumnSet().setSortColumn(table.getFirstColumn(), false);

    //ensure table state:
    assertTrue("SortEnabled", table.isSortEnabled());
    assertEquals("FirstColumn - sort index", 1, table.getFirstColumn().getSortIndex());
    assertFalse("FirstColumn - sort ascending", table.getFirstColumn().isSortAscending());
    assertEquals("SecondColumn - sort index", -1, table.getSecondColumn().getSortIndex());
    assertEquals("ThirdColumn - sort index", 0, table.getThirdColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThirdColumn().getInitialSortIndex());
    assertTrue("ThirdColumn - initial alwaysIncludeSortAtBegin", table.getThirdColumn().isInitialAlwaysIncludeSortAtBegin());
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
   * Test of {@link AbstractTable#sort()}. Sorted by: - 1. ThirdColumn (defined with AlwaysIncludeSortAtBegin in the
   * column) - 2. SecondColumn descending - 3. FirstColumn ascending.
   */
  @Test
  public void testSortTwoColumns() {
    P_Table table = new P_Table();
    table.init();

    table.setSortEnabled(true);
    table.getColumnSet().setSortColumn(table.getSecondColumn(), false);
    table.getColumnSet().addSortColumn(table.getFirstColumn(), true);

    //ensure table state:
    assertTrue("SortEnabled", table.isSortEnabled());
    assertEquals("FirstColumn - sort index", 2, table.getFirstColumn().getSortIndex());
    assertTrue("FirstColumn - sort ascending", table.getFirstColumn().isSortAscending());
    assertEquals("SecondColumn - sort index", 1, table.getSecondColumn().getSortIndex());
    assertFalse("SecondColumn - sort ascending", table.getSecondColumn().isSortAscending());
    assertEquals("ThirdColumn - sort index", 0, table.getThirdColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThirdColumn().getInitialSortIndex());
    assertTrue("ThirdColumn - initial alwaysIncludeSortAtBegin", table.getThirdColumn().isInitialAlwaysIncludeSortAtBegin());
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
  public void testGetRowByKey() {
    P_Table table = createTestTable(ITableRow.STATUS_NON_CHANGED);

    Assert.assertNull(table.getRowByKey(null));
    Assert.assertNull(table.getRowByKey(Collections.singletonList(null)));
    Assert.assertNull(table.getRowByKey(Collections.singletonList(13)));
    Assert.assertNull(table.getRowByKey(Collections.singletonList("13")));
    Assert.assertNull(table.getRowByKey(Collections.singletonList("10")));
    Assert.assertEquals("Lorem", table.getSecondColumn().getValue(table.getRowByKey(Collections.singletonList(10))));
    Assert.assertEquals("Ipsum", table.getSecondColumn().getValue(table.getRowByKey(Arrays.asList(11))));
  }

  /**
   * Test of {@link AbstractTable#sort()}. Only sorted by ThirdColumn (defined with AlwaysIncludeSortAtBegin in the
   * column).
   */
  @Test
  public void testSortDefault() {
    P_Table table = new P_Table();
    table.init();

    //ensure table state:
    assertTrue("SortEnabled", table.isSortEnabled());
    assertEquals("FirstColumn - sort index", -1, table.getFirstColumn().getSortIndex());
    assertEquals("SecondColumn - sort index", -1, table.getSecondColumn().getSortIndex());
    assertEquals("ThirdColumn - sort index", 0, table.getThirdColumn().getSortIndex());
    assertEquals("ThirdColumn - initial sort index", 20, table.getThirdColumn().getInitialSortIndex());
    assertTrue("ThirdColumn - initial alwaysIncludeSortAtBegin", table.getThirdColumn().isInitialAlwaysIncludeSortAtBegin());
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
    assertRowEquals(20, "Ipsum", 1, table, rows, i++);
    assertRowEquals(25, "Lorem", 1, table, rows, i++);
    assertRowEquals(30, "Ipsum", 1, table, rows, i++);
    assertRowEquals(1, "A Total", 2, table, rows, i++);

    // remove third column from sorting
    table.getColumnSet().getColumnByClass(P_Table.ThirdColumn.class).setInitialAlwaysIncludeSortAtBegin(false);
    table.getColumnSet().getColumnByClass(P_Table.ThirdColumn.class).setInitialSortIndex(-1);

    // add second column to sorting
    table.getColumnSet().getColumnByClass(P_Table.SecondColumn.class).setInitialSortIndex(0);

    // sortAscending of a column with sortActive = false shouldn't have any effect
    table.getColumnSet().getColumnByClass(P_Table.FirstColumn.class).setInitialSortAscending(false);

    table.getColumnSet().resetSortingAndGrouping();
    table.sort();

    // check the sort order of the rows:
    rows = table.getRows();
    assertEquals("rows size", 5, rows.size());
    i = 0;
    assertRowEquals(1, "A Total", 2, table, rows, i++);
    assertRowEquals(20, "Ipsum", 1, table, rows, i++);
    assertRowEquals(30, "Ipsum", 1, table, rows, i++);
    assertRowEquals(10, "Lorem", 1, table, rows, i++);
    assertRowEquals(25, "Lorem", 1, table, rows, i++);
  }

  @Test
  public void testReplaceRows() {
    P_Table table = new P_Table();
    table.init();
  }

  @Test
  public void testInitConfig_DefaultValues() {
    P_Table table = new P_Table();
    table.initConfig();
    assertTrue(table.isEnabled());
  }

  /**
   * ResetColumnConfiguration disposes the column set and creates a new one. If there was a context column, it has to be
   * set to null. Otherwise, the UI would throw an error because the column is not known (anymore).
   */
  @Test
  public void testResetContextColumn() {
    P_Table table = new P_Table();
    table.init();
    fillTable(table);
    FirstColumn column = table.getFirstColumn();
    table.getUIFacade().setContextColumnFromUI(column);
    assertSame(column, table.getContextColumn());

    table.resetColumnConfiguration();
    assertNull(table.getContextColumn());
  }

  @Test
  public void testResortOnSortCellUpdate() {
    P_Table table = Mockito.spy(new P_Table());
    table.init();
    fillTable(table);
    Mockito.verify(table, Mockito.times(2)).sort();
    table.getRow(0).getCellForUpdate(table.getThirdColumn()).setValue(4);
    Mockito.verify(table, Mockito.times(3)).sort();
  }

  @Test
  public void testNoResortOnCellUpdate() {
    P_Table table = Mockito.spy(new P_Table());
    table.init();
    fillTable(table);
    Mockito.verify(table, Mockito.times(2)).sort();
    table.getRow(0).getCellForUpdate(table.getSecondColumn()).setValue("Baluu");
    Mockito.verify(table, Mockito.times(2)).sort();
  }

  @Test
  public void testUserRowFilter() {
    P_Table table = new P_Table();
    table.init();
    fillTable(table);

    assertRowCount(5, 0, table);
    assertEquals(0, table.getRowFilters().size());
    assertEquals(5, table.getFilteredRowCount());
    assertEquals(0, table.getSelectedRowCount());

    table.selectRows(CollectionUtility.arrayList(table.getRow(0), table.getRow(1)));

    assertEquals(2, table.getSelectedRowCount());

    table.addRowFilter(new UserTableRowFilter(CollectionUtility.hashSet(table.getRow(0))));

    assertEquals(1, table.getRowFilters().size());
    assertEquals(1, table.getFilteredRowCount());
    assertEquals(1, table.getSelectedRowCount());

    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{10, "Lorem", 1}));
    rows.add(table.createRow(new Object[]{2, "Ipsum", 2}));
    table.replaceRows(rows);

    assertRowCount(2, 0, table);
    assertEquals(1, table.getRowFilters().size());
    assertEquals(1, table.getFilteredRowCount());
    assertEquals(1, table.getSelectedRowCount());

    table.removeUserRowFilters();
    table.addRowFilter(new UserTableRowFilter(CollectionUtility.hashSet(table.getRow(0))));

    assertRowCount(2, 0, table);
    assertEquals(1, table.getRowFilters().size());
    assertEquals(1, table.getFilteredRowCount());
    assertEquals(1, table.getSelectedRowCount());
  }

  @Test
  public void testUserRowFilter_AutoDiscard() {
    P_Table table = new P_Table();
    table.setAutoDiscardOnDelete(true);
    table.init();
    fillTable(table);

    assertRowCount(5, 0, table);
    assertEquals(0, table.getRowFilters().size());
    assertEquals(5, table.getFilteredRowCount());
    assertEquals(0, table.getSelectedRowCount());

    table.selectRows(CollectionUtility.arrayList(table.getRow(0), table.getRow(1)));

    table.addRowFilter(new UserTableRowFilter(CollectionUtility.hashSet(table.getRow(0))));

    assertEquals(1, table.getRowFilters().size());
    assertEquals(1, table.getFilteredRowCount());
    assertEquals(1, table.getSelectedRowCount());

    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{10, "Lorem", 1}));
    rows.add(table.createRow(new Object[]{2, "Ipsum", 2}));
    table.replaceRows(rows);

    assertRowCount(2, 0, table);
    assertEquals(0, table.getRowFilters().size());
    assertEquals(2, table.getFilteredRowCount());
    assertEquals(1, table.getSelectedRowCount());

    table.removeUserRowFilters();
    table.addRowFilter(new UserTableRowFilter(CollectionUtility.hashSet(table.getRow(0))));

    assertRowCount(2, 0, table);
    assertEquals(1, table.getRowFilters().size());
    assertEquals(1, table.getFilteredRowCount());
    assertEquals(1, table.getSelectedRowCount());
  }

  @Test
  public void testSelectionAfterDelete() {
    P_Table table = new P_Table();
    table.setAutoDiscardOnDelete(true);
    table.init();
    fillTable(table);

    ITableRow r0 = table.getRow(0);
    ITableRow r1 = table.getRow(1);
    ITableRow r2 = table.getRow(2);
    ITableRow r3 = table.getRow(3);
    ITableRow r4 = table.getRow(4);

    assertRowCount(5, 0, table);
    assertEquals(0, table.getSelectedRowCount());

    table.selectRows(CollectionUtility.arrayList(r1, r2, r3, r4));

    assertEquals(4, table.getSelectedRowCount());
    assertEquals(r1, table.getSelectedRows().get(0));
    assertEquals(r2, table.getSelectedRows().get(1));
    assertEquals(r3, table.getSelectedRows().get(2));
    assertEquals(r4, table.getSelectedRows().get(3));

    table.deleteRow(r1);

    assertEquals(3, table.getSelectedRowCount());
    assertEquals(r2, table.getSelectedRows().get(0));
    assertEquals(r3, table.getSelectedRows().get(1));
    assertEquals(r4, table.getSelectedRows().get(2));

    table.deleteRow(r3);

    assertEquals(2, table.getSelectedRowCount());
    assertEquals(r2, table.getSelectedRows().get(0));
    assertEquals(r4, table.getSelectedRows().get(1));

    table.deleteRow(r0);

    assertEquals(2, table.getSelectedRowCount());
    assertEquals(r2, table.getSelectedRows().get(0));
    assertEquals(r4, table.getSelectedRows().get(1));
  }

  @Test
  public void testCreateTableWithTiles() {
    P_Table table0 = new P_Table();
    table0.init();
    fillTable(table0);
    assertNull(table0.getTableTileGridMediator());
    assertNull(table0.getTileTableHeader());

    // ---

    P_TableWithTiles table = new P_TableWithTiles();
    table.init();
    fillTable(table);
    assertNotNull(table.getTableTileGridMediator());
    assertNotNull(table.getTileTableHeader());
    assertEquals(table.getRowCount(), table.getTableTileGridMediator().getTileMappings().size());

    addTableRow(table, 99, "Flupp", 161616);
    assertEquals(table.getRowCount(), table.getTableTileGridMediator().getTileMappings().size());

    table.deleteAllRows();
    table.discardAllDeletedRows();
    assertEquals(0, table.getTableTileGridMediator().getTileMappings().size());
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
    table.init();
    table.addRowsByMatrix(new Object[][]{new Object[]{10, "Lorem"}, new Object[]{11, "Ipsum"}}, status);
    return table;
  }

  private void fillTable(P_Table table) {
    table.setTableChanging(true);
    try {
      addTableRow(table, 10, "Lorem", 1);
      addTableRow(table, 1, "A Total", 2);
      addTableRow(table, 30, "Ipsum", 1);
      addTableRow(table, 25, "Lorem", 1);
      addTableRow(table, 20, "Ipsum", 1);
    }
    finally {
      table.setTableChanging(false);
    }
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

  private static void assertNoTable(ITableRow row) {
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

  public static class P_TableWithTiles extends P_Table {

    @Override
    protected boolean getConfiguredTileMode() {
      return true;
    }

    @Override
    protected ITile execCreateTile(ITableRow row) {
      return new P_Tile(row);
    }

    protected class P_Tile extends AbstractHtmlTile {

      private ITableRow m_row;

      public P_Tile(ITableRow row) {
        super(false);
        m_row = row;
        callInitializer();
      }

      @Override
      protected String getConfiguredContent() {
        return HTML.fragment(
            HTML.bold(getSecondColumn().getValue(m_row)),
            ": ",
            getThirdColumn().getValue(m_row) + "").toHtml();
      }
    }

    public class P_TileTableHeader extends AbstractTileTableHeader {

      @Order(1000)
      public class FilterField extends AbstractStringField {

        @Override
        protected String getConfiguredLabel() {
          return "Filter";
        }
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

  @Test
  public void testUnwrapText() {
    P_Table table = new P_Table();

    assertEquals("abc", table.unwrapText("abc"));
    assertEquals("abc", table.unwrapText(" abc"));
    assertEquals("abc", table.unwrapText("abc "));
    assertEquals("abc", table.unwrapText(" abc "));

    assertEquals("abc", table.unwrapText("abc"));
    assertEquals("abc", table.unwrapText("\fabc"));
    assertEquals("abc", table.unwrapText("abc\f"));
    assertEquals("abc", table.unwrapText("\fabc\f"));

    assertEquals("a bc", table.unwrapText("a bc"));
    assertEquals("a bc", table.unwrapText(" a bc"));
    assertEquals("a bc", table.unwrapText("a bc "));
    assertEquals("a bc", table.unwrapText(" a bc "));

    assertEquals("a  bc", table.unwrapText("a  bc"));
    assertEquals("a  bc", table.unwrapText("  a  bc"));
    assertEquals("a  bc", table.unwrapText("a  bc  "));
    assertEquals("a  bc", table.unwrapText("  a  bc  "));

    assertEquals("a  bc", table.unwrapText("a \tbc"));
    assertEquals("a  bc", table.unwrapText(" \ta \tbc"));
    assertEquals("a  bc", table.unwrapText("a \tbc \t"));
    assertEquals("a  bc", table.unwrapText(" \ta \tbc \t"));

    assertEquals("a bc", table.unwrapText("a\tbc"));
    assertEquals("a bc", table.unwrapText(" a\tbc"));
    assertEquals("a bc", table.unwrapText("a\tbc "));
    assertEquals("a bc", table.unwrapText(" a\tbc "));

    assertEquals("a bc", table.unwrapText("a\tbc"));
    assertEquals("a bc", table.unwrapText("\ta\tbc"));
    assertEquals("a bc", table.unwrapText("a\tbc\t"));
    assertEquals("a bc", table.unwrapText("\ta\tbc\t"));

    assertEquals("abc", table.unwrapText("abc"));
    assertEquals("abc", table.unwrapText("\tabc"));
    assertEquals("abc", table.unwrapText("abc\t"));
    assertEquals("abc", table.unwrapText("\tabc\t"));

    assertEquals("abc", table.unwrapText("abc"));
    assertEquals("abc", table.unwrapText(" \tabc"));
    assertEquals("abc", table.unwrapText("abc\t "));
    assertEquals("abc", table.unwrapText(" \tabc\t "));

    assertEquals("abc", table.unwrapText("abc"));
    assertEquals("abc", table.unwrapText(" \t\nabc"));
    assertEquals("abc", table.unwrapText("abc\n\t "));
    assertEquals("abc", table.unwrapText(" \t\nabc\n\t "));

    assertEquals("abc 123", table.unwrapText("abc\n123"));
    assertEquals("abc 123", table.unwrapText(" \t\nabc\n123"));
    assertEquals("abc 123", table.unwrapText("abc\n123\n\t "));
    assertEquals("abc 123", table.unwrapText(" \t\nabc\n123\n\t "));

    assertEquals("abc 123", table.unwrapText("abc\n 123"));
    assertEquals("abc 123", table.unwrapText(" \t\nabc\n 123"));
    assertEquals("abc 123", table.unwrapText("abc\n 123\n\t "));
    assertEquals("abc 123", table.unwrapText(" \t\nabc\n 123\n\t "));

    assertEquals("abc 123", table.unwrapText("abc\n \n123"));
    assertEquals("abc 123", table.unwrapText(" \t\nabc\n \n123"));
    assertEquals("abc 123", table.unwrapText("abc\n \n123\n\t "));
    assertEquals("abc 123", table.unwrapText(" \t\nabc\n \n123\n\t "));

    assertEquals("a bc 123", table.unwrapText("a bc\n \n123"));
    assertEquals("a bc 123", table.unwrapText(" \t\na bc\n \n123"));
    assertEquals("a bc 123", table.unwrapText("a bc\n \n123\n\t "));
    assertEquals("a bc 123", table.unwrapText(" \t\na bc\n \n123\n\t "));

    assertEquals("a bc 123", table.unwrapText("a bc\r\n \r\n123"));
    assertEquals("a bc 123", table.unwrapText(" \t\r\na bc\n \r\n123"));
    assertEquals("a bc 123", table.unwrapText("a bc\r\n \r\n123\r\n\t "));
    assertEquals("a bc 123", table.unwrapText(" \t\r\na bc\r\n \r\n123\r\n\t "));

    assertEquals("a bc 12 3", table.unwrapText("a bc\n\r \n\r \n\r  12\t3"));
    assertEquals("a bc 12 3", table.unwrapText(" \t\n\ra bc\n\r \n\r \n\r  12\t3"));
    assertEquals("a bc 12 3", table.unwrapText("a bc\n\r \n\r \n\r  12\t3\n\r\t "));
    assertEquals("a bc 12 3", table.unwrapText(" \t\n\ra bc\n\r \n\r \n\r  12\t3\n\r\t "));

    assertEquals("a  bc 12 3", table.unwrapText("a\t bc\n\r \n\r \n\r  12\t3"));
    assertEquals("a  bc 12 3", table.unwrapText(" \t\n\ra\t bc\n\r \n\r \n\r  12\t3"));
    assertEquals("a  bc 12 3", table.unwrapText("a\t bc\n\r \n\r \n\r  12\t3\n\r\t "));
    assertEquals("a  bc 12 3", table.unwrapText(" \t\n\ra\t bc\n\r \n\r \n\r  12\t3\n\r\t "));
  }

  @Test
  public void testEventHistory() {
    P_Table table = new P_Table() {
      @Override
      protected IEventHistory<TableEvent> createEventHistory() {
        return new DefaultTableEventHistory(5000L) {
          @Override
          public void notifyEvent(TableEvent event) {
            if (event.getType() == -42) {
              addToCache(event.getType(), event);
            }
            super.notifyEvent(event);
          }
        };
      }
    };
    assertTrue(table.getEventHistory().getRecentEvents().isEmpty());

    TableEvent customEvent = new TableEvent(table, -42);
    TableEvent anotherCustomEvent = new TableEvent(table, -43); // will be ignored
    TableEvent scrollToSelectionEvent = new TableEvent(table, TableEvent.TYPE_SCROLL_TO_SELECTION);

    table.fireTableEventInternal(customEvent);
    table.fireTableEventInternal(anotherCustomEvent);
    table.fireTableEventInternal(scrollToSelectionEvent);

    Collection<TableEvent> recentEvents = table.getEventHistory().getRecentEvents();
    assertEquals(2, recentEvents.size());
    assertTrue(recentEvents.contains(customEvent));
    assertTrue(recentEvents.contains(scrollToSelectionEvent));
  }
}
