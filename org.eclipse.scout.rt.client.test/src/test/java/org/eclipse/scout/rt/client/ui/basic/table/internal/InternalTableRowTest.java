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
package org.eclipse.scout.rt.client.ui.basic.table.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.junit.Test;

/**
 * Tests for {@link InternalTableRow}
 */
public class InternalTableRowTest {

  @Test
  public void testCreateInternalTableRow() throws Exception {
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    row.getCellForUpdate(0).setValue("Test");
    row.getCellForUpdate(0).setText("TestText");
    row.setStatus(ITableRow.STATUS_INSERTED);
    InternalTableRow ir = new InternalTableRow(table, row);
    assertEquals(row.getStatus(), ir.getStatus());
    assertEquals(table, ir.getTable());
    assertEquals("Test", ir.getCellForUpdate(0).getValue());
    assertEquals("TestText", ir.getCellForUpdate(0).getText());
  }

  @Test
  public void testSetStatus() throws Exception {
    TestTable table = new TestTable();
    InternalTableRow ir = new InternalTableRow(table);
    ir.setStatusDeleted();
    assertTrue(ir.isStatusDeleted());
  }

  @Test
  public void testSetCssClass() throws Exception {
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    row.getCellForUpdate(0).setValue("Test");
    InternalTableRow ir = new InternalTableRow(table, row);
    ir.setCssClass("test");
    assertEquals("test", ir.getCssClass());
    assertEquals("test", ir.getCell(0).getCssClass());
  }

  @Test
  public void testCellChange() {
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    InternalTableRow ir = new InternalTableRow(table, row);
    ir.setRowChanging(true);
    ir.cellChanged(ir.getCell(0), ICell.TEXT_BIT);
    assertEquals(1, ir.getUpdatedColumnIndexes().size());
    assertEquals(Integer.valueOf(0), ir.getUpdatedColumnIndexes().iterator().next());
    ir.setRowChanging(false);
  }

  @Test
  public void testValueChange() {
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    InternalTableRow ir = new InternalTableRow(table, row);
    ir.setRowChanging(true);
    ir.cellChanged(ir.getCell(0), ICell.VALUE_BIT);
    Set<Integer> changedColumnIdx = ir.getUpdatedColumnIndexes(ICell.VALUE_BIT);
    assertEquals(1, changedColumnIdx.size());
    assertEquals(Integer.valueOf(0), changedColumnIdx.iterator().next());
    ir.setRowChanging(false);
  }

  @Test
  public void testValueChange_NoChange() {
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    InternalTableRow ir = new InternalTableRow(table, row);
    ir.setRowChanging(true);
    ir.cellChanged(ir.getCell(0), ICell.VALUE_BIT);
    Set<Integer> changedColumnIdx = ir.getUpdatedColumnIndexes(ICell.TEXT_BIT);
    assertEquals(0, changedColumnIdx.size());
    ir.setRowChanging(false);
  }

  /**
   * A table with a test column where only the first row is editable.
   */
  public class TestTable extends AbstractTable {
    @Order(10)
    public class TestColumn extends AbstractStringColumn {
    }
  }

}
