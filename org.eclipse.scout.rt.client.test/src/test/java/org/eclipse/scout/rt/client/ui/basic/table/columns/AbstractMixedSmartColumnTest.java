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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.fixture.TestCodeType;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.ScoutFieldStatus;
import org.eclipse.scout.rt.client.ui.form.fields.ValidationFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractMixedSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IMixedSmartField;
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
 * Tests for {@link AbstractMixedSmartColumn}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractMixedSmartColumnTest {

  @Test
  public void testPrepareEditInternal() {
    AbstractMixedSmartColumn<Long, Long> column = new AbstractMixedSmartColumn<Long, Long>() {
    };
    column.setCodeTypeClass(TestCodeType.class);
    column.setMandatory(true);
    ITableRow row = mock(ITableRow.class);
    @SuppressWarnings("unchecked")
    IMixedSmartField<Long, Long> field = (IMixedSmartField<Long, Long>) column.prepareEditInternal(row);
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
    assertEquals("code type class property to be progagated to field", column.getCodeTypeClass(), field.getCodeTypeClass());
    assertEquals("browse new text to be progagated to field", column.getConfiguredBrowseNewText(), field.getBrowseNewText());
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
    IMixedSmartField<Long, Long> field = (IMixedSmartField<Long, Long>) table.getEditableSmartColumn().prepareEdit(testRow);
    field.parseAndSetValue(TestCodeType.TestCode.TEXT);
    table.getEditableSmartColumn().completeEdit(testRow, field);
    assertNull(field.getErrorStatus());
    assertTrue(testRow.getCellForUpdate(table.getEditableSmartColumn()).isContentValid());
  }

  /**
   * An unparsable error should lead to an error on the column
   */
  @Test
  public void testSetUnparsableValue() {
    P_Table table = new P_Table();
    table.addRowsByArray(new Object[]{1L});
    ITableRow testRow = table.getRow(0);
    @SuppressWarnings("unchecked")
    IMixedSmartField<Long, Long> field = (IMixedSmartField<Long, Long>) table.getEditableSmartColumn().prepareEdit(testRow);
    field.parseAndSetValue("-1L");
    table.getEditableSmartColumn().completeEdit(testRow, field);
    assertNotNull(field.getErrorStatus());
    assertFalse(testRow.getCellForUpdate(table.getEditableSmartColumn()).isContentValid());
  }

  /**
   * An unparsable error should be reset, if a valid value is entered
   */
  @Test
  public void testResetParsingError() {
    P_Table table = new P_Table();
    table.addRowsByArray(new Object[]{1L});
    ITableRow testRow = table.getRow(0);
    @SuppressWarnings("unchecked")
    IMixedSmartField<Long, Long> field = (IMixedSmartField<Long, Long>) table.getEditableSmartColumn().prepareEdit(testRow);
    field.parseAndSetValue("-1L");
    table.getEditableSmartColumn().completeEdit(testRow, field);
    field.parseAndSetValue(TestCodeType.TestCode.TEXT);
    table.getEditableSmartColumn().completeEdit(testRow, field);
    assertNull(field.getErrorStatus());
    assertTrue(testRow.getCellForUpdate(table.getEditableSmartColumn()).isContentValid());
  }

  /**
   * Tests that {@link AbstractMixedSmartColumn#execPrepareLookup(ILookupCall, ITableRow)} is called when
   * {@link IMixedSmartField#prepareKeyLookup(ILookupCall, Object)} is called on the editor field.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testPrepareLookupCallback() {
    TestMixedSmartColumn column = new TestMixedSmartColumn();
    ITableRow row = Mockito.mock(ITableRow.class);
    IMixedSmartField<String, Long> field = (IMixedSmartField<String, Long>) column.prepareEditInternal(row);
    ILookupCall call = mock(ILookupCall.class);
    field.prepareKeyLookup(call, 10L);
    assertEquals(row, column.lastRow);
    assertEquals(call, column.lastCall);
  }

  /**
   * Tests that {@link AbstractMixedSmartColumn#execPrepareLookup(ILookupCall, ITableRow)} is called when
   * {@link IMixedSmartField#prepareKeyLookup(ILookupCall, Object)} is called on the editor field.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void tesConvertValueToKeyCallback() {
    TestMixedSmartColumn column = new TestMixedSmartColumn();
    ITableRow row = Mockito.mock(ITableRow.class);
    AbstractMixedSmartField<String, Long> field = (AbstractMixedSmartField<String, Long>) column.prepareEditInternal(row);
    field.setValue("test");
    Long lookupKey = field.getValueAsLookupKey();
    assertEquals(Long.valueOf(0L), lookupKey);
    assertEquals(column.lastValue, "test");
  }

  @Test
  public void testCompleteEdit_ParsingError() throws Exception {
    assertCompleteEditWithErrors(false, ParsingFailedStatus.class);
  }

  /**
   * Tests an issue from ticket #168697. When user has entered a search-text which returned no lookup-rows, the error
   * was not displayed in the editable cell. Its basically the same test
   */
  @Test
  public void testCompleteEdit_ValidationError() throws Exception {
    assertCompleteEditWithErrors(true, ValidationFailedStatus.class);
  }

  private void assertCompleteEditWithErrors(boolean useUiFacade, Class<? extends ScoutFieldStatus> statusClass) throws Exception {
    P_Table table = new P_Table();
    table.addRowsByArray(new Long[]{3L});
    IMixedSmartField<?, ?> field = (IMixedSmartField<?, ?>) table.getEditableSmartColumn().prepareEdit(table.getRow(0));
    waitUntilLookupRowsLoaded();
    if (useUiFacade) {
      field.getUIFacade().acceptProposalFromUI("invalid Text", false, false);
    }
    else {
      field.parseAndSetValue("invalid Text");
    }
    table.getEditableSmartColumn().completeEdit(table.getRow(0), field);
    ICell c = table.getCell(0, 0);
    assertEquals("invalid Text", c.getText());
    assertNotNull(String.format("The invalid cell should have an error status: value '%s'", c.getValue(), c.getErrorStatus()));
    assertTrue(c.getErrorStatus().containsStatus(statusClass));
  }

  class TestMixedSmartColumn extends AbstractMixedSmartColumn<String, Long> {
    ILookupCall<Long> lastCall;
    ITableRow lastRow;
    String lastValue;

    @Override
    protected void execPrepareLookup(ILookupCall<Long> call, ITableRow row) {
      lastCall = call;
      lastRow = row;
    }

    @Override
    protected Long execConvertValueToKey(String value) {
      lastValue = value;
      return 0L;
    }
  }

  public static class P_Table extends AbstractTable {

    public EditableSmartColumn getEditableSmartColumn() {
      return getColumnSet().getColumnByClass(EditableSmartColumn.class);
    }

    public static class EditableSmartColumn extends AbstractMixedSmartColumn<String, Long> {

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
    JobTestUtil.waitForMinimalPermitCompetitors(ModelJobs.newInput(ClientRunContexts.copyCurrent()).getExecutionSemaphore(), 2); // 2:= 'current model job' + 'smartfield fetch model job'
    // Yield the current model job permit, so that the lookup rows can be written into the model.
    ModelJobs.yield();
  }
}
