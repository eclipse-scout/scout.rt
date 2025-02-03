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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.text.DecimalFormat;
import java.util.Locale;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.IIntegerField;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(PlatformTestRunner.class)
public class AbstractIntegerColumnTest extends AbstractIntegerColumn {

  @Test
  public void testFormattingInDecorateCellInternal() {
    ITableRow row = Mockito.mock(ITableRow.class);
    Cell cell = new Cell();
    Integer testValue = Integer.valueOf(-123456789);
    cell.setValue(testValue);

    for (Locale locale : DecimalFormat.getAvailableLocales()) {
      DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(locale);
      df.applyPattern(getFormat().toPattern());
      setFormat(df);

      updateDisplayText(row, cell);
      assertEquals("cell text not formatted as expected", df.format(testValue), cell.getText());
    }
  }

  @Test
  public void testPrepareEditInternal() {
    AbstractIntegerColumn column = new AbstractIntegerColumn() {
    };
    column.setMandatory(true);
    ITableRow row = mock(ITableRow.class);
    IIntegerField field = (IIntegerField) column.prepareEditInternal(row);
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
  }
}
