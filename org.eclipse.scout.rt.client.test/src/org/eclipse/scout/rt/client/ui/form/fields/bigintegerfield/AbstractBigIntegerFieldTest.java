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
package org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
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
  public void testParseValueInternalNull() throws ProcessingException {
    assertEquals("expected null return for null input", null, parseValueInternal(null));
  }

  @Test
  public void testParseValueInternalInRange() throws ProcessingException {
    assertEquals("parsing failed", BigInteger.valueOf(42), parseValueInternal("42"));
    assertEquals("parsing failed", BigInteger.valueOf(-42), parseValueInternal("-42"));
    assertEquals("parsing failed", BigInteger.valueOf(0), parseValueInternal("0"));
  }

  @Test
  public void testParseValueInternalMaxMin() throws ProcessingException {
    // expect default for maxValue=999999999999999999999999999999999999999999999999999999999999 and minValue=-999999999999999999999999999999999999999999999999999999999999
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too big number.", this, "98765432109876543210987654321098765432109876543210987654321098765432109876543210");
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too small number.", this, "-98765432109876543210987654321098765432109876543210987654321098765432109876543210");

    setMaxValue(new BigInteger("99999999999999999999999999999999999999999999999999999999999999999999999999999999"));
    setMinValue(new BigInteger("-99999999999999999999999999999999999999999999999999999999999999999999999999999999"));
    assertEquals("parsing failed", new BigInteger("98765432109876543210987654321098765432109876543210987654321098765432109876543210"), parseValueInternal("98765432109876543210987654321098765432109876543210987654321098765432109876543210"));
    assertEquals("parsing failed", new BigInteger("-98765432109876543210987654321098765432109876543210987654321098765432109876543210"), parseValueInternal("-98765432109876543210987654321098765432109876543210987654321098765432109876543210"));

    setMaxValue(new BigInteger("99"));
    setMinValue(new BigInteger("-99"));
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too big number.", this, "100");
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too small number.", this, "-100");
    assertEquals("parsing failed", BigInteger.valueOf(99), parseValueInternal("99"));
    assertEquals("parsing failed", BigInteger.valueOf(-99), parseValueInternal("-99"));
  }

  @Test
  public void testParseValueInternalNotANumber() throws ProcessingException {
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string not representing a number.", this, "onethousend");
  }

  @Test
  public void testParseValueInternalDecimal() throws ProcessingException {
    // expecting RoundingMode.UNNECESSARY as default
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a decimal value.", this, formatWithFractionDigits(12.1, 1));
    Assert.assertEquals("parsing failed", BigInteger.valueOf(12), parseValueInternal(formatWithFractionDigits(12.0, 1)));

    setRoundingMode(RoundingMode.HALF_UP);
    Assert.assertEquals("parsing failed", BigInteger.valueOf(12), parseValueInternal(formatWithFractionDigits(12.0, 1)));
    Assert.assertEquals("parsing failed", BigInteger.valueOf(12), parseValueInternal(formatWithFractionDigits(12.1, 1)));
    Assert.assertEquals("parsing failed", BigInteger.valueOf(13), parseValueInternal(formatWithFractionDigits(12.5, 1)));

    setMaxValue(new BigInteger("99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"));
    setMinValue(new BigInteger("-99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"));
    Assert.assertEquals("parsing failed", new BigInteger("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807"),
        parseValueInternal("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807" + m_decimalSeparator + "40007"));
    Assert.assertEquals("parsing failed", new BigInteger("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775808"),
        parseValueInternal("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807" + m_decimalSeparator + "5"));
    Assert.assertEquals("parsing failed", new BigInteger("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775808"),
        parseValueInternal("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807" + m_decimalSeparator + "92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775808"));

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

    assertEquals(ScoutTexts.get("NumberTooLargeMessageX", formatValueInternal(getMaxValue())), getErrorStatus().getMessage());

    setMinValue(BigInteger.valueOf(10));

    setValue(BigInteger.valueOf(20));
    assertNull(getErrorStatus());
    setValue(BigInteger.valueOf(101));
    assertEquals(ScoutTexts.get("NumberTooLargeMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  @Test
  public void testErrorMessageValueTooSmall() {
    setMinValue(BigInteger.valueOf(100));

    setValue(BigInteger.valueOf(100));
    assertNull(getErrorStatus());
    setValue(BigInteger.valueOf(99));
    assertNotNull(getErrorStatus());
    assertEquals(ScoutTexts.get("NumberTooSmallMessageX", formatValueInternal(getMinValue())), getErrorStatus().getMessage());

    setMaxValue(BigInteger.valueOf(200));

    setValue(BigInteger.valueOf(150));
    assertNull(getErrorStatus());
    setValue(BigInteger.valueOf(50));
    assertEquals(ScoutTexts.get("NumberTooSmallMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  private String formatWithFractionDigits(Number number, int fractionDigits) {
    m_formatter.setMinimumFractionDigits(fractionDigits);
    m_formatter.setMaximumFractionDigits(fractionDigits);
    return m_formatter.format(number);
  }

}
