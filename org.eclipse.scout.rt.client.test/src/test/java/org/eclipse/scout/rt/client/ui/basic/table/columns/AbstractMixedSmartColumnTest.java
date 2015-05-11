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
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.fixture.TestCodeType;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractMixedSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IMixedSmartField;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.lookup.DefaultCodeLookupCallFactoryService;
import org.eclipse.scout.rt.shared.services.lookup.ICodeLookupCallFactoryService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.common.code.TestingCodeService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link AbstractMixedSmartColumn}
 */
public class AbstractMixedSmartColumnTest {

  private static List<IBean<?>> s_regs;

  @BeforeClass
  public static void beforeClass() throws Exception {
    TestingCodeService codeService = new TestingCodeService(new TestCodeType());
    DefaultCodeLookupCallFactoryService codeLookupCallFactoryService = new DefaultCodeLookupCallFactoryService();
    s_regs = TestingUtility.registerBeans(
        new BeanMetaData(ICodeService.class).
            initialInstance(codeService).
            applicationScoped(true),
        new BeanMetaData(ICodeLookupCallFactoryService.class).
            initialInstance(codeLookupCallFactoryService).
            applicationScoped(true)
        );
  }

  @AfterClass
  public static void afterClass() throws Exception {
    TestingUtility.unregisterBeans(s_regs);
  }

  @Test
  public void testPrepareEditInternal() throws ProcessingException {
    AbstractMixedSmartColumn<Long, Long> column = new AbstractMixedSmartColumn<Long, Long>() {
    };
    column.setCodeTypeClass(TestCodeType.class);
    column.setMandatory(true);
    ITableRow row = Mockito.mock(ITableRow.class);
    @SuppressWarnings("unchecked")
    IMixedSmartField<Long, Long> field = (IMixedSmartField<Long, Long>) column.prepareEditInternal(row);
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
    assertEquals("code type class property to be progagated to field", column.getCodeTypeClass(), field.getCodeTypeClass());
  }

  /**
   * Tests that {@link AbstractMixedSmartColumn#execPrepareLookup(ILookupCall, ITableRow)} is called when
   * {@link IMixedSmartField#prepareKeyLookup(ILookupCall, Object)} is called on the editor field.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testPrepareLookupCallback() throws ProcessingException {
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
  public void tesConvertValueToKeyCallback() throws ProcessingException {
    TestMixedSmartColumn column = new TestMixedSmartColumn();
    ITableRow row = Mockito.mock(ITableRow.class);
    AbstractMixedSmartField<String, Long> field = (AbstractMixedSmartField<String, Long>) column.prepareEditInternal(row);
    field.setValue("test");
    Long lookupKey = field.getValueAsLookupKey();
    assertEquals(Long.valueOf(0L), lookupKey);
    assertEquals(column.lastValue, "test");
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

}
