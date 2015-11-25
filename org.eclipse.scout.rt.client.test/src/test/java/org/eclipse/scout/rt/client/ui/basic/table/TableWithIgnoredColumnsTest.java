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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.commons.annotations.ColumnData;
import org.eclipse.scout.commons.annotations.ColumnData.SdkColumnCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("unused")
@RunWith(PlatformTestRunner.class)
public class TableWithIgnoredColumnsTest {

  private static final int ROW_COUNT = 3;

  @Test
  public void testTableExportTableBeanData() throws Exception {
    doTestExportTableBeanDataOriginalDefaultIgnore(new P_Table(), new P_TableBean());
  }

  @Test
  public void testTableImportTableBeanData() throws Exception {
    doTestImportTableBeanDataOriginalDefaultIgnore(new P_Table(), new P_TableBean());
  }

  /*
   * Replace with DEFAULT
   */
  @Test
  public void testTableDefaultExportTableBeanData() throws Exception {
    doTestExportTableBeanDataOriginalDefaultIgnore(new P_TableDefault(), new P_TableDefaultBean());
  }

  @Test
  public void testTableDefaultImportTableBeanData() throws Exception {
    doTestImportTableBeanDataOriginalDefaultIgnore(new P_TableDefault(), new P_TableDefaultBean());
  }

  /*
   * Replace with IGNORE
   */
  @Test
  public void testTableIgnoreExportTableBeanData() throws Exception {
    doTestExportTableBeanDataOriginalDefaultIgnore(new P_TableIgnore(), new P_TableIgnoreBean());
  }

  @Test
  public void testTableIgnoreImportTableBeanData() throws Exception {
    doTestImportTableBeanDataOriginalDefaultIgnore(new P_TableIgnore(), new P_TableIgnoreBean());
  }

  /*
   * Replace with CREATE
   */
  @Test
  public void testTableCreateExportTableBeanData() throws Exception {
    P_TableCreate table = new P_TableCreate();
    for (int i = 1; i <= ROW_COUNT; i++) {
      table.addRow(table.createRow(new Object[]{"Default " + i, "Create " + i, "Ignore " + i}));
    }
    P_TableCreateBean tableBean = new P_TableCreateBean();
    table.exportToTableBeanData(tableBean);

    assertEquals(ROW_COUNT, tableBean.getRowCount());
    for (int i = 0; i < ROW_COUNT; i++) {
      assertEquals("Default " + (i + 1), tableBean.rowAt(i).getDefault());
      assertEquals("Create " + (i + 1), tableBean.rowAt(i).getCreate());
      assertEquals("Ignore " + (i + 1), tableBean.rowAt(i).getIgnore());
    }
  }

  @Test
  public void testTableCreateImportTableBeanData() throws Exception {
    P_TableCreateBean tableBean = new P_TableCreateBean();
    for (int i = 1; i <= ROW_COUNT; i++) {
      P_TableCreateBean.TableCreateBeanRowData row = tableBean.addRow();
      row.setDefault("Default " + i);
      row.setCreate("Create " + i);
      row.setIgnore("Ignore " + i);
    }
    P_TableCreate table = new P_TableCreate();
    table.importFromTableBeanData(tableBean);

    assertEquals(ROW_COUNT, table.getRowCount());
    for (int i = 0; i < ROW_COUNT; i++) {
      assertEquals("Default " + (i + 1), table.getDefaultColumn().getValue(i));
      assertEquals("Create " + (i + 1), table.getCreateColumn().getValue(i));
      assertEquals("Ignore " + (i + 1), table.getIgnoreColumn().getValue(i));
    }
  }

  private void doTestExportTableBeanDataOriginalDefaultIgnore(P_Table table, P_TableBean tableBean) throws Exception {
    for (int i = 1; i <= ROW_COUNT; i++) {
      table.addRow(table.createRow(new Object[]{"Default " + i, "Create " + i, "Ignore " + i}));
    }
    table.exportToTableBeanData(tableBean);

    assertEquals(ROW_COUNT, tableBean.getRowCount());
    for (int i = 0; i < ROW_COUNT; i++) {
      assertEquals("Default " + (i + 1), tableBean.rowAt(i).getDefault());
      assertEquals("Create " + (i + 1), tableBean.rowAt(i).getCreate());
      assertNull(tableBean.rowAt(i).getCustomColumnValue(table.getIgnoreColumn().getColumnId()));
    }
  }

  private void doTestImportTableBeanDataOriginalDefaultIgnore(P_Table table, P_TableBean tableBean) throws Exception {
    for (int i = 1; i <= ROW_COUNT; i++) {
      P_TableBean.TableBeanRowData row = tableBean.addRow();
      row.setDefault("Default " + i);
      row.setCreate("Create " + i);
      row.setCustomColumnValue("Ignore", "Ignore " + i);
    }
    table.importFromTableBeanData(tableBean);

    assertEquals(ROW_COUNT, table.getRowCount());
    for (int i = 0; i < ROW_COUNT; i++) {
      assertEquals("Default " + (i + 1), table.getDefaultColumn().getValue(i));
      assertEquals("Create " + (i + 1), table.getCreateColumn().getValue(i));
      assertNull(table.getIgnoreColumn().getValue(i));
    }
  }

  private static class P_Table extends AbstractTable {

    public DefaultColumn getDefaultColumn() {
      return getColumnSet().getColumnByClass(DefaultColumn.class);
    }

    public IgnoreColumn getIgnoreColumn() {
      return getColumnSet().getColumnByClass(IgnoreColumn.class);
    }

    public CreateColumn getCreateColumn() {
      return getColumnSet().getColumnByClass(CreateColumn.class);
    }

    @Order(10)
    public class DefaultColumn extends AbstractStringColumn {
    }

    @Order(20)
    @ColumnData(SdkColumnCommand.CREATE)
    public class CreateColumn extends AbstractStringColumn {
    }

    @Order(30)
    @ColumnData(SdkColumnCommand.IGNORE)
    public class IgnoreColumn extends AbstractStringColumn {
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

    public void setRows(TableBeanRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableBeanRowData extends AbstractTableRowData {

      private static final long serialVersionUID = 1L;
      public static final String DEFAULT = "default";
      public static final String CREATE = "create";
      private String m_default;
      private String m_create;

      public TableBeanRowData() {
      }

      public String getDefault() {
        return m_default;
      }

      public void setDefault(String default1) {
        m_default = default1;
      }

      public String getCreate() {
        return m_create;
      }

      public void setCreate(String create) {
        m_create = create;
      }
    }
  }

  /*
   * Replace with DEFAULT
   */
  private static class P_TableDefault extends P_Table {

    @Replace
    @Order(40)
    public class DefaultDefaultColumn extends AbstractStringColumn {
    }

    @Replace
    public class CreateDefaultColumn extends CreateColumn {
    }

    @Replace
    public class IgnoreDefaultColumn extends IgnoreColumn {
    }
  }

  private static class P_TableDefaultBean extends P_TableBean {
    private static final long serialVersionUID = 1L;

    public P_TableDefaultBean() {
    }

    @Override
    public TableDefaultBeanRowData addRow() {
      return (TableDefaultBeanRowData) super.addRow();
    }

    @Override
    public TableDefaultBeanRowData addRow(int rowState) {
      return (TableDefaultBeanRowData) super.addRow(rowState);
    }

    @Override
    public TableDefaultBeanRowData createRow() {
      return new TableDefaultBeanRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableDefaultBeanRowData.class;
    }

    @Override
    public TableDefaultBeanRowData[] getRows() {
      return (TableDefaultBeanRowData[]) super.getRows();
    }

    @Override
    public TableDefaultBeanRowData rowAt(int index) {
      return (TableDefaultBeanRowData) super.rowAt(index);
    }

    public void setRows(TableDefaultBeanRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableDefaultBeanRowData extends TableBeanRowData {

      private static final long serialVersionUID = 1L;

      public TableDefaultBeanRowData() {
      }
    }
  }

  /*
   * Replace with CREATE
   */
  private static class P_TableCreate extends P_Table {

    @Replace
    @ColumnData(SdkColumnCommand.CREATE)
    public class DefaultCreateColumn extends AbstractStringColumn {
    }

    @Replace
    @ColumnData(SdkColumnCommand.CREATE)
    public class CreateCreateColumn extends CreateColumn {
    }

    @Replace
    @ColumnData(SdkColumnCommand.CREATE)
    public class IgnoreCreateColumn extends IgnoreColumn {
    }
  }

  private static class P_TableCreateBean extends P_TableBean {
    private static final long serialVersionUID = 1L;

    public P_TableCreateBean() {
    }

    @Override
    public TableCreateBeanRowData addRow() {
      return (TableCreateBeanRowData) super.addRow();
    }

    @Override
    public TableCreateBeanRowData addRow(int rowState) {
      return (TableCreateBeanRowData) super.addRow(rowState);
    }

    @Override
    public TableCreateBeanRowData createRow() {
      return new TableCreateBeanRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableCreateBeanRowData.class;
    }

    @Override
    public TableCreateBeanRowData[] getRows() {
      return (TableCreateBeanRowData[]) super.getRows();
    }

    @Override
    public TableCreateBeanRowData rowAt(int index) {
      return (TableCreateBeanRowData) super.rowAt(index);
    }

    public void setRows(TableCreateBeanRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableCreateBeanRowData extends TableBeanRowData {

      private static final long serialVersionUID = 1L;
      public static final String IGNORE = "ignore";
      private String m_ignore;

      public TableCreateBeanRowData() {
      }

      public String getIgnore() {
        return m_ignore;
      }

      public void setIgnore(String ignore) {
        m_ignore = ignore;
      }
    }
  }

  /*
   * Replace with IGNORE
   */
  private static class P_TableIgnore extends P_Table {

    @Replace
    @ColumnData(SdkColumnCommand.IGNORE)
    public class DefaultIgnoreColumn extends AbstractStringColumn {
    }

    @Replace
    @ColumnData(SdkColumnCommand.IGNORE)
    public class CreateIgnoreColumn extends CreateColumn {
    }

    @Replace
    @ColumnData(SdkColumnCommand.IGNORE)
    public class IgnoreIgnoreColumn extends IgnoreColumn {
    }
  }

  private static class P_TableIgnoreBean extends P_TableBean {
    private static final long serialVersionUID = 1L;

    public P_TableIgnoreBean() {
    }

    @Override
    public TableIgnoreBeanRowData addRow() {
      return (TableIgnoreBeanRowData) super.addRow();
    }

    @Override
    public TableIgnoreBeanRowData addRow(int rowState) {
      return (TableIgnoreBeanRowData) super.addRow(rowState);
    }

    @Override
    public TableIgnoreBeanRowData createRow() {
      return new TableIgnoreBeanRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableIgnoreBeanRowData.class;
    }

    @Override
    public TableIgnoreBeanRowData[] getRows() {
      return (TableIgnoreBeanRowData[]) super.getRows();
    }

    @Override
    public TableIgnoreBeanRowData rowAt(int index) {
      return (TableIgnoreBeanRowData) super.rowAt(index);
    }

    public void setRows(TableIgnoreBeanRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableIgnoreBeanRowData extends TableBeanRowData {

      private static final long serialVersionUID = 1L;

      public TableIgnoreBeanRowData() {
      }
    }
  }
}
