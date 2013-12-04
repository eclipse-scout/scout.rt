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
package org.eclipse.scout.rt.client.ui.form.fields.numberfield;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractNumberFieldTest extends AbstractNumberField<BigDecimal> {

  @Before
  public void setup() {
    LocaleThreadLocal.set(new Locale("de", "CH"));
  }

  @Override
  protected BigDecimal getConfiguredMinValue() {
    return null;
  }

  @Override
  protected BigDecimal getConfiguredMaxValue() {
    return null;
  }

  @Override
  protected BigDecimal parseValueInternal(String text) throws ProcessingException {
    return parseToBigDecimalInternal(text);
  }

  public static void assertParseToBigDecimalInternalThrowsProcessingException(String msg, AbstractNumberField<?> field, String textValue) {
    boolean exceptionOccured = false;
    try {
      field.parseToBigDecimalInternal(textValue);
    }
    catch (ProcessingException e) {
      exceptionOccured = true;
    }
    assertTrue(msg, exceptionOccured);
  }

  @Test
  public void testFormatValueInternal() {
    setRoundingMode(RoundingMode.HALF_EVEN);
    Assert.assertEquals("12", formatValueInternal(BigDecimal.valueOf(12.5)));
    setRoundingMode(RoundingMode.HALF_UP);
    Assert.assertEquals("13", formatValueInternal(BigDecimal.valueOf(12.5)));

    char groupingSeparator = new DecimalFormatSymbols(LocaleThreadLocal.get()).getGroupingSeparator();
    Assert.assertEquals("123" + groupingSeparator + "456" + groupingSeparator + "789", formatValueInternal(BigDecimal.valueOf(123456789)));

    setGroupingUsed(false);
    Assert.assertEquals("123456789", formatValueInternal(BigDecimal.valueOf(123456789)));

    setGroupingUsed(true);
    Assert.assertEquals("123" + groupingSeparator + "456" + groupingSeparator + "789", formatValueInternal(BigDecimal.valueOf(123456789)));
  }

  @Test
  public void testParseValueSuffix() throws ProcessingException {
    DecimalFormat df = (DecimalFormat) DecimalFormat.getNumberInstance(LocaleThreadLocal.get());
    df.applyPattern("#,##0.00 SUF");
    setFormat(df);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999 SUF"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999      SUF"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999SUF"));

    df.applyPattern("#,##0.00");
    setFormat(df);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999"));
    assertParseToBigDecimalInternalThrowsProcessingException("After setting a pattern without suffix an excpetion is expected when parsing text with suffix.", this, "9999 SUF");

    getFormatInternal().setPositiveSuffix(" SUF");
    getFormatInternal().setNegativeSuffix(" SUF");
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999 SUF"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999      SUF"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999SUF"));

    getFormatInternal().setPositiveSuffix("");
    getFormatInternal().setNegativeSuffix("");
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999"));
    assertParseToBigDecimalInternalThrowsProcessingException("After setting an empty suffix an excpetion is expected when parsing text with suffix.", this, "9999 SUF");
  }

  @Test
  public void testDecimalFormatHandling() {
    DecimalFormat format = getFormat();
    assertTrue("expected groupingUsed-property set to true as default", format.isGroupingUsed());
    assertTrue("expected groupingUsed-property set to true as default", isGroupingUsed());

    format.setGroupingUsed(false);
    setFormat(format);
    format = getFormat();
    assertFalse("expected groupingUsed-property set to false after setting format", format.isGroupingUsed());
    assertFalse("expected groupingUsed-property set to false after setting format", isGroupingUsed());

    setGroupingUsed(true);
    format = getFormat();
    assertTrue("expected groupingUsed-property set to true after using convenience setter", format.isGroupingUsed());
    assertTrue("expected groupingUsed-property set to true after using convenience setter", isGroupingUsed());
  }

}
