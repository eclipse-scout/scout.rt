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
import java.text.DecimalFormat;
import java.util.Locale;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class AbstractDecimalFieldTest extends AbstractDecimalField<BigDecimal> {

  @Before
  public void setup() {
    LocaleThreadLocal.set(new Locale("de", "CH"));
  }

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

  @Override
  protected BigDecimal parseValueInternal(String text) throws ProcessingException {
    return parseToBigDecimalInternal(text);
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
  public void testParseValueInternalPercent() throws ProcessingException {
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string containing '%' and property 'percent' is not set to 'true'.", this, "59.88%");

    setPercent(true);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88 %"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88%"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88"));

    setMultiplier(100);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.5988), parseValueInternal("59.88 %"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.5988), parseValueInternal("59.88%"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.5988), parseValueInternal("59.88"));

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

  @Test
  public void testSetPercent() throws ProcessingException {
    DecimalFormat dfPercent = (DecimalFormat) DecimalFormat.getPercentInstance(LocaleThreadLocal.get());

    // test default
    Assert.assertFalse(isPercent());
    Assert.assertEquals("", getFormatInternal().getPositiveSuffix());
    Assert.assertEquals("", getFormatInternal().getNegativeSuffix());
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string containing '%' and property 'percent' is not set to 'true'.", this, "59.88 %");
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88"));

    setPercent(true);
    Assert.assertTrue(isPercent());
    Assert.assertEquals(dfPercent.getPositiveSuffix(), getFormatInternal().getPositiveSuffix());
    Assert.assertEquals(dfPercent.getNegativeSuffix(), getFormatInternal().getNegativeSuffix());
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88 %"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88"));

    setPercent(false);
    Assert.assertFalse(isPercent());
    Assert.assertEquals("", getFormatInternal().getPositiveSuffix());
    Assert.assertEquals("", getFormatInternal().getNegativeSuffix());
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string containing '%' and property 'percent' is not set to 'true'.", this, "59.88 %");
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88"));

    // manually setting suffixes
    getFormatInternal().setPositiveSuffix(dfPercent.getPositiveSuffix());
    Assert.assertFalse(isPercent());
    getFormatInternal().setNegativeSuffix(dfPercent.getNegativeSuffix());
    Assert.assertTrue(isPercent());
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88 %"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88"));

  }
}
