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
package org.eclipse.scout.rt.client.ui.form.fields.tablefield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IValidateContentDescriptor;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.holders.ITableHolder;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit tests for {@link AbstractTableField}
 */
@SuppressWarnings({"unused"})
@RunWith(PlatformTestRunner.class)
public class TableFieldTest {
  private static final Object[] TEST_ROW = new Object[]{1, "Test", false};

  /**
   * Test method for {@link AbstractTableField#importFormFieldData(AbstractFormFieldData, boolean)} With and without
   * primary keys. Bug 414674
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

  /**
   * Tests that there is no error when adding a {@link Status#OK_STATUS} to a cell.
   */
  @Test
  public void testOkErrorStatus_NoError() {
    P_TableField tableField = createTableField(false);
    tableField.getTable().addRowByArray(TEST_ROW);
    Cell cell = (Cell) tableField.getTable().getCell(0, 1);
    cell.setEditable(true);
    cell.addErrorStatus(Status.OK_STATUS);
    IValidateContentDescriptor desc = tableField.validateContent();
    assertNull(desc);
  }

  /**
   * Tests that there is an error on the field, when a table field with errors is validated.
   */
  @Test
  public void testOkErrorStatus_ErrorText() {
    P_TableField tableField = createTableField(false);
    tableField.getTable().addRowByArray(TEST_ROW);
    Cell cell = (Cell) tableField.getTable().getCell(0, 1);
    cell.setEditable(true);
    cell.addErrorStatus("ErrorX");
    IValidateContentDescriptor desc = tableField.validateContent();
    assertNotNull(desc);
    assertTrue(desc.getDisplayText().contains(tableField.getTable().getStringColumn().getClass().getSimpleName()));
  }

  /**
   * Tests that there is an error on the field, when a mandatory table is not filled.
   */
  @Test
  public void testMandatory_ErrorText() {
    P_TableField tableField = createTableField(false);
    tableField.getTable().addRowByArray(new Object[]{1, null, false});
    Cell cell = (Cell) tableField.getTable().getCell(0, 1);
    cell.setEditable(true);
    cell.setMandatory(true);
    IValidateContentDescriptor desc = tableField.validateContent();
    assertNotNull(desc);
    assertNull(desc.getErrorStatus());
    assertTrue(desc.getDisplayText().contains(tableField.getTable().getStringColumn().getClass().getSimpleName()));
  }

  /**
   * Tests that an invisible displayable column is shown, if there is an error.
   */
  @Test
  public void testErrorColumn_Visible() {
    P_TableField tableField = createTableField(false);
    tableField.getTable().addRowByArray(TEST_ROW);
    Cell cell = (Cell) tableField.getTable().getCell(0, 1);
    cell.setEditable(true);
    tableField.getTable().getStringColumn().setVisible(false);
    cell.addErrorStatus("ErrorX");
    IValidateContentDescriptor desc = tableField.validateContent();
    assertTrue(tableField.getTable().getStringColumn().isVisible());
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

  private void runImportFormFieldData(P_TableField tableField) {
    P_TableBean tableData1 = createTableBeanData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertRowCount(3, tableField);
    assertHiddenColumnValues(false, true, false, tableField);

    P_TableBean tableData2 = createTableBeanData(true, false, false, ITableHolder.STATUS_UPDATED);
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

  private void assertRowStates(int rowState, P_TableField tableField) {
    assertEquals(rowState, tableField.getTable().getRow(0).getStatus());
    assertEquals(rowState, tableField.getTable().getRow(1).getStatus());
    assertEquals(rowState, tableField.getTable().getRow(2).getStatus());
  }

  @Test
  public void testImportFormFieldDataWithTableValueSet() throws Exception {
    P_TableField tableField1 = createTableField(false);
    runImportFormFieldDataWithTableValueSet(tableField1);

    P_TableField tableField2 = createTableField(true);
    runImportFormFieldDataWithTableValueSet(tableField2);
  }

  private void runImportFormFieldDataWithTableValueSet(P_TableField tableField) {
    P_TableBean tableData1 = createTableBeanData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertRowCount(3, tableField);
    assertHiddenColumnValues(false, true, false, tableField);

    P_TableBean tableData2 = createTableBeanData(true, false, false, ITableHolder.STATUS_UPDATED);
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

  private void runImportFormFieldDataWithNewRow(P_TableField tableField) {
    P_TableBean tableData1 = createTableBeanData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertHiddenColumnValues(false, true, false, tableField);
    assertRowCount(3, tableField);

    addNewTableRow(tableField);

    assertRowCount(4, tableField);
    assertEquals(Integer.valueOf(4), tableField.getTable().getKeyColumn().getValue(3));
    assertEquals("Sit", tableField.getTable().getStringColumn().getValue(3));
    assertEquals(true, tableField.getTable().getHiddenColumn().getValue(3));

    P_TableBean tableData2 = createTableBeanData(true, false, false, ITableHolder.STATUS_UPDATED);
    tableField.importFormFieldData(tableData2, false);

    assertRowCount(3, tableField);
    assertKeyColumnValues(1, 2, 3, tableField);
    assertStringColumnValues("Lorem", "Ipsum", "Dolor", tableField);
    assertHiddenColumnValues(true, false, false, tableField);
  }

  private void addNewTableRow(P_TableField tableField) {
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

  private void runImportFormFieldDataWithDeletedRow(P_TableField tableField) {
    P_TableBean tableData1 = createTableBeanData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertRowCount(3, tableField);
    assertHiddenColumnValues(false, true, false, tableField);

    deleteFirstTwoTableRows(tableField);

    assertRowCount(1, tableField);

    P_TableBean tableData2 = createTableBeanData(true, false, true, ITableHolder.STATUS_UPDATED);
    tableField.importFormFieldData(tableData2, false);

    assertRowCount(3, tableField);
    assertKeyColumnValues(1, 2, 3, tableField);
    assertStringColumnValues("Lorem", "Ipsum", "Dolor", tableField);
    assertHiddenColumnValues(true, false, true, tableField);
  }

  private void deleteFirstTwoTableRows(P_TableField tableField) {
    tableField.getTable().deleteRows(new int[]{0, 1});
  }

  @Test
  public void testValueChangedTriggersWhileImportingFormFieldData() {
    importFormFieldDataAndCheckExecContentChangedTriggerd(true);
    importFormFieldDataAndCheckExecContentChangedTriggerd(false);
  }

  private void importFormFieldDataAndCheckExecContentChangedTriggerd(boolean valueChangeTriggersEnabled) {
    P_TableField tableField = createTableField(false);
    P_TableBean tableData1 = createTableBeanData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, valueChangeTriggersEnabled);
    assertEquals(valueChangeTriggersEnabled, tableField.m_execContentChangedCalled);
  }

  @Test
  public void testDelegatingValueChangedTriggerEnabledProperty() {
    P_TableField tableField = createTableField(false);
    assertValueChangeTriggerEnabledFlag(tableField, true);

    // first call
    tableField.setValueChangeTriggerEnabled(false);
    assertValueChangeTriggerEnabledFlag(tableField, false);

    // second call
    tableField.setValueChangeTriggerEnabled(false);
    assertValueChangeTriggerEnabledFlag(tableField, false);

    // revert second call
    tableField.setValueChangeTriggerEnabled(true);
    assertValueChangeTriggerEnabledFlag(tableField, false);

    // revert first call
    tableField.setValueChangeTriggerEnabled(true);
    assertValueChangeTriggerEnabledFlag(tableField, true);
  }

  @Test
  public void testDelegatingValueChangedTriggerEnabledPropertySettingTableAfterInit() {
    P_TableField tableField = createTableField(false);
    tableField.setTable(null, true);
    assertTrue(tableField.isValueChangeTriggerEnabled());

    // first call
    tableField.setValueChangeTriggerEnabled(false);
    assertFalse(tableField.isValueChangeTriggerEnabled());

    // second call
    tableField.setValueChangeTriggerEnabled(false);
    assertFalse(tableField.isValueChangeTriggerEnabled());

    // set table
    tableField.setTable(tableField.new Table(), true);

    // revert second call
    tableField.setValueChangeTriggerEnabled(true);
    assertValueChangeTriggerEnabledFlag(tableField, false);

    // revert first call
    tableField.setValueChangeTriggerEnabled(true);
    assertValueChangeTriggerEnabledFlag(tableField, true);
  }

  @Test
  public void testDelegatingValueChangedTriggerEnabledPropertyExchangingTable() {
    P_TableField tableField = createTableField(false);
    assertValueChangeTriggerEnabledFlag(tableField, true);

    // first call
    tableField.setValueChangeTriggerEnabled(false);
    assertValueChangeTriggerEnabledFlag(tableField, false);

    // second call
    tableField.setValueChangeTriggerEnabled(false);
    assertValueChangeTriggerEnabledFlag(tableField, false);

    //exchanging the table; disabling the triggers on the table once before and once after
    ITable firstTable = tableField.getTable();
    firstTable.setValueChangeTriggerEnabled(false);
    tableField.setTable(tableField.new Table(), true);
    firstTable.setValueChangeTriggerEnabled(false);

    // third call; should not affect firstField any more
    tableField.setValueChangeTriggerEnabled(false);
    assertValueChangeTriggerEnabledFlag(tableField, false);

    // assert that removed first field now works independently
    assertValueChangeTriggerEnabledFlag(tableField, false);
    assertFalse(firstTable.isValueChangeTriggerEnabled());
    firstTable.setValueChangeTriggerEnabled(true);
    assertFalse(firstTable.isValueChangeTriggerEnabled());
    firstTable.setValueChangeTriggerEnabled(true);
    // only after the second reset the trigger should be enabled
    assertTrue(firstTable.isValueChangeTriggerEnabled());

    // revert third call
    tableField.setValueChangeTriggerEnabled(true);
    assertValueChangeTriggerEnabledFlag(tableField, false);

    // revert second call
    tableField.setValueChangeTriggerEnabled(true);
    assertValueChangeTriggerEnabledFlag(tableField, false);

    // revert first call
    tableField.setValueChangeTriggerEnabled(true);
    assertValueChangeTriggerEnabledFlag(tableField, true);

    // now exchange table with one that has it's trigger already disabled
    org.eclipse.scout.rt.client.ui.form.fields.tablefield.TableFieldTest.P_TableField.Table newTable = tableField.new Table();
    newTable.setValueChangeTriggerEnabled(false);
    newTable.setValueChangeTriggerEnabled(false);
    tableField.setTable(newTable, true);

    assertTrue(tableField.isValueChangeTriggerEnabled());
    assertFalse(tableField.getTable().isValueChangeTriggerEnabled());
    newTable.setValueChangeTriggerEnabled(true);
    assertTrue(tableField.isValueChangeTriggerEnabled());
    assertFalse(tableField.getTable().isValueChangeTriggerEnabled());
    // only after the second reset the trigger should be enabled on the table
    newTable.setValueChangeTriggerEnabled(true);
    assertTrue(firstTable.isValueChangeTriggerEnabled());
  }

  private void assertValueChangeTriggerEnabledFlag(P_TableField tableField, boolean isEnabledExpected) {
    assertEquals(isEnabledExpected, tableField.isValueChangeTriggerEnabled());
    assertEquals(isEnabledExpected, tableField.getTable().isValueChangeTriggerEnabled());
  }

  @Test
  public void testDelegatingValueChangedTriggerEnabledPropertyNoTable() {
    P_TableField tableField = createTableField(false);
    tableField.setTable(null, true);
    tableField.setValueChangeTriggerEnabled(false);
    assertFalse(tableField.isValueChangeTriggerEnabled());
  }

  @Test
  public void testImportFormFieldDataWithUpdatedRow() throws Exception {
    P_TableField tableField1 = createTableField(false);
    runImportFormFieldDataWithUpdatedRow(tableField1);

    P_TableField tableField2 = createTableField(true);
    runImportFormFieldDataWithUpdatedRow(tableField2);
  }

  private void runImportFormFieldDataWithUpdatedRow(P_TableField tableField) {
    P_TableBean tableData1 = createTableBeanData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertRowCount(3, tableField);
    assertHiddenColumnValues(false, true, false, tableField);

    updateThirdRow(tableField);
    assertRowCount(3, tableField);
    assertKeyColumnValues(1, 2, 3, tableField);
    assertStringColumnValues("Lorem", "Ipsum", "Amet", tableField);
    assertHiddenColumnValues(false, true, true, tableField);

    P_TableBean tableData2 = createTableBeanData(true, false, false, ITableHolder.STATUS_UPDATED);
    tableField.importFormFieldData(tableData2, false);

    assertRowCount(3, tableField);
    assertKeyColumnValues(1, 2, 3, tableField);
    assertStringColumnValues("Lorem", "Ipsum", "Dolor", tableField);
    assertHiddenColumnValues(true, false, false, tableField);
  }

  private void updateThirdRow(P_TableField tableField) {
    ITableRow updatedRow = tableField.getTable().getRow(2);
    updatedRow.getCellForUpdate(1).setValue("Amet");
    updatedRow.getCellForUpdate(2).setValue(true);
    tableField.getTable().updateRow(updatedRow);
  }

  private P_TableBean createTableBeanData(boolean r1_value, boolean r2_value, boolean r3_value, int state) {
    P_TableBean tableData = new P_TableBean();
    org.eclipse.scout.rt.client.ui.form.fields.tablefield.TableFieldTest.P_TableBean.TableBeanRowData row = tableData.addRow();
    row.setRowState(state);
    row.setKey(1);
    row.setString("Lorem");
    row.setHidden(r1_value);

    org.eclipse.scout.rt.client.ui.form.fields.tablefield.TableFieldTest.P_TableBean.TableBeanRowData row2 = tableData.addRow();
    row2.setRowState(state);
    row2.setKey(2);
    row2.setString("Ipsum");
    row2.setHidden(r2_value);

    org.eclipse.scout.rt.client.ui.form.fields.tablefield.TableFieldTest.P_TableBean.TableBeanRowData row3 = tableData.addRow();
    row3.setRowState(state);
    row3.setKey(3);
    row3.setString("Dolor");
    row3.setHidden(r3_value);

    return tableData;
  }

  @Test
  public void testImportFormFieldDataWithAllRowStates() throws Exception {
    P_TableField tableField1 = createTableField(false);
    runImportFormFieldDataWithAllRowStates(tableField1);

    P_TableField tableField2 = createTableField(true);
    runImportFormFieldDataWithAllRowStates(tableField2);

    P_TableField tableField3 = createTableField(false);
    runImportFormFieldBeanDataWithAllRowStates(tableField3);

    P_TableField tableField4 = createTableField(true);
    runImportFormFieldBeanDataWithAllRowStates(tableField4);

  }

  private void runImportFormFieldDataWithAllRowStates(P_TableField tableField) {
    P_TableBean tableData1 = createTableBeanData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertRowStates(ITableHolder.STATUS_NON_CHANGED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData2 = createTableBeanData(true, false, false, ITableHolder.STATUS_UPDATED);
    tableField.importFormFieldData(tableData2, false);

    assertRowStates(ITableHolder.STATUS_UPDATED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData3 = createTableBeanData(true, false, false, ITableHolder.STATUS_INSERTED);
    tableField.importFormFieldData(tableData3, false);

    assertRowStates(ITableHolder.STATUS_INSERTED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData4 = createTableBeanData(true, false, false, ITableHolder.STATUS_DELETED);
    tableField.importFormFieldData(tableData4, false);

    assertEquals(0, tableField.getTable().getRowCount());

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData5 = createTableBeanData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData5, true);

    assertRowStates(ITableHolder.STATUS_NON_CHANGED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData6 = createTableBeanData(true, false, false, ITableHolder.STATUS_UPDATED);
    tableField.importFormFieldData(tableData6, true);

    assertRowStates(ITableHolder.STATUS_UPDATED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData7 = createTableBeanData(true, false, false, ITableHolder.STATUS_INSERTED);
    tableField.importFormFieldData(tableData7, true);

    assertRowStates(ITableHolder.STATUS_INSERTED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData8 = createTableBeanData(true, false, false, ITableHolder.STATUS_DELETED);
    tableField.importFormFieldData(tableData8, false);

    assertEquals(0, tableField.getTable().getRowCount());
  }

  private void runImportFormFieldBeanDataWithAllRowStates(P_TableField tableField) {
    P_TableBean tableData1 = createTableBeanData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData1, false);

    assertRowStates(ITableHolder.STATUS_NON_CHANGED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData2 = createTableBeanData(true, false, false, ITableHolder.STATUS_UPDATED);
    tableField.importFormFieldData(tableData2, false);

    assertRowStates(ITableHolder.STATUS_UPDATED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData3 = createTableBeanData(true, false, false, ITableHolder.STATUS_INSERTED);
    tableField.importFormFieldData(tableData3, false);

    assertRowStates(ITableHolder.STATUS_INSERTED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData4 = createTableBeanData(true, false, false, ITableHolder.STATUS_DELETED);
    tableField.importFormFieldData(tableData4, false);

    assertEquals(0, tableField.getTable().getRowCount());

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData5 = createTableBeanData(false, true, false, ITableHolder.STATUS_NON_CHANGED);
    tableField.importFormFieldData(tableData5, true);

    assertRowStates(ITableHolder.STATUS_NON_CHANGED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData6 = createTableBeanData(true, false, false, ITableHolder.STATUS_UPDATED);
    tableField.importFormFieldData(tableData6, true);

    assertRowStates(ITableHolder.STATUS_UPDATED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData7 = createTableBeanData(true, false, false, ITableHolder.STATUS_INSERTED);
    tableField.importFormFieldData(tableData7, true);

    assertRowStates(ITableHolder.STATUS_INSERTED, tableField);

    tableField.getTable().deleteAllRows();
    tableField.getTable().discardAllDeletedRows();
    tableField.getTable().discardAllRows();
    P_TableBean tableData8 = createTableBeanData(true, false, false, ITableHolder.STATUS_DELETED);
    tableField.importFormFieldData(tableData8, false);

    assertEquals(0, tableField.getTable().getRowCount());
  }

  private static class P_TableField extends AbstractTableField<P_TableField.Table> {

    private final boolean m_configuredDiplayable;
    private final boolean m_configuredPrimaryKey;
    private boolean m_execContentChangedCalled;

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

    public class Table extends AbstractTable {

      @Override
      protected void execContentChanged() {
        m_execContentChangedCalled = true;
      }

      public KeyColumn getKeyColumn() {
        return getColumnSet().getColumnByClass(KeyColumn.class);
      }

      public HiddenColumn getHiddenColumn() {
        return getColumnSet().getColumnByClass(HiddenColumn.class);
      }

      public StringColumn getStringColumn() {
        return getColumnSet().getColumnByClass(StringColumn.class);
      }

      @Order(10)
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

      @Order(20)
      public class StringColumn extends AbstractStringColumn {
      }

      @Order(30)
      public class HiddenColumn extends AbstractBooleanColumn {
      }
    }
  }

  /**
   * Corresponding part as TableFieldBeanData of the formData:
   */
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
      public static final String KEY = "key";
      public static final String STRING = "string";
      public static final String HIDDEN = "hidden";
      private Integer m_key;
      private String m_string;
      private Boolean m_hidden;

      public TableBeanRowData() {
      }

      public Integer getKey() {
        return m_key;
      }

      public void setKey(Integer key) {
        m_key = key;
      }

      public String getString() {
        return m_string;
      }

      public void setString(String string) {
        m_string = string;
      }

      public Boolean getHidden() {
        return m_hidden;
      }

      public void setHidden(Boolean hidden) {
        m_hidden = hidden;
      }
    }
  }
}
