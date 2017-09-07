///*******************************************************************************
// * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     BSI Business Systems Integration AG - initial API and implementation
// ******************************************************************************/
//package org.eclipse.scout.rt.client.ui.basic.table.columns;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.mock;
//
//import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
//import org.eclipse.scout.rt.client.ui.basic.table.ITable;
//import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
//import org.eclipse.scout.rt.client.ui.basic.table.columns.fixture.TestCodeType;
//import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField;
//import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
//import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
//import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//
///**
// * Tests for {@link AbstractProposalColumn}
// */
//@RunWith(PlatformTestRunner.class)
//public class AbstractProposalColumnTest {
//
//  @Test
//  public void testPrepareEditInternal() {
//    AbstractProposalColumn<Long> column = new AbstractProposalColumn<Long>() {
//    };
//    column.setCodeTypeClass(TestCodeType.class);
//    column.setMandatory(true);
//    ITableRow row = Mockito.mock(ITableRow.class);
//    @SuppressWarnings("unchecked")
//    IProposalField<Long> field = (IProposalField<Long>) column.prepareEditInternal(row);
//    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
//    assertEquals("code type class property to be progagated to field", column.getCodeTypeClass(), field.getCodeTypeClass());
//  }
//
//  /**
//   * Tests that {@link AbstractProposalColumn#execPrepareLookup(ILookupCall, ITableRow)} is called when
//   * {@link IProposalField#prepareKeyLookup(ILookupCall, Object)} is called on the editor field.
//   */
//  @SuppressWarnings("unchecked")
//  @Test
//  public void testPrepareLookupCallback() {
//    TestProposalColumn column = new TestProposalColumn();
//    ITableRow row = Mockito.mock(ITableRow.class);
//    IProposalField<Long> field = (IProposalField<Long>) column.prepareEditInternal(row);
//    ILookupCall call = mock(ILookupCall.class);
//    field.prepareKeyLookup(call, 10L);
//    assertEquals(row, column.lastRow);
//    assertEquals(call, column.lastCall);
//  }
//
//  @Test
//  public void testLookupRowWithNullText() throws Exception {
//    AbstractProposalColumn<Long> column = new AbstractProposalColumn<Long>() {
//    };
//    column.setCodeTypeClass(TestCodeType.class);
//    column.setMandatory(true);
//    ITableRow row = Mockito.mock(ITableRow.class);
//    @SuppressWarnings("unchecked")
//    IProposalField<Long> field = (IProposalField<Long>) column.prepareEditInternal(row);
//
//    field.getUIFacade().acceptProposalFromUI("", false, false);
//    assertEquals(null, field.getDisplayText());
//    assertEquals(null, field.getValue());
//  }
//
//  @Test
//  public void testLookupRowWithUntrimmedText1() throws Exception {
//    final AbstractProposalColumn<Long> column = new AbstractProposalColumn<Long>() {
//      @Override
//      protected boolean getConfiguredEditable() {
//        return true;
//      }
//
//      @Override
//      protected boolean getConfiguredTrimText() {
//        return false;
//      }
//    };
//    column.setCodeTypeClass(TestCodeType.class);
//    final ITable table = new AbstractTable() {
//      @Override
//      protected void injectColumnsInternal(OrderedCollection<IColumn<?>> columns) {
//        columns.addFirst(column);
//      }
//    };
//    ITableRow row = table.addRow();
//
//    @SuppressWarnings("unchecked")
//    IProposalField<Long> field = (IProposalField<Long>) column.prepareEditInternal(row);
//    field.getUIFacade().acceptProposalFromUI(" a ", false, true);
//    column.completeEdit(row, field);
//    assertEquals(" a ", column.getValue(row));
//  }
//
//  @Test
//  public void testLookupRowWithUntrimmedText2() throws Exception {
//    final AbstractProposalColumn<Long> column = new AbstractProposalColumn<Long>() {
//      @Override
//      protected boolean getConfiguredEditable() {
//        return true;
//      }
//
//      @Override
//      protected boolean getConfiguredTrimText() {
//        return true;
//      }
//    };
//    column.setCodeTypeClass(TestCodeType.class);
//    final ITable table = new AbstractTable() {
//      @Override
//      protected void injectColumnsInternal(OrderedCollection<IColumn<?>> columns) {
//        columns.addFirst(column);
//      }
//    };
//    ITableRow row = table.addRow();
//
//    @SuppressWarnings("unchecked")
//    IProposalField<Long> field = (IProposalField<Long>) column.prepareEditInternal(row);
//    field.getUIFacade().acceptProposalFromUI(" a ", false, false);
//    column.completeEdit(row, field);
//    assertEquals("a", column.getValue(row));
//  }
//
//  @Test
//  public void testLookupRowWithTooLongText1() throws Exception {
//    final AbstractProposalColumn<Long> column = new AbstractProposalColumn<Long>() {
//      @Override
//      protected boolean getConfiguredEditable() {
//        return true;
//      }
//
//      @Override
//      protected int getConfiguredMaxLength() {
//        return 32;
//      }
//    };
//    column.setCodeTypeClass(TestCodeType.class);
//    final ITable table = new AbstractTable() {
//      @Override
//      protected void injectColumnsInternal(OrderedCollection<IColumn<?>> columns) {
//        columns.addFirst(column);
//      }
//    };
//    ITableRow row = table.addRow();
//
//    @SuppressWarnings("unchecked")
//    IProposalField<Long> field = (IProposalField<Long>) column.prepareEditInternal(row);
//    field.getUIFacade().acceptProposalFromUI("1234567890", false, false);
//    column.completeEdit(row, field);
//    assertEquals("1234567890", column.getValue(row));
//  }
//
//  @Test
//  public void testLookupRowWithTooLongText2() throws Exception {
//    final AbstractProposalColumn<Long> column = new AbstractProposalColumn<Long>() {
//      @Override
//      protected boolean getConfiguredEditable() {
//        return true;
//      }
//
//      @Override
//      protected int getConfiguredMaxLength() {
//        return 8;
//      }
//    };
//    column.setCodeTypeClass(TestCodeType.class);
//    final ITable table = new AbstractTable() {
//      @Override
//      protected void injectColumnsInternal(OrderedCollection<IColumn<?>> columns) {
//        columns.addFirst(column);
//      }
//    };
//    ITableRow row = table.addRow();
//
//    @SuppressWarnings("unchecked")
//    IProposalField<Long> field = (IProposalField<Long>) column.prepareEditInternal(row);
//    field.getUIFacade().acceptProposalFromUI("1234567890", false, false);
//    column.completeEdit(row, field);
//    assertEquals("12345678", column.getValue(row));
//  }
//
//  private static class TestProposalColumn extends AbstractProposalColumn<Long> {
//    ILookupCall<Long> lastCall;
//    ITableRow lastRow;
//
//    @Override
//    protected void execPrepareLookup(ILookupCall<Long> call, ITableRow row) {
//      lastCall = call;
//      lastRow = row;
//    }
//  }
//
//}
