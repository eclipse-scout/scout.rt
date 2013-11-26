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

import java.math.BigInteger;
import java.math.RoundingMode;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.junit.Assert;
import org.junit.Test;

public class AbstractBigIntegerFieldTest extends AbstractBigIntegerField {

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
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string representing a decimal value.", this, "12.1");
    Assert.assertEquals("parsing failed", BigInteger.valueOf(12), parseValueInternal("12.0"));

    setRoundingMode(RoundingMode.HALF_UP);
    Assert.assertEquals("parsing failed", BigInteger.valueOf(12), parseValueInternal("12.0"));
    Assert.assertEquals("parsing failed", BigInteger.valueOf(12), parseValueInternal("12.1"));
    Assert.assertEquals("parsing failed", BigInteger.valueOf(13), parseValueInternal("12.5"));

    setMaxValue(new BigInteger("99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"));
    setMinValue(new BigInteger("-99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"));
    Assert.assertEquals("parsing failed", new BigInteger("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807"),
        parseValueInternal("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807.40007"));
    Assert.assertEquals("parsing failed", new BigInteger("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775808"),
        parseValueInternal("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807.5"));
    Assert.assertEquals("parsing failed", new BigInteger("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775808"),
        parseValueInternal("92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775807.92233720368547758079223372036854775807922337203685477580792233720368547758079223372036854775808"));

    setRoundingMode(RoundingMode.HALF_EVEN);
    Assert.assertEquals("parsing failed", BigInteger.valueOf(12), parseValueInternal("12.5"));
  }

}
