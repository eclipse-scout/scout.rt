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
package org.eclipse.scout.rt.client.ui.form.fields.integerfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractIntegerFieldTest extends AbstractIntegerField {

  private NumberFormat m_formatter;

  @Before
  public void setup() {
    m_formatter = DecimalFormat.getInstance(Locale.TRADITIONAL_CHINESE);
  }

  @Override
  protected void initFormat() {
    DecimalFormat format = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.TRADITIONAL_CHINESE);
    format.setParseBigDecimal(true);
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(0);
    format.setMaximumIntegerDigits(getConfiguredMaxIntegerDigits());
    propertySupport.setProperty(INumberValueContainer.PROP_DECIMAL_FORMAT, format);
  }

  @Test
  public void testParseValueInternalNull() {
    assertEquals("expected null return for null input", null, parseValueInternal(null));
  }

  @Test
  public void testParseValue() {
    // maxValue and minValue must not have an influence for parsing
    setMaxValue(99);
    setMinValue(-99);

    assertEquals("parsing failed", Integer.valueOf(0), parseValueInternal("0"));
    assertEquals("parsing failed", Integer.valueOf(42), parseValueInternal("42"));
    assertEquals("parsing failed", Integer.valueOf(-42), parseValueInternal("-42"));
    assertEquals("parsing failed", Integer.valueOf(101), parseValueInternal("101"));
    assertEquals("parsing failed", Integer.valueOf(-101), parseValueInternal("-101"));
  }

  @Test
  public void testParseValueInternalAroundIntegerMinMaxValue() {
    assertEquals("parsing failed", Integer.valueOf(Integer.MAX_VALUE), parseValueInternal(BigDecimal.valueOf(Integer.MAX_VALUE).toPlainString()));
    assertEquals("parsing failed", Integer.valueOf(Integer.MIN_VALUE), parseValueInternal(BigDecimal.valueOf(Integer.MIN_VALUE).toPlainString()));
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
    NlsLocale.set(Locale.TRADITIONAL_CHINESE);
    try {
      parseValueInternal(formatWithFractionDigits(12.1, 1));
      fail("Expected an exception when parsing a string representing a decimal value.");
    }
    catch (RuntimeException expected) {
    }
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
