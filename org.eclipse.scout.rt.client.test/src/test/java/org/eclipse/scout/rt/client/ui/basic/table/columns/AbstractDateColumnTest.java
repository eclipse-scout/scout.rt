/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumnTest.TestTable.TestDateColumn;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractDateColumn}
 */
@RunWith(PlatformTestRunner.class)
public class AbstractDateColumnTest {

  private static final String TEST_FORMAT1 = "YYYY-MM-dd";

  @Test
  public void testPrepareEditInternal() {
    AbstractDateColumn column = new AbstractDateColumn() {
    };
    column.setMandatory(true);
    column.setHasTime(true);
    ITableRow row = mock(ITableRow.class);
    IDateField field = (IDateField) column.prepareEditInternal(row);
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
    assertEquals("mandatory property to be progagated to field", column.isHasTime(), field.isHasTime());
  }

  @Test
  public void testCompleteEdit_ParsingError() {
    TestTable table = new TestTable();
    Date date = new Date();
    table.addRowsByArray(new Date[]{date});
    ITableRow row = table.getRow(0);

    setParseErrorInUI(row, table.getTestDateColumn());

    ICell c = table.getCell(0, 0);
    assertEquals("invalid", c.getText());
    assertEquals(date, c.getValue());
    assertNotNull(String.format("The invalid cell should have an error status: value '%s'", c.getValue(), c.getErrorStatus()));
  }

  private void setParseErrorInUI(ITableRow row, AbstractDateColumn column) {
    AbstractDateField field = (AbstractDateField) column.prepareEdit(row);
    field.getUIFacade().setDisplayTextFromUI("invalid");
    field.getUIFacade().setErrorStatusFromUI(new ParsingFailedStatus("Parsing failed", "invalid"));
    column.completeEdit(row, field);
  }

  @Test
  public void testConfiguredDateFormat() throws Exception {
    Date testDate = new Date();
    SimpleDateFormat df = new SimpleDateFormat(TEST_FORMAT1, NlsLocale.get());
    TestTable table = new TestTable();
    table.addRowsByArray(new Object[]{testDate});
    ICell cell = table.getCell(0, 0);
    assertTrue(cell.getValue() instanceof Date);
    assertEquals(df.format(testDate), cell.getText());
  }

  /**
   * Tests that the cell text changes to the correct format, if the format is set on a column
   */
  @Test
  public void testChangeFormat() {
    Date testDate = new Date();
    String testFormat = "YYYY--MM--dd";
    SimpleDateFormat df = new SimpleDateFormat(testFormat, NlsLocale.get());

    TestTable table = new TestTable();
    table.addRowsByArray(new Object[]{testDate});
    TestDateColumn col = table.getTestDateColumn();
    col.setFormat(testFormat);
    ICell cell = table.getCell(0, 0);
    assertTrue(cell.getValue() instanceof Date);
    assertEquals(df.format(testDate), cell.getText());
  }

  /**
   * Tests that the cell text changes to the correct format, if hasTime is changed
   */
  @Test
  public void testHasTimeChange() {
    Date testDate = new Date();
    TestTable table = new TestTable();
    TestDateColumn col = table.getTestDateColumn();
    col.setFormat(null);
    table.addRowsByArray(new Object[]{testDate});
    String dateOnlyText = table.getCell(0, 0).getText();
    col.setHasTime(true);
    String dateTimeText = table.getCell(0, 0).getText();
    assertTrue(dateTimeText.length() > dateOnlyText.length());
  }

  /**
   * Tests that the cell text changes to the correct format, if hasTime is changed for an editable table
   */
  @Test
  public void testHasTime_EditableChange() {
    Date testDate = new Date();
    TestTable table = new TestTable();
    TestDateColumn col = table.getTestDateColumn();
    col.setFormat(null);
    col.setEditable(true);
    table.addRowsByArray(new Object[]{testDate});
    String dateOnlyText = table.getCell(0, 0).getText();
    col.setHasTime(true);
    String dateTimeText = table.getCell(0, 0).getText();
    assertTrue(dateTimeText.length() > dateOnlyText.length());
  }

  @Test
  public void testHasDate() {
    Date testDate = new Date();
    TestTable table = new TestTable();
    TestDateColumn col = table.getTestDateColumn();
    col.setFormat(null);
    col.setHasDate(false);
    col.setHasTime(true);
    table.addRowsByArray(new Object[]{testDate});
    String timeOnlyText = table.getCell(0, 0).getText();
    col.setHasDate(true);
    String dateTimeText = table.getCell(0, 0).getText();
    assertTrue(dateTimeText.length() > timeOnlyText.length());
  }

  public class TestTable extends AbstractTable {

    public TestDateColumn getTestDateColumn() {
      return getColumnSet().getColumnByClass(TestDateColumn.class);
    }

    @Order(70)
    public class TestDateColumn extends AbstractDateColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected String getConfiguredFormat() {
        return TEST_FORMAT1;
      }
    }

  }

}
