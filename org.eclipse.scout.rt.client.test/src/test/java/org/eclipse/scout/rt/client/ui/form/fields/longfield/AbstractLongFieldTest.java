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
package org.eclipse.scout.rt.client.ui.form.fields.longfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractLongFieldTest extends AbstractLongField {

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
  public void testParseValueInternalInRange() {
    assertEquals("parsing failed", Long.valueOf(42), parseValueInternal("42"));
    assertEquals("parsing failed", Long.valueOf(-42), parseValueInternal("-42"));
    assertEquals("parsing failed", Long.valueOf(0), parseValueInternal("0"));

    assertEquals("parsing failed", Long.valueOf(Long.MAX_VALUE), parseValueInternal(Long.toString(Long.MAX_VALUE)));
    assertEquals("parsing failed", Long.valueOf(Long.MIN_VALUE), parseValueInternal(Long.toString(Long.MIN_VALUE)));
  }

  @Test
  public void testSetMaxAndMinValueNull() {
    assertEquals("expect default for maxValue=Long.MAX_VALUE", Long.valueOf(Long.MAX_VALUE), getMaxValue());
    assertEquals("expect default for minValue=Long.MIN_VALUE", Long.valueOf(Long.MIN_VALUE), getMinValue());

    setMaxValue(99L);
    setMinValue(-99L);
    assertEquals("maxValue not as set above", Long.valueOf(99), getMaxValue());
    assertEquals("minValue not as set above", Long.valueOf(-99), getMinValue());

    setMaxValue(null);
    setMinValue(null);
    assertEquals("expected maxValue=Long.MAX_VALUE after calling setter with null-param", Long.valueOf(Long.MAX_VALUE), getMaxValue());
    assertEquals("expected minValue=Long.MIN_VALUE after calling setter with null-param", Long.valueOf(Long.MIN_VALUE), getMinValue());
  }

  public void testParseValueInternalAroundLongMinMaxValue() {
    assertEquals("parsing failed", Long.valueOf(Integer.MAX_VALUE), parseValueInternal(BigDecimal.valueOf(Long.MAX_VALUE).toPlainString()));
    assertEquals("parsing failed", Long.valueOf(Integer.MIN_VALUE), parseValueInternal(BigDecimal.valueOf(Long.MIN_VALUE).toPlainString()));
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsRuntimeException("Expected an exception when parsing a string representing a too big number.", this,
        BigDecimal.valueOf(Integer.MAX_VALUE).add(BigDecimal.ONE).toPlainString());
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsRuntimeException("Expected an exception when parsing a string representing a too small number.", this,
        BigDecimal.valueOf(Integer.MIN_VALUE).subtract(BigDecimal.ONE).toPlainString());
  }

  @Test
  public void testParseValueInternalNotANumber() {
    try {
      parseValueInternal("onethousend");
      fail("Expected an exception when parsing a string not representing a number.");
    }
    catch (RuntimeException expected) {
    }
  }

  @Test
  public void testParseValueInternalDecimal() {
    // expecting RoundingMode.UNNECESSARY as default
    try {
      parseValueInternal(formatWithFractionDigits(12.7, 1));
      fail("Expected an exception when parsing a string representing a decimal value.");
    }
    catch (RuntimeException expected) {
    }
    Assert.assertEquals("parsing failed", Long.valueOf(12), parseValueInternal(formatWithFractionDigits(12.0, 1)));

    setRoundingMode(RoundingMode.HALF_UP);
    Assert.assertEquals("parsing failed", Long.valueOf(12), parseValueInternal(formatWithFractionDigits(12.0, 1)));
    Assert.assertEquals("parsing failed", Long.valueOf(12), parseValueInternal(formatWithFractionDigits(12.1, 1)));
    Assert.assertEquals("parsing failed", Long.valueOf(13), parseValueInternal(formatWithFractionDigits(12.5, 1)));

    Assert.assertEquals("parsing failed", Long.valueOf(Long.MAX_VALUE), parseValueInternal("9223372036854775807" + m_decimalSeparator + "40007"));
    Assert.assertEquals("parsing failed", Long.valueOf(Long.MAX_VALUE), parseValueInternal("9223372036854775806" + m_decimalSeparator + "5"));

    setRoundingMode(RoundingMode.HALF_EVEN);
    Assert.assertEquals("parsing failed", Long.valueOf(12), parseValueInternal(formatWithFractionDigits(12.5, 1)));
  }

  @Test
  public void testNoErrorMessage() {
    setValue(Long.valueOf(5));
    assertNull(getErrorStatus());
    setValue(getMinPossibleValue());
    assertNull(getErrorStatus());
    setValue(getMaxPossibleValue());
    assertNull(getErrorStatus());
  }

  @Test
  public void testErrorMessageValueTooLarge() {
    setMaxValue(Long.valueOf(100));

    setValue(Long.valueOf(100));
    assertNull(getErrorStatus());
    setValue(Long.valueOf(101));
    assertNotNull(getErrorStatus());

    assertEquals(ScoutTexts.get("NumberTooLargeMessageX", formatValueInternal(getMaxValue())), getErrorStatus().getMessage());

    setMinValue(Long.valueOf(10));

    setValue(Long.valueOf(20));
    assertNull(getErrorStatus());
    setValue(Long.valueOf(101));
    assertEquals(ScoutTexts.get("NumberTooLargeMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  @Test
  public void testErrorMessageValueTooSmall() {
    setMinValue(Long.valueOf(100));

    setValue(Long.valueOf(100));
    assertNull(getErrorStatus());
    setValue(Long.valueOf(99));
    assertNotNull(getErrorStatus());
    assertEquals(ScoutTexts.get("NumberTooSmallMessageX", formatValueInternal(getMinValue())), getErrorStatus().getMessage());

    setMaxValue(Long.valueOf(200));

    setValue(Long.valueOf(150));
    assertNull(getErrorStatus());
    setValue(Long.valueOf(50));
    assertEquals(ScoutTexts.get("NumberTooSmallMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  private String formatWithFractionDigits(Number number, int fractionDigits) {
    m_formatter.setMinimumFractionDigits(fractionDigits);
    m_formatter.setMaximumFractionDigits(fractionDigits);
    return m_formatter.format(number);
  }
}
