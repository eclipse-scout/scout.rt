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
package org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractBigDecimalFieldTest extends AbstractBigDecimalField {

  private NumberFormat m_formatter;

  @Before
  public void setUp() {
    m_formatter = DecimalFormat.getInstance();
  }

  @Test
  public void testParseValueInternalNull() {
    assertEquals("expected null return for null input", null, parseValueInternal(null));
  }

  @Test
  public void testParseValue() {
    setFractionDigits(5);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(42), parseValueInternal("42"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-42), parseValueInternal("-42"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0), parseValueInternal("0"));

    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(42.8532), parseValueInternal(formatWithFractionDigits(42.8532, 4)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-42.77234), parseValueInternal(formatWithFractionDigits(-42.77234, 5)));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0), parseValueInternal(formatWithFractionDigits(0.00000, 5)));

  }

  @Test
  public void testParseValueInternalAroundPossibleMinMaxValue() {

    assertEquals("parsing failed", getMaxPossibleValue(), parseValueInternal(getMaxPossibleValue().toPlainString()));
    assertEquals("parsing failed", getMinPossibleValue(), parseValueInternal(getMinPossibleValue().toPlainString()));
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsRuntimeException("Expected an exception when parsing a string representing a too big number.", this, getMaxPossibleValue().add(BigDecimal.ONE).toPlainString());
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsRuntimeException("Expected an exception when parsing a string representing a too small number.", this, getMinPossibleValue().subtract(BigDecimal.ONE).toPlainString());
  }

  @Test
  public void testParseValueInternalMultiplier() {

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
  public void testParseValueInternalNotANumber() {
    try {
      parseValueInternal("onethousend");
      fail("Expected an exception when parsing a string not representing a number.");
    }
    catch (RuntimeException expected) {
    }
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

    assertEquals(TEXTS.get("NumberTooLargeMessageX", formatValueInternal(getMaxValue())), getErrorStatus().getMessage());

    setMinValue(BigDecimal.valueOf(10));

    setValue(BigDecimal.valueOf(20));
    assertNull(getErrorStatus());
    setValue(BigDecimal.valueOf(101));
    assertEquals(TEXTS.get("NumberTooLargeMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  @Test
  public void testErrorMessageValueTooSmall() {
    setMinValue(BigDecimal.valueOf(100));

    setValue(BigDecimal.valueOf(100));
    assertNull(getErrorStatus());
    setValue(BigDecimal.valueOf(99));
    assertNotNull(getErrorStatus());
    assertEquals(TEXTS.get("NumberTooSmallMessageX", formatValueInternal(getMinValue())), getErrorStatus().getMessage());

    setMaxValue(BigDecimal.valueOf(200));

    setValue(BigDecimal.valueOf(150));
    assertNull(getErrorStatus());
    setValue(BigDecimal.valueOf(50));
    assertEquals(TEXTS.get("NumberTooSmallMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  private String formatWithFractionDigits(Number number, int fractionDigits) {
    m_formatter.setMinimumFractionDigits(fractionDigits);
    m_formatter.setMaximumFractionDigits(fractionDigits);
    return m_formatter.format(number);
  }

}
