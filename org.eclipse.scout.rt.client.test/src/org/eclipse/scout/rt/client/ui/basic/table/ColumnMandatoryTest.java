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
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the mandatory property in a table (model)
 */
public class ColumnMandatoryTest {

  /**
   * Tests that column is not mandatory by default
   */
  @Test
  public void testInitiallyMandatory() throws ProcessingException {
    AbstractColumn<String> column = getEmptyStringColumn();
    Assert.assertFalse(column.isMandatory());
  }

  /**
   * Tests setting the mandatory property
   */
  @Test
  public void testSettingProperty() throws ProcessingException {
    AbstractColumn<String> column = getEmptyStringColumn();
    column.setMandatory(true);
    Assert.assertTrue(column.isMandatory());
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
   * Tests, if a field is mandatory in a mandatory column
   */
  @Test
  public void testFieldMandatoryInColumn() throws ProcessingException {
    MandatoryTestTable testTable = new MandatoryTestTable();
    testTable.addRowByArray(getTestRow());
    IColumn mandatoryCol = testTable.getMandatoryTestColumn();
    IFormField field = mandatoryCol.prepareEdit(testTable.getRow(0));
    Assert.assertTrue(mandatoryCol.isMandatory());
    Assert.assertTrue(field.isMandatory());
    Assert.assertTrue(field.isContentValid());
  }

  /**
   * Tests, if a field is mandatory in a mandatory column
   */
  @Test
  public void testValidationOnMandatoryColumn() throws ProcessingException {
    MandatoryTestTable testTable = new MandatoryTestTable();
    testTable.addRowByArray(getEmptyTestRow());
    IColumn mandatoryCol = testTable.getMandatoryTestColumn();
    IFormField field = mandatoryCol.prepareEdit(testTable.getRow(0));
    Assert.assertFalse(field.isContentValid());
  }

  /**
   * Tests, if a field is not mandatory in a non-mandatory column
   */
  @Test
  public void testFieldNotMandatoryInColumn() throws ProcessingException {
    MandatoryTestTable testTable = new MandatoryTestTable();
    testTable.addRowByArray(getTestRow());
    IColumn col = testTable.getNonMandatoryTestColumn();
    IFormField field = col.prepareEdit(testTable.getRow(0));
    Assert.assertFalse(col.isMandatory());
    Assert.assertFalse(field.isMandatory());
  }

  /**
   * A test table with two editable columns: Mandatory and Non-mandatory column
   */
  @Order(10.0)
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
    @Order(10.0)
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
    @Order(10.0)
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
