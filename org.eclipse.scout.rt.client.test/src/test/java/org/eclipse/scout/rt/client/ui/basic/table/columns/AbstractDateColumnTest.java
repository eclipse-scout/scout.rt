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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumnTest.TestTable.TestDateColumn;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.junit.Test;

/**
 * Tests for {@link AbstractDateColumn}
 */
public class AbstractDateColumnTest {

  private static final String TEST_FORMAT1 = "YYYY-MM-dd";

  @Test
  public void testPrepareEditInternal() throws ProcessingException {
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
  public void testChangeFormat() throws Exception {
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

  public class TestTable extends AbstractTable {

    public TestDateColumn getTestDateColumn() {
      return getColumnSet().getColumnByClass(TestDateColumn.class);
    }

    @Order(70.0)
    public class TestDateColumn extends AbstractDateColumn {

      @Override
      protected String getConfiguredFormat() {
        return TEST_FORMAT1;
      }
    }
  }

}
