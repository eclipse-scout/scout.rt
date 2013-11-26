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
package org.eclipse.scout.rt.client.ui.form.fields.numberfield;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.junit.Test;

/**
 *
 */
public class AbstractNumberFieldTest extends AbstractNumberField<BigDecimal> {

  @Override
  protected BigDecimal getConfiguredMinValue() {
    return null;
  }

  @Override
  protected BigDecimal getConfiguredMaxValue() {
    return null;
  }

  @Override
  protected int getConfiguredRoundingMode() {
    return INumberField.ROUND_HALF_EVEN;
  }

  @Override
  protected DecimalFormat createDecimalFormat() {
    DecimalFormat df = super.createDecimalFormat();
    df.setMaximumFractionDigits(10);
    return df;
  }

  @Test
  public void testParseToBigDecimalInternal() throws ProcessingException {
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(42), parseToBigDecimalInternal("42"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-42), parseToBigDecimalInternal("-42"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0), parseToBigDecimalInternal("0"));

    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(42.8532), parseToBigDecimalInternal("42.8532"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-42.77234), parseToBigDecimalInternal("-42.77234"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0), parseToBigDecimalInternal("0.00000"));
  }

  public static void assertParseToBigDecimalInternalThrowsProcessingException(String msg, AbstractNumberField<?> field, String textValue) {
    boolean exceptionOccured = false;
    try {
      field.parseToBigDecimalInternal(textValue);
    }
    catch (ProcessingException e) {
      exceptionOccured = true;
    }
    assertTrue(msg, exceptionOccured);
  }

}
