/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
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

  /**
   * A table with a test column where only the first row is editable.
   */
  @Order(10.0)
  public class TestTable extends AbstractTable {
    @Order(10.0)
    public class TestColumn extends AbstractStringColumn {
    }

  }

}
