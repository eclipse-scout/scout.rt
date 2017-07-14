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
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateTimeColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractObjectColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn2;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractTimeColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.9.1
 */
@RunWith(PlatformTestRunner.class)
public class TableUtilityTest {
  private static final long DATE_IN_MILLIS = 1372324222000L; // Thu Jun 27 11:10:22 CEST 2013
  private static final long DATE_IN_MILLIS_2 = 1370324222000L; // Tue Jun 04 07:37:02 CEST 2013
  private static final int COLUMNS_AMOUNT = 12;

  private static final Object OBJECT_ROW_1 = new Object();
  private static final Object OBJECT_ROW_2 = new Object();

  private static P_Table m_table;

  @BeforeClass
  public static void setupTable() throws Exception {
    m_table = createTable(ITableRow.STATUS_NON_CHANGED);

    assertEquals(2, m_table.getRowCount());
    assertEquals(COLUMNS_AMOUNT, m_table.getColumnCount());
  }

  @Test
  public void testExportRowsAsCSVwithColumnNamesTypesFormats() throws Exception {
    Object[][] result = TableUtility.exportRowsAsCSV(m_table.getRows(), m_table.getColumns(), true, true, true);

    assertEquals(5, result.length);
    assertColumnNames(result[0]);
    assertColumnTypes(result[1]);
    assertColumnFormats(result[2]);
    assertRow1(result[3]);
    assertRow2(result[4]);
  }

  @Test
  public void testExportRowsAsCSVwithColumnNamesTypes() throws Exception {
    Object[][] result = TableUtility.exportRowsAsCSV(m_table.getRows(), m_table.getColumns(), true, true, false);

    assertEquals(4, result.length);
    assertColumnNames(result[0]);
    assertColumnTypes(result[1]);
    assertRow1(result[2]);
    assertRow2(result[3]);
  }

  @Test
  public void testExportRowsAsCSVwithColumnNamesFormats() throws Exception {
    Object[][] result = TableUtility.exportRowsAsCSV(m_table.getRows(), m_table.getColumns(), true, false, true);

    assertEquals(4, result.length);
    assertColumnNames(result[0]);
    assertColumnFormats(result[1]);
    assertRow1(result[2]);
    assertRow2(result[3]);
  }

  @Test
  public void testExportRowsAsCSVwithColumnTypesFormats() throws Exception {
    Object[][] result = TableUtility.exportRowsAsCSV(m_table.getRows(), m_table.getColumns(), false, true, true);

    assertEquals(4, result.length);
    assertColumnTypes(result[0]);
    assertColumnFormats(result[1]);
    assertRow1(result[2]);
    assertRow2(result[3]);
  }

  @Test
  public void testExportRowsAsCSVwithColumnNames() throws Exception {
    Object[][] result = TableUtility.exportRowsAsCSV(m_table.getRows(), m_table.getColumns(), true, false, false);

    assertEquals(3, result.length);
    assertColumnNames(result[0]);
    assertRow1(result[1]);
    assertRow2(result[2]);
  }

  @Test
  public void testExportRowsAsCSVwithColumnTypes() throws Exception {
    Object[][] result = TableUtility.exportRowsAsCSV(m_table.getRows(), m_table.getColumns(), false, true, false);

    assertEquals(3, result.length);
    assertColumnTypes(result[0]);
    assertRow1(result[1]);
    assertRow2(result[2]);
  }

  @Test
  public void testExportRowsAsCSVwithColumnFormats() throws Exception {
    Object[][] result = TableUtility.exportRowsAsCSV(m_table.getRows(), m_table.getColumns(), false, false, true);

    assertEquals(3, result.length);
    assertColumnFormats(result[0]);
    assertRow1(result[1]);
    assertRow2(result[2]);
  }

  @Test
  public void testExportRowsAsCSVwithoutColumnNamesTypesFormats() throws Exception {
    Object[][] result = TableUtility.exportRowsAsCSV(m_table.getRows(), m_table.getColumns(), false, false, false);

    assertEquals(2, result.length);
    assertRow1(result[0]);
    assertRow2(result[1]);
  }

  private void assertColumnNames(Object[] columnNames) {
    assertEquals(COLUMNS_AMOUNT, columnNames.length);
    assertEquals("TestBooleanColumn", columnNames[0]);
    assertEquals("TestDateColumn", columnNames[1]);
    assertEquals("TestDateTimeColumn", columnNames[2]);
    assertEquals("TestTimeColumn", columnNames[3]);
    assertEquals("TestBigDecimalColumn", columnNames[4]);
    assertEquals("TestBigDecimalColumn2", columnNames[5]);
    assertEquals("TestIntegerColumn", columnNames[6]);
    assertEquals("TestLongColumn", columnNames[7]);
    assertEquals("TestObjectColumn", columnNames[8]);
    assertEquals("TestSmartColumn", columnNames[9]);
    assertEquals("TestStringColumn", columnNames[10]);
    assertEquals("TestStringHtmlColumn", columnNames[11]);
  }

  private void assertColumnTypes(Object[] columnTypes) {
    assertEquals(COLUMNS_AMOUNT, columnTypes.length);
    assertEquals(Boolean.class, columnTypes[0]);
    assertEquals(Date.class, columnTypes[1]);
    assertEquals(Timestamp.class, columnTypes[2]);
    assertEquals(Timestamp.class, columnTypes[3]);
    assertEquals(Double.class, columnTypes[4]);
    assertEquals(Double.class, columnTypes[5]);
    assertEquals(Integer.class, columnTypes[6]);
    assertEquals(Long.class, columnTypes[7]);
    assertEquals(String.class, columnTypes[8]);
    assertEquals(String.class, columnTypes[9]);
    assertEquals(String.class, columnTypes[10]);
    assertEquals(String.class, columnTypes[11]);
  }

  private void assertColumnFormats(Object[] columnFormats) {
    assertEquals(COLUMNS_AMOUNT, columnFormats.length);
    assertNull(columnFormats[0]);
    assertEquals("dd.MM.yyyy", columnFormats[1]);
    assertEquals("dd.MM.yy hh:mm:ss", columnFormats[2]);
    assertEquals("hh:mm:ss", columnFormats[3]);
    assertEquals("#0.0000", columnFormats[4]);
    assertEquals("#0.000", columnFormats[5]);
    assertEquals("#,##0.0", columnFormats[6]);
    assertEquals("#,##0", columnFormats[7]);
    assertNull(columnFormats[8]);
    assertNull(columnFormats[9]);
    assertNull(columnFormats[10]);
    assertNull(columnFormats[11]);
  }

  private void assertRow1(Object[] row1) {
    assertEquals(COLUMNS_AMOUNT, row1.length);
    Date date = new Date(DATE_IN_MILLIS);
    assertEquals("X", row1[0]);
    assertEquals(date, row1[1]);
    assertEquals(date, row1[2]);
    assertEquals(date, row1[3]);
    assertEquals(BigDecimal.valueOf(111.2233D), row1[4]);
    assertEquals(BigDecimal.valueOf(111.223D), row1[5]);
    assertEquals(3333, row1[6]);
    assertEquals(4444L, row1[7]);
    assertEquals("", row1[8]); // no display text
    assertEquals("", row1[9]); // no display text
    assertEquals("Foo", row1[10]);
    assertEquals("Hello", row1[11]);
  }

  private void assertRow2(Object[] row2) {
    assertEquals(COLUMNS_AMOUNT, row2.length);
    Date date = new Date(DATE_IN_MILLIS_2);
    assertEquals("", row2[0]);
    assertEquals(date, row2[1]);
    assertEquals(date, row2[2]);
    assertEquals(date, row2[3]);
    assertEquals(BigDecimal.valueOf(9999.8877D), row2[4]);
    assertEquals(BigDecimal.valueOf(777.66D), row2[5]);
    assertEquals(6666, row2[6]);
    assertEquals(5555L, row2[7]);
    assertEquals("", row2[8]); // no display text
    assertEquals("", row2[9]); // no display text
    assertEquals("Bar", row2[10]);
    assertEquals("World", row2[11]);
  }

  private static P_Table createTable(int status) {
    P_Table table = new P_Table();

    Date date = new Date(DATE_IN_MILLIS);
    Date date2 = new Date(DATE_IN_MILLIS_2);

    Object[] row1 = new Object[]{Boolean.TRUE, date, date, date, BigDecimal.valueOf(111.2233D), 111.223D, 3333, 4444L, OBJECT_ROW_1, 555L, "Foo", "<p>Hello</p>"};
    Object[] row2 = new Object[]{Boolean.FALSE, date2, date2, date2, BigDecimal.valueOf(9999.8877D), 777.66D, 6666, 5555L, OBJECT_ROW_2, 444L, "Bar", "<h1>World</h1>"};

    table.addRowsByMatrix(new Object[][]{row1, row2}, status);

    assertRowCount(2, 0, table);
    asssertStatusAndTable(table, status, table.getRow(0));
    asssertStatusAndTable(table, status, table.getRow(1));

    return table;
  }

  private static void assertRowCount(int expectedRowCount, int expectedDeletedRowCount, P_Table table) {
    assertEquals(expectedRowCount, table.getRowCount());
    assertEquals(expectedDeletedRowCount, table.getDeletedRowCount());
  }

  private static void asssertStatusAndTable(P_Table expectedTable, int expectedStatus, ITableRow row) {
    assertEquals(expectedStatus, row.getStatus());
    assertEquals(expectedTable, row.getTable());
  }

  private static class P_Table extends AbstractTable {

    @Order(10)
    public class BooleanColumn extends AbstractBooleanColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestBooleanColumn";
      }
    }

    @Order(20)
    public class DateColumn extends AbstractDateColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestDateColumn";
      }

      @Override
      protected String getConfiguredFormat() {
        return "dd.MM.yyyy";
      }
    }

    @Order(30)
    public class DateTimeColumn extends AbstractDateTimeColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestDateTimeColumn";
      }

      @Override
      protected String getConfiguredFormat() {
        return "dd.MM.yy hh:mm:ss";
      }
    }

    @Order(40)
    public class TimeColumn extends AbstractTimeColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestTimeColumn";
      }

      @Override
      protected String getConfiguredFormat() {
        return "hh:mm:ss";
      }
    }

    @Order(50)
    public class BigDecimalColumn extends AbstractBigDecimalColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestBigDecimalColumn";
      }

      @Override
      protected void initConfig() {
        super.initConfig();
        DecimalFormat df = getFormat();
        df.applyPattern("#0.0000");
        setFormat(df);
      }
    }

    @Order(60)
    public class BigDecimalColumn2 extends AbstractBigDecimalColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestBigDecimalColumn2";
      }

      @Override
      protected void initConfig() {
        super.initConfig();
        DecimalFormat df = getFormat();
        df.applyPattern("#0.000");
        setFormat(df);
      }
    }

    @Order(70)
    public class IntegerColumn extends AbstractIntegerColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestIntegerColumn";
      }

      @Override
      protected void initConfig() {
        super.initConfig();
        DecimalFormat df = getFormat();
        df.applyPattern("#,##0.0");
        setFormat(df);
      }
    }

    @Order(80)
    public class LongColumn extends AbstractLongColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestLongColumn";
      }

      @Override
      protected void initConfig() {
        super.initConfig();
        DecimalFormat df = getFormat();
        df.applyPattern("#,##0");
        setFormat(df);
      }
    }

    @Order(90)
    public class ObjectColumn extends AbstractObjectColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestObjectColumn";
      }
    }

    @Order(100)
    public class SmartColumn extends AbstractSmartColumn2<Long> {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestSmartColumn";
      }
    }

    @Order(110)
    public class StringColumn extends AbstractStringColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestStringColumn";
      }
    }

    @Order(120)
    public class StringHtmlColumn extends AbstractStringColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "TestStringHtmlColumn";
      }

      @Override
      protected boolean getConfiguredHtmlEnabled() {
        return true;
      }
    }
  }
}
