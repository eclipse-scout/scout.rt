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
package org.eclipse.scout.rt.client.ui.form.fields.doublefield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class AbstractDoubleFieldTest extends AbstractDoubleField {

  @Test
  public void testParseValueInternalNull() throws ProcessingException {
    assertEquals("expected null return for null input", null, parseValueInternal(null));
  }

  @Test
  public void testParseValueInternalInRange() throws ProcessingException {
    setFractionDigits(50);
    assertEquals("parsing failed", Double.valueOf(42), parseValueInternal("42"));
    assertEquals("parsing failed", Double.valueOf(-42), parseValueInternal("-42"));
    assertEquals("parsing failed", Double.valueOf(0), parseValueInternal("0"));

    assertEquals("parsing failed", Double.valueOf(42.8592), parseValueInternal("42.8592"));
    assertEquals("parsing failed", Double.valueOf(-42.77234), parseValueInternal("-42.77234"));
    assertEquals("parsing failed", Double.valueOf(0), parseValueInternal("0.00000"));

    // very large and very small values should be parsed with loss of precision but without exception
    assertTrue(new BigDecimal("90000000000000000000000000000000000000000000000000000000000000000000000000000000").compareTo(
        NumberUtility.toBigDecimal(parseValueInternal("98765432109876543210987654321098765432109876543210987654321098765432109876543210"))) < 0);
    assertTrue(new BigDecimal("-90000000000000000000000000000000000000000000000000000000000000000000000000000000").compareTo(
        NumberUtility.toBigDecimal(parseValueInternal("-98765432109876543210987654321098765432109876543210987654321098765432109876543210"))) > 0);

  }

  @Test
  public void testSetMaxAndMinValueNull() {
    assertEquals("expect default for maxValue=Double.MAX_VALUE", Double.valueOf(Double.MAX_VALUE), getMaxValue());
    assertEquals("expect default for minValue=-Double.MAX_VALUE", Double.valueOf(-Double.MAX_VALUE), getMinValue());

    setMaxValue(99d);
    setMinValue(-99d);
    assertEquals("maxValue not as set above", Double.valueOf(99d), getMaxValue());
    assertEquals("minValue not as set above", Double.valueOf(-99d), getMinValue());

    setMaxValue(null);
    setMinValue(null);
    assertEquals("expected maxValue=Double.MAX_VALUE after calling setter with null-param", Double.valueOf(Double.MAX_VALUE), getMaxValue());
    assertEquals("expected minValue=-Double.MAX_VALUE after calling setter with null-param", Double.valueOf(-Double.MAX_VALUE), getMinValue());
  }

  @Test
  public void testParseValueInternalMaxMin() throws ProcessingException {
    // expect default for maxValue=Double.MAX_VALUE and minValue=-Double.MAX_VALUE
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too big number.", this, BigDecimal.valueOf(Double.MAX_VALUE).toPlainString() + "0");
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too small number.", this, BigDecimal.valueOf(-Double.MAX_VALUE).toPlainString() + "0");
    assertEquals("parsing failed", Double.valueOf(Double.MAX_VALUE), parseValueInternal(BigDecimal.valueOf(Double.MAX_VALUE).toPlainString()));
    assertEquals("parsing failed", Double.valueOf(-Double.MAX_VALUE), parseValueInternal(BigDecimal.valueOf(-Double.MAX_VALUE).toPlainString()));

    setMaxValue(99d);
    setMinValue(-99d);
    setFractionDigits(2);
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too big number.", this, "99.04");
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too small number.", this, "-99.9");
    assertEquals("parsing failed", Double.valueOf(99), parseValueInternal("99"));
    assertEquals("parsing failed", Double.valueOf(-99), parseValueInternal("-99"));
  }

  @Test
  public void testParseValueInternalPercent() throws ProcessingException {
    boolean exceptionOccured = false;
    try {
      parseValueInternal("59.88%");
    }
    catch (ProcessingException e) {
      exceptionOccured = true;
    }
    assertTrue("Expected an exception when parsing a string containing '%' and property 'percent' is not set to 'true'.", exceptionOccured);

    setPercent(true);
    assertEquals("parsing failed", Double.valueOf(59.88d), parseValueInternal("59.88%"));
    assertEquals("parsing failed", Double.valueOf(59.88d), parseValueInternal("59.88"));

    setMultiplier(100);
    assertEquals("parsing failed", Double.valueOf(0.5988d), parseValueInternal("59.88%"));
    assertEquals("parsing failed", Double.valueOf(0.5988d), parseValueInternal("59.88"));

  }

  @Test
  public void testParseValueInternalRounding() throws ProcessingException {
    // expecting RoundingMode.HALF_EVEN as default
    LocaleThreadLocal.get();
    setFractionDigits(1);
    assertEquals("parsing failed", Double.valueOf(123.5d), parseValueInternal("123.457"));
    assertEquals("parsing failed", Double.valueOf(12.6d), parseValueInternal("12.55"));
    assertEquals("parsing failed", Double.valueOf(12.6d), parseValueInternal("12.65"));

    setFractionDigits(3);
    assertEquals("parsing failed", Double.valueOf(123.456d), parseValueInternal("123.456"));
    assertEquals("parsing failed", Double.valueOf(123.456d), parseValueInternal("123.45645688"));
    assertEquals("parsing failed", Double.valueOf(123.457d), parseValueInternal("123.4565000007"));

    setFractionDigits(0);
    assertEquals("parsing failed", Double.valueOf(123d), parseValueInternal("123"));
    assertEquals("parsing failed", Double.valueOf(123d), parseValueInternal("123.456"));
    assertEquals("parsing failed", Double.valueOf(124d), parseValueInternal("123.75645688"));

    setRoundingMode(RoundingMode.HALF_UP);
    setFractionDigits(1);
    assertEquals("parsing failed", Double.valueOf(12.6d), parseValueInternal("12.55"));
    assertEquals("parsing failed", Double.valueOf(12.7d), parseValueInternal("12.65"));
  }

  @Test
  public void testParseValueInternalRoundingModeUnnecessary() throws ProcessingException {
    setRoundingMode(RoundingMode.UNNECESSARY);
    setFractionDigits(0);

    boolean exceptionOccured = false;
    try {
      parseValueInternal("12.1");
    }
    catch (ProcessingException e) {
      exceptionOccured = true;
    }
    Assert.assertTrue("Expected an exception when parsing a string representing a number with too many fraction digits.", exceptionOccured);
    Assert.assertEquals("parsing failed", Double.valueOf(12), parseValueInternal("12.0"));
    Assert.assertEquals("parsing failed", Double.valueOf(12), parseValueInternal("12"));

    setFractionDigits(5);
    exceptionOccured = false;
    try {
      parseValueInternal("12.123456");
    }
    catch (ProcessingException e) {
      exceptionOccured = true;
    }
    Assert.assertTrue("Expected an exception when parsing a string representing a number with too many fraction digits.", exceptionOccured);
    Assert.assertEquals("parsing failed", Double.valueOf(12), parseValueInternal("12.0"));
    Assert.assertEquals("parsing failed", Double.valueOf(12.1), parseValueInternal("12.1"));
    Assert.assertEquals("parsing failed", Double.valueOf(12.12345), parseValueInternal("12.12345"));
  }

  @Test
  public void testParseValueInternalMultiplier() throws ProcessingException {
    setFractionDigits(0);
    setMultiplier(10);
    assertEquals("parsing failed", Double.valueOf(5.9d), parseValueInternal("59"));

    setFractionDigits(2);
    setMultiplier(1);
    assertEquals("parsing failed", Double.valueOf(59.88d), parseValueInternal("59.88"));

    setMultiplier(10);
    assertEquals("parsing failed", Double.valueOf(5.988d), parseValueInternal("59.88"));

    setMultiplier(100);
    assertEquals("parsing failed", Double.valueOf(0.5988d), parseValueInternal("59.88"));

    setMultiplier(1000);
    assertEquals("parsing failed", Double.valueOf(0.05988d), parseValueInternal("59.88"));

    setMultiplier(1000000000);
    assertEquals("parsing failed", Double.valueOf(0.00000005988d), parseValueInternal("59.88"));

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

}
