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
package org.eclipse.scout.rt.client.ui.form.fields.integerfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(ScoutClientTestRunner.class)
public class AbstractIntegerFieldTest extends AbstractIntegerField {

  private NumberFormat m_formatter;

  @Before
  public void setUp() {
    m_formatter = DecimalFormat.getInstance();
  }

  @Test
  public void testParseValueInternalNull() throws ProcessingException {
    assertEquals("expected null return for null input", null, parseValueInternal(null));
  }

  @Test
  public void testParseValueInternalInRange() throws ProcessingException {
    assertEquals("parsing failed", Integer.valueOf(42), parseValueInternal("42"));
    assertEquals("parsing failed", Integer.valueOf(-42), parseValueInternal("-42"));
    assertEquals("parsing failed", Integer.valueOf(0), parseValueInternal("0"));

    assertEquals("parsing failed", Integer.valueOf(Integer.MAX_VALUE), parseValueInternal(Integer.toString(Integer.MAX_VALUE)));
    assertEquals("parsing failed", Integer.valueOf(Integer.MIN_VALUE), parseValueInternal(Integer.toString(Integer.MIN_VALUE)));

  }

  @Test
  public void testSetMaxAndMinValueNull() {
    assertEquals("expect default for maxValue=Integer.MAX_VALUE", Integer.valueOf(Integer.MAX_VALUE), getMaxValue());
    assertEquals("expect default for minValue=Integer.MIN_VALUE", Integer.valueOf(Integer.MIN_VALUE), getMinValue());

    setMaxValue(99);
    setMinValue(-99);
    assertEquals("maxValue not as set above", Integer.valueOf(99), getMaxValue());
    assertEquals("minValue not as set above", Integer.valueOf(-99), getMinValue());

    setMaxValue(null);
    setMinValue(null);
    assertEquals("expected maxValue=Integer.MAX_VALUE after calling setter with null-param", Integer.valueOf(Integer.MAX_VALUE), getMaxValue());
    assertEquals("expected minValue=Integer.MIN_VALUE after calling setter with null-param", Integer.valueOf(Integer.MIN_VALUE), getMinValue());
  }

  @Test
  public void testParseValueInternalMaxMin() throws ProcessingException {
    // expect default for maxValue=Integer.MAX_VALUE and minValue=Integer.MIN_VALUE
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too big number.", this, BigDecimal.valueOf(Integer.MAX_VALUE).add(BigDecimal.ONE).toPlainString());
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too small number.", this, BigDecimal.valueOf(Integer.MIN_VALUE).subtract(BigDecimal.ONE).toPlainString());
    assertEquals("parsing failed", Integer.valueOf(Integer.MAX_VALUE), parseValueInternal(BigDecimal.valueOf(Integer.MAX_VALUE).toPlainString()));
    assertEquals("parsing failed", Integer.valueOf(Integer.MIN_VALUE), parseValueInternal(BigDecimal.valueOf(Integer.MIN_VALUE).toPlainString()));

    setMaxValue(99);
    setMinValue(-99);
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too big number.", this, "100");
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too small number.", this, "-100");
    assertEquals("parsing failed", Integer.valueOf(99), parseValueInternal("99"));
    assertEquals("parsing failed", Integer.valueOf(-99), parseValueInternal("-99"));
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
  public void testParseValueInternalDecimal() throws ProcessingException {
    // expecting RoundingMode.UNNECESSARY as default
    boolean exceptionOccured = false;
    try {
      parseValueInternal(formatWithFractionDigits(12.1, 1));
    }
    catch (ProcessingException e) {
      exceptionOccured = true;
    }
    assertTrue("Expected an exception when parsing a string representing a decimal value.", exceptionOccured);
    assertEquals("parsing failed", Integer.valueOf(12), parseValueInternal(formatWithFractionDigits(12.0, 1)));

    setRoundingMode(RoundingMode.HALF_UP);
    assertEquals("parsing failed", Integer.valueOf(12), parseValueInternal(formatWithFractionDigits(12.0, 1)));
    assertEquals("parsing failed", Integer.valueOf(12), parseValueInternal(formatWithFractionDigits(12.1, 1)));
    assertEquals("parsing failed", Integer.valueOf(13), parseValueInternal(formatWithFractionDigits(12.5, 1)));

    assertEquals("parsing failed", Integer.valueOf(Integer.MAX_VALUE), parseValueInternal(formatWithFractionDigits(2147483647.40007, 5)));
    assertEquals("parsing failed", Integer.valueOf(Integer.MAX_VALUE), parseValueInternal(formatWithFractionDigits(2147483646.5, 1)));

    setRoundingMode(RoundingMode.HALF_EVEN);
    assertEquals("parsing failed", Integer.valueOf(12), parseValueInternal(formatWithFractionDigits(12.5, 1)));
  }

  @Test
  public void testNoErrorMessage() {
    setValue(5);
    assertNull(getErrorStatus());
    setValue(Integer.MIN_VALUE);
    assertNull(getErrorStatus());
    setValue(Integer.MAX_VALUE);
    assertNull(getErrorStatus());
  }

  @Test
  public void testErrorMessageValueTooLarge() {
    setMaxValue(100);

    setValue(100);
    assertNull(getErrorStatus());
    setValue(101);
    assertNotNull(getErrorStatus());
    assertEquals(ScoutTexts.get("NumberTooLargeMessageX", formatValueInternal(getMaxValue())), getErrorStatus().getMessage());

    setMinValue(10);

    setValue(20);
    assertNull(getErrorStatus());
    setValue(101);
    assertEquals(ScoutTexts.get("NumberTooLargeMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  @Test
  public void testErrorMessageValueTooSmall() {
    setMinValue(100);

    setValue(100);
    assertNull(getErrorStatus());
    setValue(99);
    assertNotNull(getErrorStatus());
    assertEquals(ScoutTexts.get("NumberTooSmallMessageX", formatValueInternal(getMinValue())), getErrorStatus().getMessage());

    setMaxValue(200);

    setValue(150);
    assertNull(getErrorStatus());
    setValue(50);
    assertEquals(ScoutTexts.get("NumberTooSmallMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  private String formatWithFractionDigits(Number number, int fractionDigits) {
    m_formatter.setMinimumFractionDigits(fractionDigits);
    m_formatter.setMaximumFractionDigits(fractionDigits);
    return m_formatter.format(number);
  }
}
