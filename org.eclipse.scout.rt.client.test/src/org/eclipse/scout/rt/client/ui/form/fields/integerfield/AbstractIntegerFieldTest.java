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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class AbstractIntegerFieldTest extends AbstractIntegerField {

  private static Locale ORIGINAL_LOCALE;

  @BeforeClass
  public static void setupBeforeClass() {
    ORIGINAL_LOCALE = LocaleThreadLocal.get();
    LocaleThreadLocal.set(new Locale("de", "CH"));
  }

  @AfterClass
  public static void tearDownAfterClass() {
    LocaleThreadLocal.set(ORIGINAL_LOCALE);
  }

  @Test
  public void testParseValueInternalNull() throws ProcessingException {
    assertEquals("expected null return for null input", null, parseValueInternal(null));
  }

  @Test
  public void testParseValueInternalInRange() throws ProcessingException {
    Assert.assertEquals("parsing failed", Integer.valueOf(42), parseValueInternal("42"));
    Assert.assertEquals("parsing failed", Integer.valueOf(-42), parseValueInternal("-42"));
    Assert.assertEquals("parsing failed", Integer.valueOf(0), parseValueInternal("0"));

    Assert.assertEquals("parsing failed", Integer.valueOf(Integer.MAX_VALUE), parseValueInternal(Integer.toString(Integer.MAX_VALUE)));
    Assert.assertEquals("parsing failed", Integer.valueOf(Integer.MIN_VALUE), parseValueInternal(Integer.toString(Integer.MIN_VALUE)));

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
    Assert.assertTrue("Expected an exception when parsing a string not representing a number.", exceptionOccured);
  }

  @Test
  public void testParseValueInternalDecimal() throws ProcessingException {
    // expecting RoundingMode.UNNECESSARY as default
    boolean exceptionOccured = false;
    try {
      parseValueInternal("12.1");
    }
    catch (ProcessingException e) {
      exceptionOccured = true;
    }
    Assert.assertTrue("Expected an exception when parsing a string representing a decimal value.", exceptionOccured);
    Assert.assertEquals("parsing failed", Integer.valueOf(12), parseValueInternal("12.0"));

    setRoundingMode(RoundingMode.HALF_UP);
    Assert.assertEquals("parsing failed", Integer.valueOf(12), parseValueInternal("12.0"));
    Assert.assertEquals("parsing failed", Integer.valueOf(12), parseValueInternal("12.1"));
    Assert.assertEquals("parsing failed", Integer.valueOf(13), parseValueInternal("12.5"));

    Assert.assertEquals("parsing failed", Integer.valueOf(Integer.MAX_VALUE), parseValueInternal("2147483647.40007"));
    Assert.assertEquals("parsing failed", Integer.valueOf(Integer.MAX_VALUE), parseValueInternal("2147483646.5"));

    setRoundingMode(RoundingMode.HALF_EVEN);
    Assert.assertEquals("parsing failed", Integer.valueOf(12), parseValueInternal("12.5"));

  }

}
