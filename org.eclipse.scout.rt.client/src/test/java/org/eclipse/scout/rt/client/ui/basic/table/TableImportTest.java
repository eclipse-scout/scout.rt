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
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.junit.Test;

/**
 * Tests the import of beans into a table
 */
public class TableImportTest {
  private static final String TEST_VALUE = "test";

  @Test
  public void testSuccessfulImport() throws ProcessingException {
    P_Table table = new P_Table();
    P_TableBean tableBean = new P_TableBean();
    P_TableBean.TableBeanRowData row = tableBean.addRow();
    row.setDefault(TEST_VALUE);
    table.importFromTableBeanData(tableBean);
    String value = table.getDefaultColumn().getValue(0);
    assertEquals(TEST_VALUE, value);
    //TODO parse should be called only once
    //assertEquals(1, table.getDefaultColumn().getParseCount());
  }

  public static class P_Table extends AbstractTable {

    public DefaultColumn getDefaultColumn() {
      return getColumnSet().getColumnByClass(DefaultColumn.class);
    }

    @Order(10.0)
    public class DefaultColumn extends AbstractStringColumn {

      private int parseCount = 0;

      public int getParseCount() {
        return parseCount;
      }

      @Override
      public String execParseValue(ITableRow row, Object rawValue) throws ProcessingException {
        parseCount++;
        return super.execParseValue(row, rawValue);
      }

    }
  }

  private static class P_TableBean extends AbstractTableFieldBeanData {
    private static final long serialVersionUID = 1L;

    public P_TableBean() {
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

    @SuppressWarnings("unused")
    public void setRows(TableBeanRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableBeanRowData extends AbstractTableRowData {
      private static final long serialVersionUID = 1L;
      private String m_default;

      public TableBeanRowData() {
      }

      @SuppressWarnings("unused")
      public String getDefault() {
        return m_default;
      }

      public void setDefault(String default1) {
        m_default = default1;
      }
    }
  }

}
