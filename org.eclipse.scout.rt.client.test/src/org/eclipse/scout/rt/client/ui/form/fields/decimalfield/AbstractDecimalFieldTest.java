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
package org.eclipse.scout.rt.client.ui.form.fields.decimalfield;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class AbstractDecimalFieldTest extends AbstractDecimalField<BigDecimal> {

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
    return BigDecimal.ROUND_HALF_EVEN;
  }

  @Override
  protected int getConfiguredFractionDigits() {
    return 5;
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

  @Test
  public void testFormatValueInternal() {
    setMaxFractionDigits(3);
    setRoundingMode(RoundingMode.HALF_EVEN);
    Assert.assertEquals("12.246", formatValueInternal(BigDecimal.valueOf(12.2465)));
    setRoundingMode(RoundingMode.HALF_UP);
    Assert.assertEquals("12.247", formatValueInternal(BigDecimal.valueOf(12.2465)));
    setRoundingMode(RoundingMode.UNNECESSARY);

    boolean exceptionOccured = false;
    try {
      formatValueInternal(BigDecimal.valueOf(12.2465));
    }
    catch (ArithmeticException e) {
      exceptionOccured = true;
    }
    assertTrue("Expected an ArithmeticException when formatting a value with more fraction digits than maxFractionDigits and RoundingMode.UNNECESSARY.", exceptionOccured);
  }
}
