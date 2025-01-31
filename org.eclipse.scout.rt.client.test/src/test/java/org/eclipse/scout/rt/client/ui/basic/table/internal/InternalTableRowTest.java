/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.internal;

import static org.junit.Assert.*;

import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

/**
 * Tests for {@link InternalTableRow}
 */
public class InternalTableRowTest {

  private static final String TEST_ICON_ID = "iconId";

  @Test
  public void testCreateInternalTableRow() {
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
  public void testSetStatus() {
    TestTable table = new TestTable();
    InternalTableRow ir = new InternalTableRow(table);
    ir.setStatusDeleted();
    assertTrue(ir.isStatusDeleted());
  }

  @Test
  public void testSetCssClass() {
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

  @Test
  public void testSetEnabled() {
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    InternalTableRow ir = new InternalTableRow(table, row);
    assertTrue(ir.isEnabled());
    ir.setRowChanging(true);
    ir.setEnabled(false);
    assertTrue(ir.isRowPropertiesChanged());
    ir.setRowChanging(false);
  }

  @Test
  public void testSetEnabled_NoChange() {
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    InternalTableRow ir = new InternalTableRow(table, row);
    assertTrue(ir.isEnabled());
    ir.setRowChanging(true);
    ir.setEnabled(true);
    assertFalse(ir.isRowPropertiesChanged());
    ir.setRowChanging(false);
  }

  @Test
  public void testSetIconId() {
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    InternalTableRow ir = new InternalTableRow(table, row);
    assertNull(ir.getIconId());
    ir.setRowChanging(true);
    ir.setIconId(TEST_ICON_ID);
    assertTrue(ir.isRowPropertiesChanged());
    ir.setRowChanging(false);
  }

  @Test
  public void testSetIconId_NoChange() {
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    InternalTableRow ir = new InternalTableRow(table, row);
    ir.setIconId(TEST_ICON_ID);
    assertEquals(TEST_ICON_ID, ir.getIconId());
    ir.setRowChanging(true);
    ir.setIconId(TEST_ICON_ID);
    assertFalse(ir.isRowPropertiesChanged());
    ir.setRowChanging(false);
  }

  @Test
  public void testChangedValue() {
    int[] cellChangeBits =
        new int[]{ICell.OBSERVER_BIT, ICell.VALUE_BIT, ICell.TEXT_BIT, ICell.ICON_ID_BIT, ICell.TOOLTIP_BIT, ICell.H_ALIGN_BIT, ICell.BG_COLOR_BIT, ICell.FG_COLOR_BIT, ICell.FONT_BIT, ICell.EDITABLE_BIT, ICell.CSS_CLASS_BIT,
            ICell.HTML_ENABLED_BIT, ICell.MANDATORY_BIT, ICell.ERROR_STATUS_BIT};
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    InternalTableRow ir = new InternalTableRow(table, row);
    ir.setRowChanging(true);
    for (int cellChangeBit : cellChangeBits) {
      ir.cellChanged(ir.getCell(0), cellChangeBit);
      assertEquals(1, ir.getUpdatedColumnIndexes(cellChangeBit).size());
      assertEquals(Integer.valueOf(0), CollectionUtility.firstElement(ir.getUpdatedColumnIndexes(cellChangeBit)));
    }
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
