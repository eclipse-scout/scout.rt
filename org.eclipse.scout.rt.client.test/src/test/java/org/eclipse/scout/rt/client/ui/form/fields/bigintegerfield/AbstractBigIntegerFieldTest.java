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
package org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class AbstractBigIntegerFieldTest extends AbstractBigIntegerField {

  private NumberFormat m_formatter;
  private char m_decimalSeparator;

  @Before
  public void setUp() {
    m_formatter = DecimalFormat.getInstance();
    DecimalFormat df = (DecimalFormat) DecimalFormat.getNumberInstance();
    m_decimalSeparator = df.getDecimalFormatSymbols().getDecimalSeparator();
  }

  @Test
  public void testParseValueInternalNull() {
    assertEquals("expected null return for null input", null, parseValueInternal(null));
  }

  @Test
  public void testParseValue() {
    // maxValue and minValue must not have an influence for parsing
    setMaxValue(BigInteger.valueOf(99));
    setMinValue(BigInteger.valueOf(-99));

    assertEquals("parsing failed", BigInteger.valueOf(0), parseValueInternal("0"));
    assertEquals("parsing failed", BigInteger.valueOf(42), parseValueInternal("42"));
    assertEquals("parsing failed", BigInteger.valueOf(-42), parseValueInternal("-42"));
    assertEquals("parsing failed", BigInteger.valueOf(101), parseValueInternal("101"));
    assertEquals("parsing failed", BigInteger.valueOf(-101), parseValueInternal("-101"));
  }

  @Test
  public void testParseValueInternalAroundPossibleMinMaxValue() {

    assertEquals("parsing failed", getMaxPossibleValue(), parseValueInternal(new BigDecimal(getMaxPossibleValue()).toPlainString()));
    assertEquals("parsing failed", getMinPossibleValue(), parseValueInternal(new BigDecimal(getMinPossibleValue()).toPlainString()));
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsRuntimeException("Expected an exception when parsing a string representing a too big number.", this,
        new BigDecimal(getMaxPossibleValue().add(BigInteger.ONE)).toPlainString());
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsRuntimeException("Expected an exception when parsing a string representing a too small number.", this,
        new BigDecimal(getMinPossibleValue().subtract(BigInteger.ONE)).toPlainString());
  }

  @Test
  public void testParseValueInternalNotANumber() {
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsRuntimeException("Expected an exception when parsing a string not representing a number.", this, "onethousend");
  }

  @Test
  public void testParseValueInternalDecimal() {
    // expecting RoundingMode.UNNECESSARY as default
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsRuntimeException("Expected an exception when parsing a string representing a decimal value.", this, formatWithFractionDigits(12.1, 1));
    Assert.assertEquals("parsing failed", BigInteger.valueOf(12), parseValueInternal(formatWithFractionDigits(12.0, 1)));

    setRoundingMode(RoundingMode.HALF_UP);
    Assert.assertEquals("parsing failed", BigInteger.valueOf(12), parseValueInternal(formatWithFractionDigits(12.0, 1)));
    Assert.assertEquals("parsing failed", BigInteger.valueOf(12), parseValueInternal(formatWithFractionDigits(12.1, 1)));
    Assert.assertEquals("parsing failed", BigInteger.valueOf(13), parseValueInternal(formatWithFractionDigits(12.5, 1)));

    Assert.assertEquals("parsing failed", new BigInteger("99999999999999999999999999999999999999999999999999999999999"),
        parseValueInternal("99999999999999999999999999999999999999999999999999999999999" + m_decimalSeparator + "40007"));
    Assert.assertEquals("parsing failed", new BigInteger("99999999999999999999999999999999999999999999999999999999999"),
        parseValueInternal("99999999999999999999999999999999999999999999999999999999998" + m_decimalSeparator + "5"));
    Assert.assertEquals("parsing failed", new BigInteger("99999999999999999999999999999999999999999999999999999999999"),
        parseValueInternal("99999999999999999999999999999999999999999999999999999999998" + m_decimalSeparator + "999999999999999999999999999999999999999999999999999999999999"));

    setRoundingMode(RoundingMode.HALF_EVEN);
    Assert.assertEquals("parsing failed", BigInteger.valueOf(12), parseValueInternal(formatWithFractionDigits(12.5, 1)));
  }

  @Test
  public void testNoErrorMessage() {
    setValue(BigInteger.valueOf(5));
    assertNull(getErrorStatus());
    setValue(getMinPossibleValue());
    assertNull(getErrorStatus());
    setValue(getMaxPossibleValue());
    assertNull(getErrorStatus());
  }

  @Test
  public void testErrorMessageValueTooLarge() {
    setMaxValue(BigInteger.valueOf(100));

    setValue(BigInteger.valueOf(100));
    assertNull(getErrorStatus());
    setValue(BigInteger.valueOf(101));
    assertNotNull(getErrorStatus());

    assertEquals(TEXTS.get("NumberTooLargeMessageX", formatValueInternal(getMaxValue())), getErrorStatus().getMessage());

    setMinValue(BigInteger.valueOf(10));

    setValue(BigInteger.valueOf(20));
    assertNull(getErrorStatus());
    setValue(BigInteger.valueOf(101));
    assertEquals(TEXTS.get("NumberTooLargeMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  @Test
  public void testErrorMessageValueTooSmall() {
    setMinValue(BigInteger.valueOf(100));

    setValue(BigInteger.valueOf(100));
    assertNull(getErrorStatus());
    setValue(BigInteger.valueOf(99));
    assertNotNull(getErrorStatus());
    assertEquals(TEXTS.get("NumberTooSmallMessageX", formatValueInternal(getMinValue())), getErrorStatus().getMessage());

    setMaxValue(BigInteger.valueOf(200));

    setValue(BigInteger.valueOf(150));
    assertNull(getErrorStatus());
    setValue(BigInteger.valueOf(50));
    assertEquals(TEXTS.get("NumberTooSmallMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  private String formatWithFractionDigits(Number number, int fractionDigits) {
    m_formatter.setMinimumFractionDigits(fractionDigits);
    m_formatter.setMaximumFractionDigits(fractionDigits);
    return m_formatter.format(number);
  }

}
