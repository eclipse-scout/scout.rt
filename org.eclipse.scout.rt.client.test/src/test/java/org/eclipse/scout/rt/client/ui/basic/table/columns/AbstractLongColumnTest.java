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
import static org.mockito.Mockito.mock;

import java.text.DecimalFormat;
import java.util.Locale;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.ILongField;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractLongColumn}
 */
@RunWith(PlatformTestRunner.class)
public class AbstractLongColumnTest extends AbstractLongColumn {

  @Test
  public void testFormattingInDecorateCellInternal() {
    ITableRow row = mock(ITableRow.class);
    Cell cell = new Cell();
    Long testValue = Long.valueOf(-123456789);
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
    AbstractLongColumn column = new AbstractLongColumn() {
    };
    column.setMandatory(true);
    ITableRow row = mock(ITableRow.class);
    ILongField field = (ILongField) column.prepareEditInternal(row);
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
  }

}
