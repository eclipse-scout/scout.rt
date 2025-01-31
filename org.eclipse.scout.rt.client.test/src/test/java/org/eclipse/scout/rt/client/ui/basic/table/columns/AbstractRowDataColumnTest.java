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

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that updating other columns is possible by adding a change listener.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractRowDataColumnTest {

  @Test
  public void testTablesObservedBeforeInsertionEvent() {
    TestTable table = new TestTable();
    table.init();
    ITableRow row = table.createRow();
    table.getRowDataColumn().setValue(row, "newValue");
    row = table.addRow(row);

    assertEquals("newValue", table.getRowDataColumn().getValue(0));
    assertNull(table.getRowDataColumn().getDisplayText(table.getRow(0)));

    assertEquals("newValue", table.getStringTestColumn().getValue(0));
    assertEquals("newValue", table.getStringTestColumn().getDisplayText(table.getRow(0)));
  }

  @Test
  public void testSuccessfulImport() {
    TestTable table = new TestTable();
    table.init();

    TestTableBean tableBean = new TestTableBean();
    TestTableBean.TableBeanRowData row = tableBean.addRow();
    row.setRowData("newValue");

    table.importFromTableBeanData(tableBean);
    assertEquals("newValue", table.getRowDataColumn().getValue(0));
    assertNull(table.getRowDataColumn().getDisplayText(table.getRow(0)));

    assertEquals("newValue", table.getStringTestColumn().getValue(0));
    assertEquals("newValue", table.getStringTestColumn().getDisplayText(table.getRow(0)));
    assertEquals(ITableRow.STATUS_NON_CHANGED, table.getRow(0).getStatus());
  }

  @Test
  public void testSetValueTwice() {
    TestTable table = new TestTable();
    table.init();

    ITableRow row = table.createRow();
    row = table.addRow(row);

    // set value initial
    table.getRowDataColumn().setValue(row, "foo");
    assertEquals("foo", table.getRowDataColumn().getValue(row));
    assertEquals("foo", table.getStringTestColumn().getValue(row));

    // reset value
    table.getStringTestColumn().setValue(row, null);
    // set same value again
    table.getRowDataColumn().setValue(row, "foo");
    assertEquals("foo", table.getRowDataColumn().getValue(row));
    assertEquals("foo", table.getStringTestColumn().getValue(row));
  }

  public class TestTable extends AbstractTable {

    public StringTestColumn getStringTestColumn() {
      return getColumnSet().getColumnByClass(StringTestColumn.class);
    }

    public RowDataColumn getRowDataColumn() {
      return getColumnSet().getColumnByClass(RowDataColumn.class);
    }

    @Order(10)
    public class StringTestColumn extends AbstractStringColumn {
    }

    @Order(20)
    public class RowDataColumn extends AbstractRowDataColumn<String> {

      @Override
      protected void updateTableColumns(ITableRow r, String newValue) {
        getStringTestColumn().setValue(r, newValue);
      }
    }
  }
}

class TestTableBean extends AbstractTableFieldBeanData {
  private static final long serialVersionUID = 1L;

  public TestTableBean() {
  }

  @Override
  public TableBeanRowData addRow() {
    return (TableBeanRowData) super.addRow();
  }

  @Override
  public TableBeanRowData addRow(int rowState) {
    return (TableBeanRowData) super.addRow(rowState);
  }

  @Override
  public TableBeanRowData createRow() {
    return new TableBeanRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return TableBeanRowData.class;
  }

  @Override
  public TableBeanRowData[] getRows() {
    return (TableBeanRowData[]) super.getRows();
  }

  @Override
  public TableBeanRowData rowAt(int index) {
    return (TableBeanRowData) super.rowAt(index);
  }

  public void setRows(TableBeanRowData[] rows) {
    super.setRows(rows);
  }

  public static class TableBeanRowData extends AbstractTableRowData {
    private static final long serialVersionUID = 1L;
    private String m_stringTest;
    private String m_rowData;

    public TableBeanRowData() {
    }

    public String getStringTest() {
      return m_stringTest;
    }

    public void setStringTest(String stringTest) {
      m_stringTest = stringTest;
    }

    public String getRowData() {
      return m_rowData;
    }

    public void setRowData(String rowData) {
      m_rowData = rowData;
    }
  }
}
