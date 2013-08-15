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
package org.eclipse.scout.rt.client.ui.form.fields.tablefield;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.ITableHolder;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.junit.Test;

/**
 * JUnit tests for {@link AbstractTableField}
 */
public class TableFieldTest {

  /**
   * Test method for {@link AbstractTableField#importFormFieldData(AbstractFormFieldData, boolean)} With and without
   * primary keys.
   * Bug 414674
   */

  @Test
  public void testCreateTableField() throws Exception {
    P_TableField tableField1 = createTableField(false);
    assertEquals("prerequisite: tableField1.isPrimaryKey()", false, tableField1.getTable().getKeyColumn().isPrimaryKey());
    assertEquals("prerequisite: tableField1.isDisplayable()", true, tableField1.getTable().getKeyColumn().isDisplayable());

    P_TableField tableField2 = createTableField(true);
    assertEquals("prerequisite: tableField2.isPrimaryKey()", true, tableField2.getTable().getKeyColumn().isPrimaryKey());
    assertEquals("prerequisite: tableField2.isDisplayable()", false, tableField2.getTable().getKeyColumn().isDisplayable());
  }

  private P_TableField createTableField(boolean hasPrimaryKey) {
    return new P_TableField(hasPrimaryKey);
  }

  @Test
  public void testImportFormFieldData() throws Exception {
    P_TableField tableField1 = createTableField(false);
    runImportFormFieldData(tableField1);

    P_TableField tableField2 = createTableField(true);
    runImportFormFieldData(tableField2);
  }

  private void runImportFormFieldData(P_TableField tableField) throws ProcessingException {
    P_Table tableData1 = createTableData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertRowCount(3, tableField);
    assertHiddenColumnValues(false, true, false, tableField);

    P_Table tableData2 = createTableData(true, false, false, ITableHolder.STATUS_UPDATED);
    tableField.importFormFieldData(tableData2, false);

    assertRowCount(3, tableField);
    assertHiddenColumnValues(true, false, false, tableField);
  }

  private void assertKeyColumnValues(Integer expectedValueRow1, Integer expectedValueRow2, Integer expectedValueRow3, P_TableField tableField) {
    assertEquals(expectedValueRow1, tableField.getTable().getKeyColumn().getValue(0));
    assertEquals(expectedValueRow2, tableField.getTable().getKeyColumn().getValue(1));
    assertEquals(expectedValueRow3, tableField.getTable().getKeyColumn().getValue(2));
  }

  private void assertHiddenColumnValues(boolean expectedValueRow1, boolean expectedValueRow2, boolean expectedValueRow3, P_TableField tableField) {
    assertEquals(expectedValueRow1, tableField.getTable().getHiddenColumn().getValue(0));
    assertEquals(expectedValueRow2, tableField.getTable().getHiddenColumn().getValue(1));
    assertEquals(expectedValueRow3, tableField.getTable().getHiddenColumn().getValue(2));
  }

  private void assertStringColumnValues(String expectedValueRow1, String expectedValueRow2, String expectedValueRow3, P_TableField tableField) {
    assertEquals(expectedValueRow1, tableField.getTable().getStringColumn().getValue(0));
    assertEquals(expectedValueRow2, tableField.getTable().getStringColumn().getValue(1));
    assertEquals(expectedValueRow3, tableField.getTable().getStringColumn().getValue(2));
  }

  private void assertRowCount(int expectedRowCount, P_TableField tableField) {
    assertEquals(expectedRowCount, tableField.getTable().getRowCount());
  }

  @Test
  public void testImportFormFieldDataWithTableValueSet() throws Exception {
    P_TableField tableField1 = createTableField(false);
    runImportFormFieldDataWithTableValueSet(tableField1);

    P_TableField tableField2 = createTableField(true);
    runImportFormFieldDataWithTableValueSet(tableField2);
  }

  private void runImportFormFieldDataWithTableValueSet(P_TableField tableField) throws ProcessingException {
    P_Table tableData1 = createTableData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertRowCount(3, tableField);
    assertHiddenColumnValues(false, true, false, tableField);

    P_Table tableData2 = createTableData(true, false, false, ITableHolder.STATUS_UPDATED);
    tableData2.setValueSet(false);
    tableField.importFormFieldData(tableData2, false);

    assertRowCount(3, tableField);
    // we should still have the old values
    assertHiddenColumnValues(false, true, false, tableField);
  }

  @Test
  public void testImportFormFieldDataWithNewRow() throws Exception {
    P_TableField tableField1 = createTableField(false);
    runImportFormFieldDataWithNewRow(tableField1);

    P_TableField tableField2 = createTableField(true);
    runImportFormFieldDataWithNewRow(tableField2);
  }

  private void runImportFormFieldDataWithNewRow(P_TableField tableField) throws ProcessingException {
    P_Table tableData1 = createTableData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertHiddenColumnValues(false, true, false, tableField);
    assertRowCount(3, tableField);

    addNewTableRow(tableField);

    assertRowCount(4, tableField);
    assertEquals(Integer.valueOf(4), tableField.getTable().getKeyColumn().getValue(3));
    assertEquals("Sit", tableField.getTable().getStringColumn().getValue(3));
    assertEquals(true, tableField.getTable().getHiddenColumn().getValue(3));

    P_Table tableData2 = createTableData(true, false, false, ITableHolder.STATUS_UPDATED);
    tableField.importFormFieldData(tableData2, false);

    assertRowCount(3, tableField);
    assertKeyColumnValues(1, 2, 3, tableField);
    assertStringColumnValues("Lorem", "Ipsum", "Dolor", tableField);
    assertHiddenColumnValues(true, false, false, tableField);
  }

  private void addNewTableRow(P_TableField tableField) throws ProcessingException {
    ITableRow newRow = tableField.getTable().createRow();

    tableField.getTable().getKeyColumn().setValue(newRow, 4);
    tableField.getTable().getHiddenColumn().setValue(newRow, true);
    tableField.getTable().getStringColumn().setValue(newRow, "Sit");
    tableField.getTable().addRow(newRow);
  }

  @Test
  public void testImportFormFieldDataWithDeletedRow() throws Exception {
    P_TableField tableField1 = createTableField(false);
    runImportFormFieldDataWithDeletedRow(tableField1);

    P_TableField tableField2 = createTableField(true);
    runImportFormFieldDataWithDeletedRow(tableField2);
  }

  private void runImportFormFieldDataWithDeletedRow(P_TableField tableField) throws ProcessingException {
    P_Table tableData1 = createTableData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertRowCount(3, tableField);
    assertHiddenColumnValues(false, true, false, tableField);

    deleteFirstTwoTableRows(tableField);

    assertRowCount(1, tableField);

    P_Table tableData2 = createTableData(true, false, true, ITableHolder.STATUS_UPDATED);
    tableField.importFormFieldData(tableData2, false);

    assertRowCount(3, tableField);
    assertKeyColumnValues(1, 2, 3, tableField);
    assertStringColumnValues("Lorem", "Ipsum", "Dolor", tableField);
    assertHiddenColumnValues(true, false, true, tableField);
  }

  private void deleteFirstTwoTableRows(P_TableField tableField) throws ProcessingException {
    tableField.getTable().deleteRows(new int[]{0, 1});
  }

  @Test
  public void testImportFormFieldDataWithUpdatedRow() throws Exception {
    P_TableField tableField1 = createTableField(false);
    runImportFormFieldDataWithUpdatedRow(tableField1);

    P_TableField tableField2 = createTableField(true);
    runImportFormFieldDataWithUpdatedRow(tableField2);
  }

  private void runImportFormFieldDataWithUpdatedRow(P_TableField tableField) throws ProcessingException {
    P_Table tableData1 = createTableData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertRowCount(3, tableField);
    assertHiddenColumnValues(false, true, false, tableField);

    updateThirdRow(tableField);
    assertRowCount(3, tableField);
    assertKeyColumnValues(1, 2, 3, tableField);
    assertStringColumnValues("Lorem", "Ipsum", "Amet", tableField);
    assertHiddenColumnValues(false, true, true, tableField);

    P_Table tableData2 = createTableData(true, false, false, ITableHolder.STATUS_UPDATED);
    tableField.importFormFieldData(tableData2, false);

    assertRowCount(3, tableField);
    assertKeyColumnValues(1, 2, 3, tableField);
    assertStringColumnValues("Lorem", "Ipsum", "Dolor", tableField);
    assertHiddenColumnValues(true, false, false, tableField);
  }

  private void updateThirdRow(P_TableField tableField) throws ProcessingException {
    ITableRow updatedRow = tableField.getTable().getRow(2);
    updatedRow.getCellForUpdate(1).setValue("Amet");
    updatedRow.getCellForUpdate(2).setValue(true);
    tableField.getTable().updateRow(updatedRow);
  }

  private P_Table createTableData(boolean r1_value, boolean r2_value, boolean r3_value, int state) {
    int r;
    P_Table tableData = new P_Table();
    r = tableData.addRow();
    tableData.setRowState(r, state);
    tableData.setKey(r, 1);
    tableData.setString(r, "Lorem");
    tableData.setHidden(r, r1_value);

    r = tableData.addRow();
    tableData.setRowState(r, state);
    tableData.setKey(r, 2);
    tableData.setString(r, "Ipsum");
    tableData.setHidden(r, r2_value);

    r = tableData.addRow();
    tableData.setKey(r, 3);
    tableData.setRowState(r, state);
    tableData.setString(r, "Dolor");
    tableData.setHidden(r, r3_value);

    return tableData;
  }

  private static class P_TableField extends AbstractTableField<P_TableField.Table> {

    private final boolean m_configuredDiplayable;
    private final boolean m_configuredPrimaryKey;

    public P_TableField(boolean withPrimaryKey) {
      super(false);
      if (withPrimaryKey) {
        m_configuredDiplayable = false;
        m_configuredPrimaryKey = true;
      }
      else {
        m_configuredDiplayable = true;
        m_configuredPrimaryKey = false;
      }
      callInitializer();
    }

    @Order(10.0)
    public class Table extends AbstractTable {

      public KeyColumn getKeyColumn() {
        return getColumnSet().getColumnByClass(KeyColumn.class);
      }

      public HiddenColumn getHiddenColumn() {
        return getColumnSet().getColumnByClass(HiddenColumn.class);
      }

      public StringColumn getStringColumn() {
        return getColumnSet().getColumnByClass(StringColumn.class);
      }

      @Order(10.0)
      public class KeyColumn extends AbstractIntegerColumn {

        @Override
        protected boolean getConfiguredDisplayable() {
          return m_configuredDiplayable;
        }

        @Override
        protected boolean getConfiguredPrimaryKey() {
          return m_configuredPrimaryKey;
        }
      }

      @Order(20.0)
      public class StringColumn extends AbstractStringColumn {
      }

      @Order(30.0)
      public class HiddenColumn extends AbstractBooleanColumn {
      }
    }
  }

  /**
   * Corresponding part of the formData:
   */
  private static class P_Table extends AbstractTableFieldData {
    private static final long serialVersionUID = 1L;

    public P_Table() {
    }

    public static final int KEY_COLUMN_ID = 0;
    public static final int STRING_COLUMN_ID = 1;
    public static final int HIDDEN_COLUMN_ID = 2;

    public void setKey(int row, Integer key) {
      setValueInternal(row, KEY_COLUMN_ID, key);
    }

    public Integer getKey(int row) {
      return (Integer) getValueInternal(row, KEY_COLUMN_ID);
    }

    public void setString(int row, String string) {
      setValueInternal(row, STRING_COLUMN_ID, string);
    }

    public String getString(int row) {
      return (String) getValueInternal(row, STRING_COLUMN_ID);
    }

    public void setHidden(int row, Boolean hidden) {
      setValueInternal(row, HIDDEN_COLUMN_ID, hidden);
    }

    public Boolean getHidden(int row) {
      return (Boolean) getValueInternal(row, HIDDEN_COLUMN_ID);
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public Object getValueAt(int row, int column) {
      switch (column) {
        case KEY_COLUMN_ID:
          return getKey(row);
        case STRING_COLUMN_ID:
          return getString(row);
        case HIDDEN_COLUMN_ID:
          return getHidden(row);
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(int row, int column, Object value) {
      switch (column) {
        case KEY_COLUMN_ID:
          setKey(row, (Integer) value);
          break;
        case STRING_COLUMN_ID:
          setString(row, (String) value);
          break;
        case HIDDEN_COLUMN_ID:
          setHidden(row, (Boolean) value);
          break;
      }
    }
  }

}
