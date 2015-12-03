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
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.fixture.TestCodeType;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractSmartColumnTest {
  private P_Table table = new P_Table();

  @Test
  public void testResetInvalidValue() {
    table.addRowsByArray(new Long[]{3L});
    ITableRow testRow = table.getRow(0);

    parseAndSetInEditField(testRow, "invalid Text");
    assertFalse(testRow.getCell(0).isContentValid());
    parseAndSetInEditField(testRow, "Test");

    assertEquals("Test", testRow.getCell(0).getText());
    assertEquals(0L, testRow.getCell(0).getValue());
    assertTrue(testRow.getCell(0).isContentValid());
  }

  private void parseAndSetInEditField(ITableRow testRow, String text) {
    IValueField<?> field = prepareTestEdit();
    field.parseAndSetValue(text);
    table.getEditableSmartColumn().completeEdit(testRow, field);
  }

  private IValueField prepareTestEdit() {
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
