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
package org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutClientTestRunner.class)
public class AbstractBigDecimalFieldTest extends AbstractBigDecimalField {

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
    setFractionDigits(5);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(42), parseValueInternal("42"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-42), parseValueInternal("-42"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0), parseValueInternal("0"));

    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(42.8532), parseValueInternal(formatWithFractionDigits(42.8532, 4)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-42.77234), parseValueInternal(formatWithFractionDigits(-42.77234, 5)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0), parseValueInternal(formatWithFractionDigits(0.00000, 5)));

  }

  @Test
  public void testParseValueInternalMaxMin() throws ProcessingException {
    setFractionDigits(80);
    // expect default for maxValue=999999999999999999999999999999999999999999999999999999999999 and minValue=-999999999999999999999999999999999999999999999999999999999999
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too big number.", this, "98765432109876543210987654321098765432109876543210987654321098765432109876543210");
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too small number.", this, "-98765432109876543210987654321098765432109876543210987654321098765432109876543210");

    setMaxValue(new BigDecimal("99999999999999999999999999999999999999999999999999999999999999999999999999999999"));
    setMinValue(new BigDecimal("-99999999999999999999999999999999999999999999999999999999999999999999999999999999"));
    ScoutAssert.assertComparableEquals(new BigDecimal("98765432109876543210987654321098765432109876543210987654321098765432109876543210"),
        parseValueInternal("98765432109876543210987654321098765432109876543210987654321098765432109876543210"));
    ScoutAssert.assertComparableEquals(new BigDecimal("-98765432109876543210987654321098765432109876543210987654321098765432109876543210"),
        parseValueInternal("-98765432109876543210987654321098765432109876543210987654321098765432109876543210"));

    ScoutAssert.assertComparableEquals(new BigDecimal("98765432109876543210987654321098765432109876543210987654321098765432109876543210.98765432109876543210987654321098765432109876543210987654321098765432109876543210"),
        parseValueInternal("98765432109876543210987654321098765432109876543210987654321098765432109876543210" + m_decimalSeparator + "98765432109876543210987654321098765432109876543210987654321098765432109876543210"));
    ScoutAssert.assertComparableEquals(new BigDecimal("-98765432109876543210987654321098765432109876543210987654321098765432109876543210.98765432109876543210987654321098765432109876543210987654321098765432109876543210"),
        parseValueInternal("-98765432109876543210987654321098765432109876543210987654321098765432109876543210" + m_decimalSeparator + "98765432109876543210987654321098765432109876543210987654321098765432109876543210"));

    setMaxValue(new BigDecimal("99"));
    setMinValue(new BigDecimal("-99"));
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too big number.", this, "100");
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too small number.", this, "-100");
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(99), parseValueInternal("99"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-99), parseValueInternal("-99"));
  }

  @Test
  public void testParseValueInternalMultiplier() throws ProcessingException {

    setFractionDigits(0);
    setMultiplier(10);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(5.9d), parseValueInternal("59"));

    setFractionDigits(2);
    setMultiplier(1);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88d), parseValueInternal(formatWithFractionDigits(59.88, 2)));

    setFractionDigits(2);
    setMultiplier(10);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(5.988d), parseValueInternal(formatWithFractionDigits(59.88, 2)));

    setFractionDigits(2);
    setMultiplier(100);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.5988d), parseValueInternal(formatWithFractionDigits(59.88, 2)));

    setFractionDigits(2);
    setMultiplier(1000);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.05988d), parseValueInternal(formatWithFractionDigits(59.88, 2)));

    setFractionDigits(2);
    setMultiplier(1000000000);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.00000005988d), parseValueInternal(formatWithFractionDigits(59.88, 2)));

  }

  @Test
  public void testParseValueInternalNotANumber() throws ProcessingException {
    boolean exceptionOccured = false;
    try {
      parseValueInternal("onethousend");
    }
    catch (ProcessingException e) {
      exceptionOccured = true;
    }
    assertTrue("Expected an exception when parsing a string not representing a number.", exceptionOccured);
  }

  @Test
  public void testNoErrorMessage() {
    setValue(BigDecimal.valueOf(5));
    assertNull(getErrorStatus());
    setValue(getMinPossibleValue());
    assertNull(getErrorStatus());
    setValue(getMaxPossibleValue());
    assertNull(getErrorStatus());
  }

  @Test
  public void testErrorMessageValueTooLarge() {
    setMaxValue(BigDecimal.valueOf(100));

    setValue(BigDecimal.valueOf(100));
    assertNull(getErrorStatus());
    setValue(BigDecimal.valueOf(101));
    assertNotNull(getErrorStatus());

    assertEquals(ScoutTexts.get("NumberTooLargeMessageX", formatValueInternal(getMaxValue())), getErrorStatus().getMessage());

    setMinValue(BigDecimal.valueOf(10));

    setValue(BigDecimal.valueOf(20));
    assertNull(getErrorStatus());
    setValue(BigDecimal.valueOf(101));
    assertEquals(ScoutTexts.get("NumberTooLargeMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  @Test
  public void testErrorMessageValueTooSmall() {
    setMinValue(BigDecimal.valueOf(100));

    setValue(BigDecimal.valueOf(100));
    assertNull(getErrorStatus());
    setValue(BigDecimal.valueOf(99));
    assertNotNull(getErrorStatus());
    assertEquals(ScoutTexts.get("NumberTooSmallMessageX", formatValueInternal(getMinValue())), getErrorStatus().getMessage());

    setMaxValue(BigDecimal.valueOf(200));

    setValue(BigDecimal.valueOf(150));
    assertNull(getErrorStatus());
    setValue(BigDecimal.valueOf(50));
    assertEquals(ScoutTexts.get("NumberTooSmallMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  private String formatWithFractionDigits(Number number, int fractionDigits) {
    m_formatter.setMinimumFractionDigits(fractionDigits);
    m_formatter.setMaximumFractionDigits(fractionDigits);
    return m_formatter.format(number);
  }

}
