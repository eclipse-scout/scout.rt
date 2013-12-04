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
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Locale;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.junit.Before;
import org.junit.Test;

public class AbstractBigDecimalFieldTest extends AbstractBigDecimalField {

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
    setFractionDigits(5);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(42), parseValueInternal("42"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-42), parseValueInternal("-42"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0), parseValueInternal("0"));

    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(42.8532), parseValueInternal("42.8532"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-42.77234), parseValueInternal("-42.77234"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0), parseValueInternal("0.00000"));

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
        parseValueInternal("98765432109876543210987654321098765432109876543210987654321098765432109876543210.98765432109876543210987654321098765432109876543210987654321098765432109876543210"));
    ScoutAssert.assertComparableEquals(new BigDecimal("-98765432109876543210987654321098765432109876543210987654321098765432109876543210.98765432109876543210987654321098765432109876543210987654321098765432109876543210"),
        parseValueInternal("-98765432109876543210987654321098765432109876543210987654321098765432109876543210.98765432109876543210987654321098765432109876543210987654321098765432109876543210"));

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
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88d), parseValueInternal("59.88"));

    setFractionDigits(2);
    setMultiplier(10);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(5.988d), parseValueInternal("59.88"));

    setFractionDigits(2);
    setMultiplier(100);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.5988d), parseValueInternal("59.88"));

    setFractionDigits(2);
    setMultiplier(1000);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.05988d), parseValueInternal("59.88"));

    setFractionDigits(2);
    setMultiplier(1000000000);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.00000005988d), parseValueInternal("59.88"));

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
