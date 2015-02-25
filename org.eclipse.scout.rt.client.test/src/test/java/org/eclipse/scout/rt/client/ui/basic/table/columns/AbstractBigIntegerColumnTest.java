/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import java.text.NumberFormat;
import java.util.Locale;

import org.easymock.EasyMock;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield.AbstractBigIntegerField;
import org.junit.Test;

/**
 * JUnit tests for {@link AbstractBigIntegerColumn}
 * 
 * @since 4.0.0
 */
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
  public void testEditorFieldMinAndMaxValue() throws ProcessingException {
    ITableRow row = EasyMock.createMock(ITableRow.class);
    AbstractBigIntegerField field = (AbstractBigIntegerField) prepareEditInternal(row);

    assertEquals("minValue not mapped to editor field", MIN_VALUE, field.getMinValue());
    assertEquals("maxValue not mapped to editor field", MAX_VALUE, field.getMaxValue());
  }

  @Test
  public void testFormattingInDecorateCellInternal() throws ProcessingException {
    ITableRow row = EasyMock.createMock(ITableRow.class);
    Cell cell = new Cell();
    BigInteger testValue = BigInteger.valueOf(-123456789);
    cell.setValue(testValue);

    DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.CANADA_FRENCH);
    setFormat(df);

    decorateCellInternal(cell, row);
    assertEquals("cell text not formatted as expected", df.format(testValue), cell.getText());
  }

}
