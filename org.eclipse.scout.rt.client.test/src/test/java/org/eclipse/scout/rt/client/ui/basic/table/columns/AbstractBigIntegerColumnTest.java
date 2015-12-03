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

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Locale;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield.AbstractBigIntegerField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * JUnit tests for {@link AbstractBigIntegerColumn}
 *
 * @since 4.0.0
 */
@RunWith(PlatformTestRunner.class)
public class AbstractBigIntegerColumnTest extends AbstractBigIntegerColumn {

  private static final BigInteger MIN_VALUE = new BigInteger("-999999999994");
  private static final BigInteger MAX_VALUE = new BigInteger("999999999991");

  @Override
  protected BigInteger getConfiguredMinValue() {
    return MIN_VALUE;
  }

  @Override
  protected BigInteger getConfiguredMaxValue() {
    return MAX_VALUE;
  }

  @Test
  public void testEditorFieldMinAndMaxValue() {
    ITableRow row = Mockito.mock(ITableRow.class);
    AbstractBigIntegerField field = (AbstractBigIntegerField) prepareEditInternal(row);

    assertEquals("minValue not mapped to editor field", MIN_VALUE, field.getMinValue());
    assertEquals("maxValue not mapped to editor field", MAX_VALUE, field.getMaxValue());
  }

  @Test
  public void testFormattingInDecorateCellInternal() {
    ITableRow row = Mockito.mock(ITableRow.class);
    Cell cell = new Cell();
    BigInteger testValue = BigInteger.valueOf(-123456789);
    cell.setValue(testValue);

    DecimalFormat df = BEANS.get(NumberFormatProvider.class).getNumberInstance(Locale.CANADA_FRENCH);
    setFormat(df);

    updateDisplayText(row, cell);
    assertEquals("cell text not formatted as expected", df.format(testValue), cell.getText());
  }

}
