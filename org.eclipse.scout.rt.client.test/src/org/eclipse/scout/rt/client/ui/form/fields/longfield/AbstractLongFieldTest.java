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
package org.eclipse.scout.rt.client.ui.form.fields.longfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractLongFieldTest extends AbstractLongField {

  @Before
  public void setup() {
    LocaleThreadLocal.set(new Locale("de", "CH"));
  }

  @Test
  public void testParseValueInternalNull() throws ProcessingException {
    assertEquals("expected null return for null input", null, parseValueInternal(null));
  }

  @Test
  public void testParseValueInternalInRange() throws ProcessingException {
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

    setMaxValue(99l);
    setMinValue(-99l);
    assertEquals("maxValue not as set above", Long.valueOf(99), getMaxValue());
    assertEquals("minValue not as set above", Long.valueOf(-99), getMinValue());

    setMaxValue(null);
    setMinValue(null);
    assertEquals("expected maxValue=Long.MAX_VALUE after calling setter with null-param", Long.valueOf(Long.MAX_VALUE), getMaxValue());
    assertEquals("expected minValue=Long.MIN_VALUE after calling setter with null-param", Long.valueOf(Long.MIN_VALUE), getMinValue());
  }

  @Test
  public void testParseValueInternalMaxMin() throws ProcessingException {
    // expect default for maxValue=Long.MAX_VALUE and minValue=Long.MIN_VALUE
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too big number.", this, BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE).toPlainString());
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too small number.", this, BigDecimal.valueOf(Long.MIN_VALUE).subtract(BigDecimal.ONE).toPlainString());
    assertEquals("parsing failed", Long.valueOf(Long.MAX_VALUE), parseValueInternal(BigDecimal.valueOf(Long.MAX_VALUE).toPlainString()));
    assertEquals("parsing failed", Long.valueOf(Long.MIN_VALUE), parseValueInternal(BigDecimal.valueOf(Long.MIN_VALUE).toPlainString()));

    setMaxValue(99l);
    setMinValue(-99l);
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too big number.", this, "100");
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a too small number.", this, "-100");
    assertEquals("parsing failed", Long.valueOf(99), parseValueInternal("99"));
    assertEquals("parsing failed", Long.valueOf(-99), parseValueInternal("-99"));
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
      parseValueInternal("12.7");
    }
    catch (ProcessingException e) {
      exceptionOccured = true;
    }
    assertTrue("Expected an exception when parsing a string representing a decimal value.", exceptionOccured);
    Assert.assertEquals("parsing failed", Long.valueOf(12), parseValueInternal("12.0"));

    setRoundingMode(RoundingMode.HALF_UP);
    Assert.assertEquals("parsing failed", Long.valueOf(12), parseValueInternal("12.0"));
    Assert.assertEquals("parsing failed", Long.valueOf(12), parseValueInternal("12.1"));
    Assert.assertEquals("parsing failed", Long.valueOf(13), parseValueInternal("12.5"));

    Assert.assertEquals("parsing failed", Long.valueOf(Long.MAX_VALUE), parseValueInternal("9223372036854775807.40007"));
    Assert.assertEquals("parsing failed", Long.valueOf(Long.MAX_VALUE), parseValueInternal("9223372036854775806.5"));

    setRoundingMode(RoundingMode.HALF_EVEN);
    Assert.assertEquals("parsing failed", Long.valueOf(12), parseValueInternal("12.5"));
  }
}
