/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.platform.Order;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link AbstractBooleanColumn} with {@link AbstractBooleanColumn#isTriStateEnabled()} set to true
 */
public class AbstractBooleanColumnWithTriStateTest {

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
    assertNull(value);
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
    assertNull(value);
    column.setEditable(true);
    //toggle
    IBooleanField checkbox = (IBooleanField) table.getTestBooleanColumn().prepareEdit(row);
    checkbox.toggleValue();
    table.getTestBooleanColumn().completeEdit(row, checkbox);
    value = table.getTestBooleanColumn().getValue(0);
    assertNotNull(value);
    assertFalse(value);
    //toggle
    checkbox = (IBooleanField) table.getTestBooleanColumn().prepareEdit(row);
    checkbox.toggleValue();
    table.getTestBooleanColumn().completeEdit(row, checkbox);
    value = table.getTestBooleanColumn().getValue(0);
    assertNotNull(value);
    assertTrue(value);
  }

  @Test
  public void testChangeTriStateEnabled() {
    TestTable table = new TestTable();
    TestTable.TestBooleanColumn column = table.getTestBooleanColumn();
    table.addRowByArray(new Object[]{null});
    table.addRowByArray(new Object[]{null});
    table.addRowByArray(new Object[]{true});
    table.addRowByArray(new Object[]{false});

    assertNull(column.getValue(0));
    assertNull(column.getValue(1));
    assertTrue(column.getValue(2));
    assertFalse(column.getValue(3));

    column.setTriStateEnabled(false);
    assertNotNull(column.getValue(0));
    assertFalse(column.getValue(0));
    assertNotNull(column.getValue(1));
    assertFalse(column.getValue(1));
    assertTrue(column.getValue(2));
    assertFalse(column.getValue(3));
  }

  public class TestTable extends AbstractTable {

    public TestBooleanColumn getTestBooleanColumn() {
      return getColumnSet().getColumnByClass(TestBooleanColumn.class);
    }

    @Order(10)
    public class TestBooleanColumn extends AbstractBooleanColumn {

      @Override
      protected boolean getConfiguredTriStateEnabled() {
        return true;
      }
    }
  }
}
