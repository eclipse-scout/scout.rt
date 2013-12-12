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
package org.eclipse.scout.rt.client.ui.form.fields.decimalfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest.P_PropertyTracker;
import org.eclipse.scout.rt.client.ui.valuecontainer.IDecimalValueContainer;
import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class AbstractDecimalFieldTest extends AbstractDecimalField<BigDecimal> {

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

  @Override
  protected BigDecimal getConfiguredMinValue() {
    return null;
  }

  @Override
  protected BigDecimal getConfiguredMaxValue() {
    return null;
  }

  @Override
  protected RoundingMode getConfiguredRoundingMode() {
    return RoundingMode.HALF_EVEN;
  }

  @Override
  protected int getConfiguredFractionDigits() {
    return 5;
  }

  @Override
  protected BigDecimal parseValueInternal(String text) throws ProcessingException {
    return parseToBigDecimalInternal(text);
  }

  @Test
  public void testParseToBigDecimalInternal() throws ProcessingException {
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(42), parseToBigDecimalInternal("42"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-42), parseToBigDecimalInternal("-42"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0), parseToBigDecimalInternal("0"));

    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(42.8532), parseToBigDecimalInternal("42.8532"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(-42.77234), parseToBigDecimalInternal("-42.77234"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0), parseToBigDecimalInternal("0.00000"));
  }

  @Test
  public void testParseValueInternalPercent() throws ProcessingException {
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string containing '%' and property 'percent' is not set to 'true'.", this, "59.88%");

    setPercent(true);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88 %"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88%"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88"));

    setMultiplier(100);
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.5988), parseValueInternal("59.88 %"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.5988), parseValueInternal("59.88%"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(0.5988), parseValueInternal("59.88"));

  }

  @Test
  public void testFormatValueInternal() {
    setMaxFractionDigits(3);
    setRoundingMode(RoundingMode.HALF_EVEN);
    assertEquals("12.246", formatValueInternal(BigDecimal.valueOf(12.2465)));
    setRoundingMode(RoundingMode.HALF_UP);
    assertEquals("12.247", formatValueInternal(BigDecimal.valueOf(12.2465)));
    setRoundingMode(RoundingMode.UNNECESSARY);

    boolean exceptionOccured = false;
    try {
      formatValueInternal(BigDecimal.valueOf(12.2465));
    }
    catch (ArithmeticException e) {
      exceptionOccured = true;
    }
    assertTrue("Expected an ArithmeticException when formatting a value with more fraction digits than maxFractionDigits and RoundingMode.UNNECESSARY.", exceptionOccured);
  }

  @Test
  public void testSetPercent() throws ProcessingException {
    DecimalFormat dfPercent = (DecimalFormat) DecimalFormat.getPercentInstance(LocaleThreadLocal.get());

    // test default
    assertFalse(isPercent());
    assertEquals("", getFormatInternal().getPositiveSuffix());
    assertEquals("", getFormatInternal().getNegativeSuffix());
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string containing '%' and property 'percent' is not set to 'true'.", this, "59.88 %");
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88"));

    setPercent(true);
    assertTrue(isPercent());
    assertEquals(dfPercent.getPositiveSuffix(), getFormatInternal().getPositiveSuffix());
    assertEquals(dfPercent.getNegativeSuffix(), getFormatInternal().getNegativeSuffix());
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88 %"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88"));

    setPercent(false);
    assertFalse(isPercent());
    assertEquals("", getFormatInternal().getPositiveSuffix());
    assertEquals("", getFormatInternal().getNegativeSuffix());
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsProcessingException("Expected an exception when parsing a string containing '%' and property 'percent' is not set to 'true'.", this, "59.88 %");
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88"));

    // manually setting suffixes
    getFormatInternal().setPositiveSuffix(dfPercent.getPositiveSuffix());
    assertFalse(isPercent());
    getFormatInternal().setNegativeSuffix(dfPercent.getNegativeSuffix());
    assertTrue(isPercent());
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88 %"));
    ScoutAssert.assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal("59.88"));

  }

  @Test
  public void testSetMaxFractionDigitsSmallerThanMin() {
    setMinFractionDigits(42);
    assertEquals("expected minFractionDigits to be as set before", 42, getMinFractionDigits());

    setMaxFractionDigits(9);
    assertEquals("expected minFractionDigits to be adapted when setting maxFractionDigits to a smaller value", 9, getMinFractionDigits());
  }

  @Test
  public void testSetMinFractionDigitsBiggerThanMax() {
    setMaxFractionDigits(7);
    assertEquals("expected maxFractionDigits to be as set before", 7, getMaxFractionDigits());

    setMinFractionDigits(9);
    assertEquals("expected maxFractionDigits to be adapted when setting minFractionDigits to a bigger value", 9, getMinFractionDigits());
  }

  @Test
  public void testPropertySupportForMinFractionDigits() {
    setMinFractionDigits(2);
    setMaxFractionDigits(7);
    P_PropertyTracker formatTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_DECIMAL_FORMAT, formatTracker);

    setMinFractionDigits(2);
    assertFalse("expected tracker not to be notified, when new value is same as old value", formatTracker.m_notified);

    DecimalFormat oldFormat = getFormat();

    setMinFractionDigits(4);
    assertTrue("expected tracker to be notified, when value changed", formatTracker.m_notified);
    assertEquals("expected getter to return new setting", 4, getMinFractionDigits());
    assertEquals("expected new setting in property change notification", 4, ((DecimalFormat) formatTracker.m_cachedProperty).getMinimumFractionDigits());

    DecimalFormat newFormat = (DecimalFormat) ((DecimalFormat) formatTracker.m_cachedProperty).clone();
    newFormat.setMinimumFractionDigits(2);
    assertEquals("expected no other difference in new format", oldFormat, newFormat);
  }

  @Test
  public void testPropertySupportForMaxFractionDigits() {
    setMaxFractionDigits(2);
    P_PropertyTracker formatTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_DECIMAL_FORMAT, formatTracker);

    setMaxFractionDigits(2);
    assertFalse("expected tracker not to be notified, when new value is same as old value", formatTracker.m_notified);

    DecimalFormat oldFormat = getFormat();

    setMaxFractionDigits(4);
    assertTrue("expected tracker to be notified, when value changed", formatTracker.m_notified);
    assertEquals("expected getter to return new setting", 4, getMaxFractionDigits());
    assertEquals("expected new setting in property change notification", 4, ((DecimalFormat) formatTracker.m_cachedProperty).getMaximumFractionDigits());

    DecimalFormat newFormat = (DecimalFormat) ((DecimalFormat) formatTracker.m_cachedProperty).clone();
    newFormat.setMaximumFractionDigits(2);
    assertEquals("expected no other difference in new format", oldFormat, newFormat);
  }

  @Test
  public void testPropertySupportForMultiplier() {
    setMultiplier(1);
    P_PropertyTracker formatTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_DECIMAL_FORMAT, formatTracker);

    setMultiplier(1);
    assertFalse("expected tracker not to be notified, when new value is same as old value", formatTracker.m_notified);

    DecimalFormat oldFormat = getFormat();

    setMultiplier(100);
    assertTrue("expected tracker to be notified, when value changed", formatTracker.m_notified);
    assertEquals("expected getter to return new setting", 100, getMultiplier());
    assertEquals("expected new setting in property change notification", 100, ((DecimalFormat) formatTracker.m_cachedProperty).getMultiplier());

    DecimalFormat newFormat = (DecimalFormat) ((DecimalFormat) formatTracker.m_cachedProperty).clone();
    newFormat.setMultiplier(1);
    assertEquals("expected no other difference in new format", oldFormat, newFormat);
  }

  @Test
  public void testPropertySupportForPercent() {
    DecimalFormat dfPercent = (DecimalFormat) DecimalFormat.getPercentInstance(LocaleThreadLocal.get());
    setPercent(false);
    // Bug in DecimalFormat: private members for suffix patterns (not suffix itself) are "" as default,
    // but become null when empty suffixes are set. These private members are checked in equals(Object)
    DecimalFormat workaroundFormat = getFormat();
    workaroundFormat.setPositiveSuffix(workaroundFormat.getPositiveSuffix());
    workaroundFormat.setNegativeSuffix(workaroundFormat.getNegativeSuffix());
    setFormat(workaroundFormat);

    P_PropertyTracker formatTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_DECIMAL_FORMAT, formatTracker);

    setPercent(false);
    assertFalse("expected tracker not to be notified, when new value is same as old value", formatTracker.m_notified);

    DecimalFormat oldFormat = getFormat();

    setPercent(true);
    assertTrue("expected tracker to be notified, when value changed", formatTracker.m_notified);
    assertEquals("expected getter to return new setting", true, isPercent());
    assertEquals("expected new setting in property change notification", dfPercent.getPositiveSuffix(), ((DecimalFormat) formatTracker.m_cachedProperty).getPositiveSuffix());
    assertEquals("expected new setting in property change notification", dfPercent.getNegativeSuffix(), ((DecimalFormat) formatTracker.m_cachedProperty).getNegativeSuffix());

    DecimalFormat newFormat = (DecimalFormat) ((DecimalFormat) formatTracker.m_cachedProperty).clone();
    newFormat.setPositiveSuffix("");
    newFormat.setNegativeSuffix("");
    assertEquals("expected no other difference in new format", oldFormat, newFormat);
  }

  @Test
  public void testPropertySupportForParsingFractionDigits() {
    setFractionDigits(5);
    P_PropertyTracker propertyTracker = new P_PropertyTracker();
    addPropertyChangeListener(IDecimalValueContainer.PROP_PARSING_FRACTION_DIGITS, propertyTracker);

    setFractionDigits(5);
    assertFalse("expected tracker not to be notified, when new value is same as old value", propertyTracker.m_notified);

    setFractionDigits(6);
    assertTrue("expected tracker to be notified, when value changed", propertyTracker.m_notified);
    ScoutAssert.assertComparableEquals("expected getter to return new setting", 6, getFractionDigits());
    ScoutAssert.assertComparableEquals("expected new setting in property change notification", 6, (Integer) propertyTracker.m_cachedProperty);
  }

}
