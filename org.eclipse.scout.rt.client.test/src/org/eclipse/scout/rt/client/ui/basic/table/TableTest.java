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

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
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

    ITableRow[] deletedRows = table.getDeletedRows();
    Assert.assertEquals(2, deletedRows.length);
    asssertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows[0]);
    asssertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows[1]);

    table.discardDeletedRow(deletedRows[0]);
    assertRowCount(0, 1, table);
    asssertNoTable(deletedRows[0]);
    asssertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows[1]);
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

    ITableRow[] deletedRows = table.getDeletedRows();
    Assert.assertEquals(2, deletedRows.length);
    asssertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows[0]);
    asssertStatusAndTable(table, ITableRow.STATUS_DELETED, deletedRows[1]);

    table.discardAllDeletedRows();
    assertRowCount(0, 0, table);
    asssertNoTable(deletedRows[0]);
    asssertNoTable(deletedRows[1]);
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

  private P_Table createTable(int status) throws ProcessingException {
    P_Table table = new P_Table();

    table.addRowsByMatrix(new Object[][]{new Object[]{10, "Lorem"}, new Object[]{11, "Ipsum"}}, status);
    assertRowCount(2, 0, table);
    asssertStatusAndTable(table, status, table.getRow(0));
    asssertStatusAndTable(table, status, table.getRow(1));

    return table;
  }

  private static void assertRowCount(int expectedRowCount, int expectedDeletedRowCount, P_Table table) {
    Assert.assertEquals(expectedRowCount, table.getRowCount());
    Assert.assertEquals(expectedDeletedRowCount, table.getDeletedRowCount());
  }

  private static void asssertStatusAndTable(P_Table expectedTable, int expectedStatus, ITableRow row) {
    Assert.assertEquals(expectedStatus, row.getStatus());
    Assert.assertEquals(expectedTable, row.getTable());
  }

  private static void asssertNoTable(ITableRow row) {
    Assert.assertNull(row.getTable());
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
  }
}
