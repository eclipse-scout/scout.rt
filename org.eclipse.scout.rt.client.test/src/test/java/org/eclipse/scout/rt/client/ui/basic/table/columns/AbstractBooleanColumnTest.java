/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.platform.Order;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link AbstractBooleanColumn}
 */
public class AbstractBooleanColumnTest {

  @Test
  public void testPrepareEditInternal() {
    AbstractBooleanColumn column = new AbstractBooleanColumn() {
    };
    column.setMandatory(true);
    ITableRow row = Mockito.mock(ITableRow.class);
    IBooleanField field = (IBooleanField) column.prepareEditInternal(row);
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
  }

  @Test
  public void testInitNull() {
    TestTable table = new TestTable();
    table.addRowByArray(new Object[]{null});
    Boolean value = table.getTestBooleanColumn().getValue(0);
    assertNotNull(value);
    assertFalse(value);
  }

  @Test
  public void testInitFalse() {
    TestTable table = new TestTable();
    table.addRowByArray(new Object[]{false});
    Boolean value = table.getTestBooleanColumn().getValue(0);
    assertNotNull(value);
    assertFalse(value);
  }

  @Test
  public void testInitTrue() {
    TestTable table = new TestTable();
    table.addRowByArray(new Object[]{true});
    Boolean value = table.getTestBooleanColumn().getValue(0);
    assertNotNull(value);
    assertTrue(value);
  }

  @Test
  public void testToggle() {
    TestTable table = new TestTable();
    TestTable.TestBooleanColumn column = table.getTestBooleanColumn();
    ITableRow row = table.addRowByArray(new Object[]{null});
    Boolean value = column.getValue(0);
    assertNotNull(value);
    assertFalse(value);
    column.setEditable(true);
    //toggle
    IBooleanField checkbox = (IBooleanField) table.getTestBooleanColumn().prepareEdit(row);
    checkbox.toggleValue();
    table.getTestBooleanColumn().completeEdit(row, checkbox);
    value = table.getTestBooleanColumn().getValue(0);
    assertNotNull(value);
    assertTrue(value);
    //toggle
    checkbox = (IBooleanField) table.getTestBooleanColumn().prepareEdit(row);
    checkbox.toggleValue();
    table.getTestBooleanColumn().completeEdit(row, checkbox);
    value = table.getTestBooleanColumn().getValue(0);
    assertNotNull(value);
    assertFalse(value);
  }

  public class TestTable extends AbstractTable {

    public TestBooleanColumn getTestBooleanColumn() {
      return getColumnSet().getColumnByClass(TestBooleanColumn.class);
    }

    @Order(10)
    public class TestBooleanColumn extends AbstractBooleanColumn {
    }
  }
}
