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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumnTest.TestDecorationTable.C1Column;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.IIntegerField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractColumn}
 */
@RunWith(PlatformTestRunner.class)
public class AbstractColumnTest extends AbstractColumn<Object> {
  private static final String INVALID = "invalid";
  private static final String VALID = "valid";
  private static final String INVALID_MESSAGE = "invalid value";

  /**
   * Tests that column properties are mapped correctly to editor field. {@link #mapEditorFieldProperties(IFormField)}
   */
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
   * Test that value, text and error status are correct after adding a valid row to a table with
   * {@link AbstractTable#addRowByArray(Object)}
   */
  @Test
  public void testValidColumn() {
    TestTable table = new TestTable();
    table.addRowsByArray(new String[]{VALID});
    ICell c0 = table.getCell(0, 0);
    assertTrue(table.getValidateTestColumn().isContentValid(table.getRow(0)));
    assertNoErrorStatus(c0);
    assertEquals(VALID, c0.getValue());
    assertEquals(VALID, c0.getText());
  }

  /**
   * Test that value, text and error status are correct after adding an invalid row to a table with
   * {@link AbstractTable#addRowByArray(Object)}
   */
  @Test
  public void testInvalidColumn() {
    TestTable table = new TestTable();
    table.addRowsByArray(new String[]{INVALID, "a"});
    ICell c0 = table.getCell(0, 0);
    ICell c1 = table.getCell(1, 0);

    assertFalse(table.getValidateTestColumn().isContentValid(table.getRow(0)));
    assertNoErrorStatus(c1);
    assertErrorStatus(c0);
    //invalid value is set on the table anyways
    assertEquals(INVALID, c0.getValue());
    assertEquals(INVALID, c0.getText());

    assertEquals("a", c1.getValue());
    assertEquals("a", c1.getText());
  }

  /**
   * Test that setting a valid value in an initially invalid table resets the error.
   */
  @Test
  public void testResetValidationError() {
    TestTable table = new TestTable();
    table.addRowsByArray(new String[]{INVALID});
    table.getValidateTestColumn().setValue(0, VALID);
    ICell c0 = table.getCell(0, 0);
    assertTrue(table.getValidateTestColumn().isContentValid(table.getRow(0)));
    assertNoErrorStatus(c0);
    assertEquals(VALID, c0.getValue());
    assertEquals(VALID, c0.getText());
  }

  /**
   * Test that setting a valid value using the cell editor in an initially invalid table resets the error.
   */
  @Test
  public void testResetValidationError_UsingField() {
    TestTable table = new TestTable();
    table.addRowsByArray(new String[]{"aaa"});
    ITableRow testRow = table.getRow(0);

    TestTable.ValidateTestColumn testColumn = table.getValidateTestColumn();
    ColumnTestUtility.editCellValue(testRow, testColumn, INVALID);

    ICell c0 = table.getCell(0, 0);
    assertFalse(testColumn.isContentValid(testRow));
    assertEquals("aaa", c0.getValue());
    assertEquals(INVALID, c0.getText());
  }

  /**
   * A displayable invalid column should become visible after validation
   */
  @Test
  public void testInvalidColumnVisible() {
    TestTable testTable = new TestTable();
    testTable.getValidateTestColumn().setVisible(false);
    testTable.addRowsByArray(new String[]{INVALID});
    ICell c0 = testTable.getCell(0, 0);
    assertErrorStatus(c0);
    assertTrue(testTable.getValidateTestColumn().isVisible());
  }

  /**
   * Tests validation with a {@link VetoException}<br>
   * Invalid column values should have an error status
   */
  @Test
  public void testValidateVetoColumn() {
    TestVetoTable table = new TestVetoTable();
    table.addRowsByArray(new String[]{INVALID, "a"});
    ICell c0 = table.getCell(0, 0);
    ICell c1 = table.getCell(1, 0);

    assertErrorStatus(c0);
    assertNoErrorStatus(c1);
  }

  /**
   * Tests mandatory validation after setting property.
   */
  @Test
  public void testValidate_MandatoryChange() {
    TestVetoTable table = new TestVetoTable();
    table.addRowsByArray(new String[]{"", ""});
    table.addRowsByArray(new String[]{"", ""});
    table.getValidateTestColumn().setMandatory(true);
    ICell c0 = table.getCell(0, 0);
    assertFalse(c0.isContentValid());
  }

  @Test
  public void testValidColumn_EditField() throws Exception {
    TestVetoTable table = new TestVetoTable();
    table.addRowsByArray(new String[]{VALID, "a"});
    TestVetoTable.ValidateTestColumn testColumn = table.getValidateTestColumn();
    testColumn.setMandatory(true);
    IValueField field = (IValueField) testColumn.prepareEdit(table.getRow(0));
    assertEquals(VALID, field.getValue());
  }

  @Test
  public void testValidColumn_EditField1() throws Exception {
    TestVetoTable table = new TestVetoTable();
    TestVetoTable.ValidateTestColumn testColumn = table.getValidateTestColumn();
    testColumn.setMandatory(true);
    table.addRowsByArray(new String[]{null, "a"});

    ColumnTestUtility.editCellValue(table.getRow(0), table.getValidateTestColumn(), VALID);
    assertEquals(VALID, testColumn.getValue(0));
  }

  /**
   * Tests that the error status is correct on the table when a field cell is edited and throwing a
   * {@link VetoException}
   */
  @Test
  public void testValidateVetoColumn_EditField() throws Exception {
    TestVetoTable table = new TestVetoTable();
    table.addRowsByArray(new String[]{VALID, "a"});
    ColumnTestUtility.editCellValue(table.getRow(0), table.getValidateTestColumn(), INVALID);
    ICell c0 = table.getCell(0, 0);
    assertErrorStatus(c0);
    assertEquals(INVALID, c0.getText());
  }

  @Test
  public void testCompleteEdit_ParsingError() {
    ParsingTestTable table = new ParsingTestTable();
    table.addRowsByArray(new Integer[]{0});
    IIntegerField field = new AbstractIntegerField() {
    };
    field.parseAndSetValue("invalid number");
    table.getIntTestColumn().completeEdit(table.getRow(0), field);
    ICell c = table.getCell(0, 0);
    assertEquals("invalid number", c.getText());
    assertEquals(0, c.getValue());
    assertNotNull(String.format("The invalid cell should have an error status: value '%s'", c.getValue(), c.getErrorStatus()));
  }

  @Test
  public void testNoInitialDecoration() throws Exception {
    TestVetoTable table = new TestVetoTable();
    table.addRowsByArray(new String[]{"a"});
    ICell c0 = table.getCell(0, 0);
    assertEquals(null, c0.getCssClass());
    assertEquals(1, table.getValidateTestColumn().getDecorateCount());
  }

  @Test
  public void testInitialDecoration() {
    TestVetoTable table = new TestVetoTable();
    table.addRowsByArray(new String[]{"decorate"});
    ICell c0 = table.getCell(0, 0);
    assertEquals("decorated", c0.getCssClass());
    assertEquals(1, table.getValidateTestColumn().getDecorateCount());
  }

  @Test
  public void testDecoration_SetValue() {
    TestVetoTable table = new TestVetoTable();
    table.addRowsByArray(new String[]{"b"});
    table.getValidateTestColumn().setValue(0, "decorate");
    ICell c0 = table.getCell(0, 0);
    assertEquals("decorated", c0.getCssClass());
    assertEquals(2, table.getValidateTestColumn().getDecorateCount());
  }

  @Test
  public void testDecorationsAfterAllInserts() throws Exception {
    TestDecorationTable table = new TestDecorationTable();
    table.addRowsByArray(new String[]{"a", "b"});
  }

  private void assertErrorStatus(ICell c) {
    assertNotNull(String.format("The invalid cell should have an error status: value '%s'", c.getValue(), c.getErrorStatus()));
    assertEquals(INVALID_MESSAGE, c.getErrorStatus().getMessage());
  }

  private void assertNoErrorStatus(ICell c) {
    assertNull(String.format("The valid cell should not have an error status: value '%s'", c.getValue()), c.getErrorStatus());
  }

  @Test
  public void testUpdateDisplayText() {
    final TestDecorationTable table = new TestDecorationTable();
    ITableRow row = table.addRow();
    table.getC1Column().setValue(row, "newValue");
    assertEquals("newValue", table.getC1Column().getDisplayText(table.getRow(0)));
  }

  @Test
  public void testInvisibleContextColumn() {
    TestDecorationTable table = new TestDecorationTable();
    table.addRowsByArray(new String[]{"a", "b"});
    C1Column c1 = table.getC1Column();
    assertTrue(c1.isVisible());
    table.getUIFacade().setContextColumnFromUI(c1);
    assertSame(c1, table.getContextColumn());

    c1.setVisible(false);
    assertNull(table.getContextColumn());
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
      protected String execValidateValue(ITableRow row, String rawValue) {
        Cell cell = row.getCellForUpdate(this);
        if (INVALID.equals(rawValue)) {
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

    @Order(70)
    public class ValidateTestColumn extends AbstractStringColumn {
      private int m_decorateCount = 0;

      public int getDecorateCount() {
        return m_decorateCount;
      }

      @Override
      protected void execDecorateCell(Cell cell, ITableRow row) {
        m_decorateCount++;
        if ("decorate".equals(cell.getValue())) {
          cell.setCssClass("decorated");
        }
      }

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected String execValidateValue(ITableRow row, String rawValue) {
        if (INVALID.equals(rawValue)) {
          throw new VetoException(INVALID_MESSAGE);
        }
        return rawValue;
      }
    }
  }

  public class TestDecorationTable extends AbstractTable {

    public C1Column getC1Column() {
      return getColumnSet().getColumnByClass(C1Column.class);
    }

    public C2Column getC2Column() {
      return getColumnSet().getColumnByClass(C2Column.class);
    }

    @Order(70)
    public class C1Column extends AbstractStringColumn {
    }

    @Order(70)
    public class C2Column extends AbstractStringColumn {

      @Override
      protected void execDecorateCell(Cell cell, ITableRow row) {
        if (getC1Column().getValue(row) == null) {
          throw new ProcessingException("decoration on empty column");
        }
      }
    }
  }

  public class ParsingTestTable extends AbstractTable {

    public IntTestColumn getIntTestColumn() {
      return getColumnSet().getColumnByClass(IntTestColumn.class);
    }

    @Order(70)
    public class IntTestColumn extends AbstractIntegerColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }
    }
  }

}
