/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractColumn}
 */
@RunWith(PlatformTestRunner.class)
public class AbstractColumnTest extends AbstractColumn<Object> {
  private static final String INVALID_MESSAGE = "invalid value";

  @Test
  public void testMapEditorFieldProperties() {
    String bColor = "469406";
    setBackgroundColor(bColor);
    String fColor = "FAAAF1";
    setForegroundColor(fColor);
    FontSpec fontSpec = new FontSpec("Arial", FontSpec.STYLE_ITALIC | FontSpec.STYLE_BOLD, 0);
    setFont(fontSpec);
    setMandatory(true);

    AbstractValueField<String> field = new AbstractValueField<String>() {
    };
    mapEditorFieldProperties(field);
    assertEquals("expected backgroundColor property to be progagated to field", bColor, field.getBackgroundColor());
    assertEquals("expected foregroundColor property to be progagated to field", fColor, field.getForegroundColor());
    assertEquals("expected font property to be progagated to field", fontSpec, field.getFont());
    assertTrue("expected mandatory property to be progagated to field", field.isMandatory());
  }

  /**
   * Invalid column values should have an error status
   */
  @Test
  public void testValidateColumn() throws ProcessingException {
    TestTable table = new TestTable();
    table.addRowsByArray(new String[]{"invalid", "a"});
    ICell c0 = table.getCell(0, 0);
    ICell c1 = table.getCell(1, 0);

    assertNoErrorStatus(c1);
    assertErrorStatus(c0);
    //invalid value is set on the table anyways
    assertEquals("invalid", c0.getValue());
    assertEquals("invalid", c0.getText());

    assertEquals("a", c1.getValue());
    assertEquals("a", c1.getText());
  }

  @Test
  public void testResetValidationError() throws ProcessingException {
    TestTable table = new TestTable();
    table.addRowsByArray(new String[]{"invalid"});
    table.getValidateTestColumn().setValue(0, "valid");
    ICell c0 = table.getCell(0, 0);
    assertNoErrorStatus(c0);
    assertEquals("valid", c0.getValue());
    assertEquals("valid", c0.getText());
  }

  /**
   * A displayable invalid column should become visible after validation
   */
  @Test
  public void testInvalidColumnVisible() throws ProcessingException {
    TestTable testTable = new TestTable();
    testTable.getValidateTestColumn().setVisible(false);
    testTable.addRowsByArray(new String[]{"invalid"});
    ICell c0 = testTable.getCell(0, 0);
    assertErrorStatus(c0);
    assertTrue(testTable.getValidateTestColumn().isVisible());
  }

  /**
   * Tests validation with a {@link VetoException}<br>
   * Invalid column values should have an error status
   */
  @Test
  public void testValidateVetoColumn() throws ProcessingException {
    TestVetoTable table = new TestVetoTable();
    table.addRowsByArray(new String[]{"invalid", "a"});
    ICell c0 = table.getCell(0, 0);
    ICell c1 = table.getCell(1, 0);

    assertErrorStatus(c0);
    assertNoErrorStatus(c1);
  }

  /**
   * Tests mandatory validation after setting property.
   */
  @Test
  public void testValidate_MandatoryChange() throws ProcessingException {
    TestVetoTable table = new TestVetoTable();
    table.addRowsByArray(new String[]{"", ""});
    table.addRowsByArray(new String[]{"", ""});
    table.getValidateTestColumn().setMandatory(true);
    ICell c0 = table.getCell(0, 0);
    assertNotNull(c0.getErrorStatus());
  }

  /**
   * Tests that the error status is correct on the table when a field cell is edited and throwing a
   * {@link VetoException}
   */
  @Test
  public void testValidateVetoColumn_CompleteEdit() throws Exception {
    TestVetoTable table = new TestVetoTable();
    table.addRowsByArray(new String[]{"valid", "a"});
    IStringField sf = new AbstractStringField() {
    };
    sf.setValue("invalid");
    table.getValidateTestColumn().completeEdit(table.getRow(0), sf);
    ICell c0 = table.getCell(0, 0);
    assertErrorStatus(c0);
  }

  private void assertErrorStatus(ICell c) {
    assertNotNull(String.format("The invalid cell should have an error status: value '%s'", c.getValue()), c.getErrorStatus());
    assertEquals(INVALID_MESSAGE, c.getErrorStatus().getMessage());
  }

  private void assertNoErrorStatus(ICell c) {
    assertNull(String.format("The valid cell should not have an error status: value '%s'", c.getValue()), c.getErrorStatus());
  }

  public class TestTable extends AbstractTable {

    public ValidateTestColumn getValidateTestColumn() {
      return getColumnSet().getColumnByClass(ValidateTestColumn.class);
    }

    public class ValidateTestColumn extends AbstractStringColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected String execValidateValue(ITableRow row, String rawValue) throws ProcessingException {
        Cell cell = row.getCellForUpdate(this);
        if ("invalid".equals(rawValue)) {
          cell.addErrorStatus(INVALID_MESSAGE);
        }
        else {
          cell.clearErrorStatus();
        }

        return rawValue;
      }
    }
  }

  public class TestVetoTable extends AbstractTable {

    public ValidateTestColumn getValidateTestColumn() {
      return getColumnSet().getColumnByClass(ValidateTestColumn.class);
    }

    @Order(70.0)
    public class ValidateTestColumn extends AbstractStringColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected String execValidateValue(ITableRow row, String rawValue) throws ProcessingException {
        if ("invalid".equals(rawValue)) {
          throw new VetoException(INVALID_MESSAGE);
        }
        return rawValue;
      }
    }
  }

}
