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
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(PlatformTestRunner.class)
public class AbstractBigDecimalColumnTest extends AbstractBigDecimalColumn {

  @Test
  public void testFormattingInDecorateCellInternal() {
    ITableRow row = Mockito.mock(ITableRow.class);
    Cell cell = new Cell();
    BigDecimal testValue = new BigDecimal("-123456789.12345");
    cell.setValue(testValue);

    for (Locale locale : DecimalFormat.getAvailableLocales()) {
      DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(locale);
      df.applyPattern(getFormat().toPattern());
      df.setMaximumFractionDigits(4);
      df.setRoundingMode(RoundingMode.HALF_UP);
      setFormat(df);

      char decimalSeparator = df.getDecimalFormatSymbols().getDecimalSeparator();
      updateDisplayText(row, cell);
      assertEquals("cell text not formatted as expected", df.format(testValue), cell.getText());
      DecimalFormat df1235 = (DecimalFormat) DecimalFormat.getInstance(locale);
      df1235.applyPattern("#");
      assertTrue("cell text[" + cell.getText() + "] not rounded as expected", cell.getText().endsWith(decimalSeparator + df1235.format(1235)));
    }
  }

}
