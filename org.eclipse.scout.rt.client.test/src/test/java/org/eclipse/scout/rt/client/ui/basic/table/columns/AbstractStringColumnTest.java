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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumnTest.TestTable.TestStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link AbstractStringColumn}
 */
public class AbstractStringColumnTest {

  @Test
  public void testPrepareEditInternal() {
    AbstractStringColumn column = new AbstractStringColumn() {
    };
    column.setCssClass("myCSSClass");
    column.setInputMasked(true);
    column.setMandatory(true);
    ITableRow row = Mockito.mock(ITableRow.class);
    IStringField field = (IStringField) column.prepareEditInternal(row);
    assertEquals("input masked property to be progagated to field", column.isInputMasked(), field.isInputMasked());
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
  }

  @Test
  public void testCustomValidation() {
    TestTable table = new TestTable();
    TestStringColumn col = table.getTestStringColumn();
    ITableRow row = table.addRowByArray(new Object[]{"valid"});
    IStringField editor = (IStringField) col.prepareEdit(row);
    editor.setValue("invalid");
    col.completeEdit(row, editor);
    assertFalse(table.getCell(0, 0).isContentValid());
  }

  @Test
  public void testMaxLengthValidation() {
    TestTable table = new TestTable();
    TestStringColumn col = table.getTestStringColumn();
    col.setMaxLength(1);
    table.addRowByArray(new Object[]{"text"});
    assertEquals("t", table.getCell(0, 0).getValue());
  }

  public class TestTable extends AbstractTable {

    public TestStringColumn getTestStringColumn() {
      return getColumnSet().getColumnByClass(TestStringColumn.class);
    }

    @Order(70)
    public class TestStringColumn extends AbstractStringColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected String execValidateValue(ITableRow row, String rawValue) {
        if ("invalid".equals(rawValue)) {
          throw new VetoException("invalid");
        }
        return super.execValidateValue(row, rawValue);
      }
    }

  }

}
