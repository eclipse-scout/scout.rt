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

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
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
    table.initTable();
    ITableRow row = table.createRow();
    table.getRowDataColumn().setValue(row, "newValue");
    row = table.addRow(row);

    assertEquals("newValue", table.getRowDataColumn().getValue(0));
    assertEquals(null, table.getRowDataColumn().getDisplayText(table.getRow(0)));
    assertEquals("newValue", table.getStringTestColumn().getValue(0));
    assertEquals("newValue", table.getStringTestColumn().getDisplayText(table.getRow(0)));
  }

  @Test
  public void testSuccessfulImport() {
    TestTable table = new TestTable();
    table.initTable();

    TestTableBean tableBean = new TestTableBean();
    TestTableBean.TableBeanRowData row = tableBean.addRow();
    row.setRowData("newValue");

    table.importFromTableBeanData(tableBean);
    assertEquals("newValue", table.getRowDataColumn().getValue(0));
    assertEquals(null, table.getRowDataColumn().getDisplayText(table.getRow(0)));
    assertEquals("newValue", table.getStringTestColumn().getValue(0));
    assertEquals("newValue", table.getStringTestColumn().getDisplayText(table.getRow(0)));
    assertEquals(ITableRow.STATUS_NON_CHANGED, table.getRow(0).getStatus());
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
    public class RowDataColumn extends AbstractTestRowDataColumn<String> {

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

abstract class AbstractTestRowDataColumn<T> extends AbstractColumn<T> {

  private TableListener m_updateTableRowListener;

  @Override
  public void initColumn() {
    super.initColumn();
    if (getTable() != null) {
      if (m_updateTableRowListener != null) {
        getTable().removeTableListener(m_updateTableRowListener);
      }
      m_updateTableRowListener = new P_UpdateTableRowListener(this);
      getTable().addTableListener(m_updateTableRowListener);
    }
  }

  @Override
  public void disposeColumn() {
    super.disposeColumn();
    if (getTable() != null) {
      if (m_updateTableRowListener != null) {
        getTable().removeTableListener(m_updateTableRowListener);
      }
    }
    m_updateTableRowListener = null;
  }

  /**
   * Updates all other columns based on this column's value.
   */
  protected abstract void updateTableColumns(ITableRow r, T newValue);

  protected class P_UpdateTableRowListener extends TableAdapter {

    private final IColumn<?> m_column;

    public P_UpdateTableRowListener(IColumn<?> column) {
      m_column = column;
    }

    @Override
    public void tableChanged(TableEvent e) {
      switch (e.getType()) {
        case TableEvent.TYPE_ROWS_INSERTED:
        case TableEvent.TYPE_ROWS_UPDATED: {
          try {
            getTable().setTableChanging(true);
            for (ITableRow row : e.getRows()) {
              // Trigger "updateTableColumn" when a row was inserted, or the value of this column is changed.
              // Do _not_ trigger the method when other columns change their values (this might lead to loops).
              if (e.getType() == TableEvent.TYPE_ROWS_INSERTED || e.getUpdatedColumns(row).contains(m_column)) {
                final int origStatus = row.getStatus();
                updateTableColumns(row, getValue(row));
                row.setStatus(origStatus);
              }
            }
          }
          catch (ProcessingException ex) {
            BEANS.get(ExceptionHandler.class).handle(ex);
          }
          finally {
            getTable().setTableChanging(false);
          }
          break;
        }
      }
    }
  }

}
