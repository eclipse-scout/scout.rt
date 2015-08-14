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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.services.lookup.DefaultLookupCallProvisioningService;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.fixture.TestCodeType;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.DefaultCodeLookupCallFactoryService;
import org.eclipse.scout.rt.shared.services.lookup.ICodeLookupCallFactoryService;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.common.code.TestingCodeService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractSmartColumnTest {
  private P_Table table = new P_Table();
  private static List<IBean<?>> s_regs;

  @BeforeClass
  public static void beforeClass() throws Exception {
    TestingCodeService codeService = new TestingCodeService(new TestCodeType());
    DefaultCodeLookupCallFactoryService codeLookupCallFactoryService = new DefaultCodeLookupCallFactoryService();
    s_regs = TestingUtility.registerBeans(
        new BeanMetaData(ICodeService.class).
            withInitialInstance(codeService).
            withApplicationScoped(true),
        new BeanMetaData(ICodeLookupCallFactoryService.class).
            withInitialInstance(codeLookupCallFactoryService).
            withApplicationScoped(true),
        new BeanMetaData(ILookupCallProvisioningService.class).
            withInitialInstance(new DefaultLookupCallProvisioningService()).
            withApplicationScoped(true)
        );
  }

  @AfterClass
  public static void afterClass() throws Exception {
    TestingUtility.unregisterBeans(s_regs);
  }

  @Test
  public void testResetInvalidValue() throws ProcessingException {
    table.addRowsByArray(new Long[]{3L});
    ITableRow testRow = table.getRow(0);

    parseAndSetInEditField(testRow, "invalid Text");
    parseAndSetInEditField(testRow, "Test");

    assertEquals("Test", testRow.getCell(0).getText());
    assertEquals(0L, testRow.getCell(0).getValue());
    assertTrue(testRow.getCell(0).isContentValid());
  }

  private void parseAndSetInEditField(ITableRow testRow, String text) throws ProcessingException {
    IValueField<?> field = prepareTestEdit();
    field.parseAndSetValue(text);
    table.getEditableSmartColumn().completeEdit(testRow, field);
  }

  private IValueField prepareTestEdit() throws ProcessingException {
    return (IValueField<?>) table.getEditableSmartColumn().prepareEdit(table.getRow(0));
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

}
