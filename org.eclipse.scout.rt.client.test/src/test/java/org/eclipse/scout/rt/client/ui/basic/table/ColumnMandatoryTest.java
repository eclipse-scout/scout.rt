/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnMandatoryTest.MandatoryTestTable.NonMandatoryTestColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the mandatory property in a table (model)
 */
@RunWith(PlatformTestRunner.class)
public class ColumnMandatoryTest {

  /**
   * Tests that column is not mandatory by default
   */
  @Test
  public void testInitiallyMandatory() {
    AbstractColumn<String> column = getEmptyStringColumn();
    assertFalse(column.isMandatory());
  }

  /**
   * Tests setting the mandatory property
   */
  @Test
  public void testSettingProperty() {
    MandatoryTestTable testTable = new MandatoryTestTable();
    final NonMandatoryTestColumn column = testTable.getNonMandatoryTestColumn();
    column.setMandatory(true);
    testTable.addRowByArray(getEmptyTestRow());
    final Cell cell = (Cell) testTable.getCell(0, 0);
    assertFalse(cell.isContentValid());
    assertTrue(column.isMandatory());
  }

  /**
   * Tests, if a field is mandatory by setting value
   */
  @Test
  public void testMandatoryErrorIfNoValue() {
    MandatoryTestTable testTable = new MandatoryTestTable();
    testTable.addRowByArray(getTestRow());
    IColumn mandatoryCol = testTable.getMandatoryTestColumn();
    IFormField field = mandatoryCol.prepareEdit(testTable.getRow(0));
    assertTrue(mandatoryCol.isMandatory());
    assertTrue(field.isMandatory());
    assertTrue(field.isContentValid());
  }

  /**
   * Tests mandatory column with empty value
   */
  @Test
  public void testFieldMandatoryInColumn() {
    MandatoryTestTable testTable = new MandatoryTestTable();
    testTable.addRowByArray(getEmptyTestRow());
    final Cell cell = (Cell) testTable.getCell(0, 0);
    assertFalse(cell.isContentValid());
  }

  /**
   * Tests, if a field is mandatory in a mandatory column
   */
  @Test
  public void testValidationOnMandatoryColumn() {
    MandatoryTestTable testTable = new MandatoryTestTable();
    testTable.addRowByArray(getEmptyTestRow());
    IColumn mandatoryCol = testTable.getMandatoryTestColumn();
    IFormField field = mandatoryCol.prepareEdit(testTable.getRow(0));
    assertFalse(field.isContentValid());
  }

  /**
   * Tests, if a field is not mandatory in a non-mandatory column
   */
  @Test
  public void testFieldNotMandatoryInColumn() {
    MandatoryTestTable testTable = new MandatoryTestTable();
    testTable.addRowByArray(getTestRow());
    IColumn col = testTable.getNonMandatoryTestColumn();
    IFormField field = col.prepareEdit(testTable.getRow(0));
    assertFalse(col.isMandatory());
    assertFalse(field.isMandatory());
  }

  private AbstractColumn<String> getEmptyStringColumn() {
    return new AbstractColumn<String>() {
    };
  }

  private String[] getTestRow() {
    return new String[]{"a", "b"};
  }

  private String[] getEmptyTestRow() {
    return new String[]{null, null};
  }

  /**
   * A test table with two editable columns: Mandatory and Non-mandatory column
   */
  public class MandatoryTestTable extends AbstractTable {

    public MandatoryTestTable() {
      setEnabled(true);
    }

    public MandatoryTestColumn getMandatoryTestColumn() {
      return getColumnSet().getColumnByClass(MandatoryTestColumn.class);
    }

    public NonMandatoryTestColumn getNonMandatoryTestColumn() {
      return getColumnSet().getColumnByClass(NonMandatoryTestColumn.class);
    }

    /**
     * A mandatory column
     */
    @Order(10)
    public class MandatoryTestColumn extends AbstractStringColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected boolean getConfiguredMandatory() {
        return true;
      }
    }

    /**
     * A non-mandatory column
     */
    @Order(10)
    public class NonMandatoryTestColumn extends AbstractStringColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected boolean getConfiguredMandatory() {
        return false;
      }
    }
  }
}
