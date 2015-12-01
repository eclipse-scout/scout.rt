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

import static org.eclipse.scout.rt.testing.commons.ScoutAssert.assertComparableEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest.P_PropertyTracker;
import org.eclipse.scout.rt.client.ui.valuecontainer.IDecimalValueContainer;
import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.TestingUtility.NumberStringPercentSuffix;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link AbstractDecimalField}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractDecimalFieldTest extends AbstractDecimalField<BigDecimal> {

  private static final BigDecimal DEFAULT_MIN_VALUE = new BigDecimal("-999999999999999999999999999999999999999999999999999999999999");
  private static final BigDecimal DEFAULT_MAX_VALUE = new BigDecimal("999999999999999999999999999999999999999999999999999999999999");
  private NumberFormat m_formatter;

  @Before
  public void setUp() {
    m_formatter = DecimalFormat.getInstance();
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
  protected BigDecimal getMinPossibleValue() {
    return DEFAULT_MIN_VALUE;
  }

  @Override
  protected BigDecimal getMaxPossibleValue() {
    return DEFAULT_MAX_VALUE;
  }

  @Override
  protected BigDecimal parseValueInternal(String text) {
    return parseToBigDecimalInternal(text);
  }

  @Test
  public void testParseToBigDecimalInternal() {
    for (Locale locale : DecimalFormat.getAvailableLocales()) {
      DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(locale);
      df.applyPattern(getFormat().toPattern());
      setFormat(df);
      assertComparableEquals(BigDecimal.valueOf(42), parseToBigDecimalInternal(TestingUtility.createLocaleSpecificNumberString(locale, false, "42")));
      assertComparableEquals(BigDecimal.valueOf(-42), parseToBigDecimalInternal(TestingUtility.createLocaleSpecificNumberString(locale, true, "42")));
      assertComparableEquals(BigDecimal.valueOf(0), parseToBigDecimalInternal(TestingUtility.createLocaleSpecificNumberString(locale, false, "0")));

      assertComparableEquals(BigDecimal.valueOf(42.8532), parseToBigDecimalInternal(TestingUtility.createLocaleSpecificNumberString(locale, false, "42", "8532")));
      assertComparableEquals(BigDecimal.valueOf(-42.77234), parseToBigDecimalInternal(TestingUtility.createLocaleSpecificNumberString(locale, true, "42", "77234")));
      assertComparableEquals(BigDecimal.valueOf(0), parseToBigDecimalInternal(TestingUtility.createLocaleSpecificNumberString(locale, false, "0", "00000")));
    }
  }

  @Test
  public void testParseValueInternalPercent() {
    for (Locale locale : DecimalFormat.getAvailableLocales()) {
      setPercent(false);
      setMultiplier(1);
      DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(locale);
      df.applyPattern(getFormat().toPattern());
      setFormat(df);
      AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsRuntimeException("Expected an exception when parsing a string containing '%' and property 'percent' is not set to 'true'.",
          this, TestingUtility.createLocaleSpecificNumberString(locale, false, "59", "88", NumberStringPercentSuffix.JUST_SYMBOL));

      setPercent(true);
      assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal(TestingUtility.createLocaleSpecificNumberString(locale, false, "59", "88", NumberStringPercentSuffix.BLANK_AND_SYMBOL)));
      assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal(TestingUtility.createLocaleSpecificNumberString(locale, false, "59", "88", NumberStringPercentSuffix.JUST_SYMBOL)));
      assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal(TestingUtility.createLocaleSpecificNumberString(locale, false, "59", "88", NumberStringPercentSuffix.NONE)));

      setMultiplier(100);
      assertComparableEquals(BigDecimal.valueOf(0.5988), parseValueInternal(TestingUtility.createLocaleSpecificNumberString(locale, false, "59", "88", NumberStringPercentSuffix.BLANK_AND_SYMBOL)));
      assertComparableEquals(BigDecimal.valueOf(0.5988), parseValueInternal(TestingUtility.createLocaleSpecificNumberString(locale, false, "59", "88", NumberStringPercentSuffix.JUST_SYMBOL)));
      assertComparableEquals(BigDecimal.valueOf(0.5988), parseValueInternal(TestingUtility.createLocaleSpecificNumberString(locale, false, "59", "88", NumberStringPercentSuffix.NONE)));
    }
  }

  @Test
  public void testFormatValueInternal() {
    setMaxFractionDigits(3);
    setRoundingMode(RoundingMode.HALF_EVEN);
    assertEquals(m_formatter.format(12.246), formatValueInternal(BigDecimal.valueOf(12.2465)));
    setRoundingMode(RoundingMode.HALF_UP);
    assertEquals(m_formatter.format(12.247), formatValueInternal(BigDecimal.valueOf(12.2465)));
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
  public void testSetPercent() {
    DecimalFormat dfPercent = (DecimalFormat) DecimalFormat.getPercentInstance(NlsLocale.get());

    // test default
    assertFalse(isPercent());
    assertEquals("", getFormatInternal().getPositiveSuffix());
    assertEquals("", getFormatInternal().getNegativeSuffix());
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsRuntimeException("Expected an exception when parsing a string containing '%' and property 'percent' is not set to 'true'.", this, "59.88 %");
    assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal(m_formatter.format(59.88)));

    setPercent(true);
    assertTrue(isPercent());
    assertEquals(dfPercent.getPositiveSuffix(), getFormatInternal().getPositiveSuffix());
    assertEquals(dfPercent.getNegativeSuffix(), getFormatInternal().getNegativeSuffix());
    assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal(m_formatter.format(59.88) + " %"));
    assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal(m_formatter.format(59.88)));

    setPercent(false);
    assertFalse(isPercent());
    assertEquals("", getFormatInternal().getPositiveSuffix());
    assertEquals("", getFormatInternal().getNegativeSuffix());
    AbstractNumberFieldTest.assertParseToBigDecimalInternalThrowsRuntimeException("Expected an exception when parsing a string containing '%' and property 'percent' is not set to 'true'.", this, "59.88 %");
    assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal(m_formatter.format(59.88)));

    // manually setting suffixes
    getFormatInternal().setPositiveSuffix(dfPercent.getPositiveSuffix());
    assertFalse(isPercent());
    getFormatInternal().setNegativeSuffix(dfPercent.getNegativeSuffix());
    assertTrue(isPercent());
    assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal(m_formatter.format(59.88) + " %"));
    assertComparableEquals(BigDecimal.valueOf(59.88), parseValueInternal(m_formatter.format(59.88)));

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
    DecimalFormat dfPercent = (DecimalFormat) DecimalFormat.getPercentInstance(NlsLocale.get());
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
    assertComparableEquals("expected getter to return new setting", 6, getFractionDigits());
    assertComparableEquals("expected new setting in property change notification", 6, (Integer) propertyTracker.m_cachedProperty);
  }

  @Test
  public void testNoErrorMessage() {
    setValue(BigDecimal.valueOf(5));
    assertNull(getErrorStatus());
    setValue(DEFAULT_MIN_VALUE);
    assertNull(getErrorStatus());
    setValue(DEFAULT_MAX_VALUE);
    assertNull(getErrorStatus());
  }

  @Test
  public void testErrorMessageValueTooLarge() {
    setMaxValue(BigDecimal.valueOf(100));

    setValue(BigDecimal.valueOf(100));
    assertNull(getErrorStatus());
    setValue(BigDecimal.valueOf(101));
    assertNotNull(getErrorStatus());

    assertEquals(ScoutTexts.get("NumberTooLargeMessageX", formatValueInternal(getMaxValue())), getErrorStatus().getMessage());

    setMinValue(BigDecimal.valueOf(10));

    setValue(BigDecimal.valueOf(20));
    assertNull(getErrorStatus());
    setValue(BigDecimal.valueOf(101));
    assertEquals(ScoutTexts.get("NumberTooLargeMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

  @Test
  public void testErrorMessageValueTooSmall() {
    setMinValue(BigDecimal.valueOf(100));

    setValue(BigDecimal.valueOf(100));
    assertNull(getErrorStatus());
    setValue(BigDecimal.valueOf(99));
    assertNotNull(getErrorStatus());
    assertEquals(ScoutTexts.get("NumberTooSmallMessageX", formatValueInternal(getMinValue())), getErrorStatus().getMessage());

    setMaxValue(BigDecimal.valueOf(200));

    setValue(BigDecimal.valueOf(150));
    assertNull(getErrorStatus());
    setValue(BigDecimal.valueOf(50));
    assertEquals(ScoutTexts.get("NumberTooSmallMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())), getErrorStatus().getMessage());
  }

}
