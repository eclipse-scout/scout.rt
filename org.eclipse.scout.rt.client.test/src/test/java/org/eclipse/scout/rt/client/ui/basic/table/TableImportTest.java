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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.junit.Test;

/**
 * Tests the import of beans into a table
 */
public class TableImportTest {
  private static final String TEST_VALUE = "test";
  private P_Table m_table = new P_Table();

  @Test
  public void testSuccessfulImport() {
    P_TableBean tableBean = new P_TableBean();
    P_TableBean.TableBeanRowData row = tableBean.addRow();
    row.setDefault(TEST_VALUE);
    m_table.importFromTableBeanData(tableBean);
    String value = m_table.getDefaultColumn().getValue(0);
    String text = m_table.getDefaultColumn().getValue(0);
    assertEquals(TEST_VALUE, value);
    assertEquals(TEST_VALUE, text);
    assertEquals(1, m_table.getDefaultColumn().getParseCount());
    assertEquals(1, m_table.getDefaultColumn().getValidateCount());
    assertEquals(1, m_table.getDefaultColumn().getDecorateCount());
    assertEquals(ITableRow.STATUS_NON_CHANGED, m_table.getRow(0).getStatus());
  }

  @Test
  public void testInvalidValue() {
    P_TableBean tableBean = new P_TableBean();
    P_TableBean.TableBeanRowData row = tableBean.addRow();
    row.setDefault("invalid");
    m_table.importFromTableBeanData(tableBean);
    assertEquals("invalid", m_table.getCell(0, 0).getText());
    assertFalse(m_table.getCell(0, 0).isContentValid());
  }

  /**
   * Tests that properties are unchanged after importing data twice.
   */
  @Test
  public void testSuccessfulImportProps() {
    P_TableBean tableBean = new P_TableBean();
    P_TableBean.TableBeanRowData row = tableBean.addRow();
    row.setDefault(TEST_VALUE);
    m_table.getDefaultColumn().setBackgroundColor("black");
    m_table.importFromTableBeanData(tableBean);
    m_table.importFromTableBeanData(tableBean);
    assertEquals("black", m_table.getCell(0, 0).getBackgroundColor());
    assertTrue(m_table.getCell(0, 0).isHtmlEnabled());
  }

  public static class P_Table extends AbstractTable {

    public DefaultColumn getDefaultColumn() {
      return getColumnSet().getColumnByClass(DefaultColumn.class);
    }

    @Order(10)
    public class DefaultColumn extends AbstractStringColumn {

      @Override
      protected boolean getConfiguredHtmlEnabled() {
        return true;
      }

      private int m_parseCount = 0;
      private int m_validateCount = 0;
      private int m_decorateCount = 0;

      public int getDecorateCount() {
        return m_decorateCount;
      }

      public int getValidateCount() {
        return m_validateCount;
      }

      public int getParseCount() {
        return m_parseCount;
      }

      @Override
      public String execParseValue(ITableRow row, Object rawValue) {
        m_parseCount++;
        return super.execParseValue(row, rawValue);
      }

      @Override
      protected String execValidateValue(ITableRow row, String rawValue) {
        m_validateCount++;
        if (rawValue == "invalid") {
          throw new VetoException("invalid value");
        }
        return super.execValidateValue(row, rawValue);
      }

      @Override
      protected void execDecorateCell(Cell cell, ITableRow row) {
        m_decorateCount++;
        super.execDecorateCell(cell, row);
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
