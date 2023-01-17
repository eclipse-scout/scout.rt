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
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.fixture.TestCodeType;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.ValidationFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Tests for {@link AbstractSmartColumn}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractMixedSmartColumnTest {

  @Test
  public void testPrepareEditInternal() {
    AbstractSmartColumn<Long> column = new AbstractSmartColumn<>() {
    };
    column.setCodeTypeClass(TestCodeType.class);
    column.setMandatory(true);
    ITableRow row = mock(ITableRow.class);
    @SuppressWarnings("unchecked")
    ISmartField<Long> field = (ISmartField<Long>) column.prepareEditInternal(row);
    assertEquals("mandatory property to be propagated to field", column.isMandatory(), field.isMandatory());
    assertEquals("code type class property to be propagated to field", column.getCodeTypeClass(), field.getCodeTypeClass());
  }

  /**
   * Tests successful editing of a table cell
   */
  @Test
  public void testEditingValidValue() {
    P_Table table = new P_Table();
    table.addRowsByArray(new Object[]{1L});
    ITableRow testRow = table.getRow(0);
    @SuppressWarnings("unchecked")
    ISmartField<Long> field = (ISmartField<Long>) table.getEditableSmartColumn().prepareEdit(testRow);
    field.parseAndSetValue(TestCodeType.TestCode.TEXT);
    table.getEditableSmartColumn().completeEdit(testRow, field);
    assertNull(field.getErrorStatus());
    assertTrue(testRow.getCellForUpdate(table.getEditableSmartColumn()).isContentValid());
  }

  /**
   * An unparseable error should lead to an error on the column
   */
  @Test
  public void testSetUnparseableValue() {
    P_Table table = new P_Table();
    table.addRowsByArray(new Object[]{1L});
    ITableRow testRow = table.getRow(0);
    @SuppressWarnings("unchecked")
    ISmartField<Long> field = (ISmartField<Long>) table.getEditableSmartColumn().prepareEdit(testRow);
    field.parseAndSetValue("-1L");
    table.getEditableSmartColumn().completeEdit(testRow, field);
    assertNotNull(field.getErrorStatus());
    assertFalse(testRow.getCellForUpdate(table.getEditableSmartColumn()).isContentValid());
  }

  /**
   * An unparseable error should be reset, if a valid value is entered
   */
  @Test
  public void testResetParsingError() {
    P_Table table = new P_Table();
    table.addRowsByArray(new Object[]{1L});
    ITableRow testRow = table.getRow(0);
    @SuppressWarnings("unchecked")
    ISmartField<Long> field = (ISmartField<Long>) table.getEditableSmartColumn().prepareEdit(testRow);
    field.parseAndSetValue("-1L");
    table.getEditableSmartColumn().completeEdit(testRow, field);
    field.parseAndSetValue(TestCodeType.TestCode.TEXT);
    table.getEditableSmartColumn().completeEdit(testRow, field);
    assertNull(field.getErrorStatus());
    assertTrue(testRow.getCellForUpdate(table.getEditableSmartColumn()).isContentValid());
  }

  /**
   * Tests that {@link AbstractSmartColumn#execPrepareLookup(ILookupCall, ITableRow)} is called when
   * {@link ISmartField#prepareKeyLookup(ILookupCall, Object)} is called on the editor field.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testPrepareLookupCallback() {
    TestMixedSmartColumn column = new TestMixedSmartColumn();
    ITableRow row = Mockito.mock(ITableRow.class);
    ISmartField<Long> field = (ISmartField<Long>) column.prepareEditInternal(row);
    ILookupCall call = mock(ILookupCall.class);
    field.prepareKeyLookup(call, 10L);
    assertEquals(row, column.lastRow);
    assertEquals(call, column.lastCall);
  }

  @Test
  public void testCompleteEdit_ParsingError() {
    assertCompleteEditWithErrors(false, ParsingFailedStatus.class);
  }

  /**
   * Tests an issue from ticket #168697. When user has entered a search-text which returned no lookup-rows, the error
   * was not displayed in the editable cell. It is basically the same test
   */
  @Test
  public void testCompleteEdit_ValidationError() {
    assertCompleteEditWithErrors(true, ValidationFailedStatus.class);
  }

  private void assertCompleteEditWithErrors(boolean useUiFacade, Class<? extends IStatus> statusClass) {
    P_Table table = new P_Table();
    table.addRowsByArray(new Long[]{3L});
    ISmartField<?> field = (ISmartField<?>) table.getEditableSmartColumn().prepareEdit(table.getRow(0));
    waitUntilLookupRowsLoaded();
    if (useUiFacade) {
      field.getUIFacade().setDisplayTextFromUI("invalid Text");
      field.getUIFacade().setErrorStatusFromUI(new ValidationFailedStatus("invalid Text"));
    }
    else {
      field.parseAndSetValue("invalid Text");
    }
    table.getEditableSmartColumn().completeEdit(table.getRow(0), field);
    ICell c = table.getCell(0, 0);
    assertEquals("invalid Text", c.getText());
    assertNotNull(String.format("The invalid cell should have an error status: value '%s', error-status: '%s'.", c.getValue(), c.getErrorStatus()));
    assertTrue(c.getErrorStatus().containsStatus(statusClass));
  }

  class TestMixedSmartColumn extends AbstractSmartColumn<Long> {
    ILookupCall<Long> lastCall;
    ITableRow lastRow;

    @Override
    protected void execPrepareLookup(ILookupCall<Long> call, ITableRow row) {
      lastCall = call;
      lastRow = row;
    }
  }

  public static class P_Table extends AbstractTable {

    public EditableSmartColumn getEditableSmartColumn() {
      return getColumnSet().getColumnByClass(EditableSmartColumn.class);
    }

    public static class EditableSmartColumn extends AbstractSmartColumn<Long> {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected Class<? extends ICodeType<?, Long>> getConfiguredCodeType() {
        return TestCodeType.class;
      }
    }
  }

  /**
   * Waits for at most 30s until lookup rows are loaded.
   */
  private static void waitUntilLookupRowsLoaded() {
    Assertions.assertTrue(ModelJobs.isModelThread(), "must be invoked from model thread");

    // Wait until asynchronous load of lookup rows is completed and ready to be written back to the smart field.
    JobTestUtil.waitForMinimalPermitCompetitors(ModelJobs.newInput(ClientRunContexts.copyCurrent()).getExecutionSemaphore(), 1);
    // Yield the current model job permit, so that the lookup rows can be written into the model.
    ModelJobs.yield();
  }
}
