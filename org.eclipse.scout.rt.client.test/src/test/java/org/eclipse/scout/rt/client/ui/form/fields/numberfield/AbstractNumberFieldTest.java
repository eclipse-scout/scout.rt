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
package org.eclipse.scout.rt.client.ui.form.fields.numberfield;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertComparableEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class AbstractNumberFieldTest extends AbstractNumberField<BigDecimal> {

  private static Locale ORIGINAL_LOCALE;
  private static final BigDecimal DEFAULT_MIN_VALUE = new BigDecimal("-999999999999999999999999999999999999999999999999999999999999");
  private static final BigDecimal DEFAULT_MAX_VALUE = new BigDecimal("999999999999999999999999999999999999999999999999999999999999");

  private AtomicInteger m_displayTextChangedCounter;
  private List<String> m_displayTextChangedHistory;

  @Before
  public void setup() {
    m_displayTextChangedCounter = new AtomicInteger();
    m_displayTextChangedHistory = new ArrayList<>();
    addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IValueField.PROP_DISPLAY_TEXT.equals(evt.getPropertyName())) {
          m_displayTextChangedCounter.incrementAndGet();
          m_displayTextChangedHistory.add((String) evt.getNewValue());
        }
      }
    });
  }

  @BeforeClass
  public static void setupBeforeClass() {
    ORIGINAL_LOCALE = NlsLocale.getOrElse(null);
    NlsLocale.set(new Locale("de", "CH"));
  }

  @AfterClass
  public static void tearDownAfterClass() {
    NlsLocale.set(ORIGINAL_LOCALE);
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

  public static void assertParseToBigDecimalInternalThrowsRuntimeException(String msg, AbstractNumberField<?> field, String textValue) {
    try {
      field.parseToBigDecimalInternal(textValue);
      fail(msg);
    }
    catch (RuntimeException expected) {
    }
  }

  public static <T extends Number> void assertValidateValueInternalThrowsRuntimeException(String msg, AbstractNumberField<T> field, T rawValue) {
    try {
      field.validateValueInternal(rawValue);
      fail(msg);
    }
    catch (RuntimeException expected) {
    }
  }

  @Test
  public void testParseValueSuffix() {
    for (Locale locale : DecimalFormat.getAvailableLocales()) {
      DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(locale);
      df.applyPattern(getFormat().toPattern());
      df.applyPattern("#,##0.00 SUF");
      setFormat(df);
      assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999"));
      assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999 SUF"));
      assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999      SUF"));
      assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999SUF"));

      df.applyPattern("#,##0.00");
      setFormat(df);
      assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999"));
      assertParseToBigDecimalInternalThrowsRuntimeException("After setting a pattern without suffix an excpetion is expected when parsing text with suffix.", this, "9999 SUF");

      getFormatInternal().setPositiveSuffix(" SUF");
      getFormatInternal().setNegativeSuffix(" SUF");
      assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999"));
      assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999 SUF"));
      assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999      SUF"));
      assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999SUF"));

      getFormatInternal().setPositiveSuffix("");
      getFormatInternal().setNegativeSuffix("");
      assertComparableEquals(BigDecimal.valueOf(9999), parseValueInternal("9999"));
      assertParseToBigDecimalInternalThrowsRuntimeException("After setting an empty suffix an excpetion is expected when parsing text with suffix.", this, "9999 SUF");
    }
  }

  @Test
  public void testFormatValueInternal() {
    setRoundingMode(RoundingMode.HALF_EVEN);
    assertEquals("12", formatValueInternal(BigDecimal.valueOf(12.5)));
    setRoundingMode(RoundingMode.HALF_UP);
    assertEquals("13", formatValueInternal(BigDecimal.valueOf(12.5)));

    char groupingSeparator = new DecimalFormatSymbols(NlsLocale.get()).getGroupingSeparator();
    assertEquals("123" + groupingSeparator + "456" + groupingSeparator + "789", formatValueInternal(BigDecimal.valueOf(123456789)));

    setGroupingUsed(false);
    assertEquals("123456789", formatValueInternal(BigDecimal.valueOf(123456789)));

    setGroupingUsed(true);
    assertEquals("123" + groupingSeparator + "456" + groupingSeparator + "789", formatValueInternal(BigDecimal.valueOf(123456789)));
  }

  @Test
  public void testInternalFormatInstanceNotPropagated() {
    setGroupingUsed(true);
    P_PropertyTracker formatTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_DECIMAL_FORMAT, formatTracker);

    DecimalFormat format = getFormat();
    assertNotSame("public getter should not return internal instance", getFormatInternal(), format);
    assertEquals("internal instance and copy returned by public getter should be equal", getFormatInternal(), format);

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

  @Test
  public void testPropertySupportForFormat() {
    setGroupingUsed(true);
    P_PropertyTracker formatTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_DECIMAL_FORMAT, formatTracker);

    DecimalFormat oldFormat = getFormat();
    setFormat(oldFormat);
    assertFalse("expected tracker not to be notified, when new value is same as old value", formatTracker.m_notified);

    DecimalFormat format = getFormat();
    format.applyPattern("#.#");
    setFormat(format);

    assertTrue("expected tracker to be notified, when value changed", formatTracker.m_notified);
    assertFalse("expected new setting in property change notification", ((DecimalFormat) formatTracker.m_cachedProperty).isGroupingUsed());

  }

  @Test
  public void testPropertySupportForMinValue() {
    setMinValue(BigDecimal.TEN);
    P_PropertyTracker propertyTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_MIN_VALUE, propertyTracker);

    setMinValue(BigDecimal.TEN);
    assertFalse("expected tracker not to be notified, when new value is same as old value", propertyTracker.m_notified);

    setMinValue(BigDecimal.ONE);
    assertTrue("expected tracker to be notified, when value changed", propertyTracker.m_notified);
    assertComparableEquals("expected getter to return new setting", BigDecimal.ONE, getMinValue());
    assertComparableEquals("expected new setting in property change notification", BigDecimal.ONE, (BigDecimal) propertyTracker.m_cachedProperty);
  }

  @Test
  public void testPropertySupportForMaxValue() {
    setMaxValue(BigDecimal.TEN);
    P_PropertyTracker propertyTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_MAX_VALUE, propertyTracker);

    setMaxValue(BigDecimal.TEN);
    assertFalse("expected tracker not to be notified, when new value is same as old value", propertyTracker.m_notified);

    setMaxValue(BigDecimal.ONE);
    assertTrue("expected tracker to be notified, when value changed", propertyTracker.m_notified);
    assertComparableEquals("expected getter to return new setting", BigDecimal.ONE, getMaxValue());
    assertComparableEquals("expected new setting in property change notification", BigDecimal.ONE, (BigDecimal) propertyTracker.m_cachedProperty);
  }

  @Test
  public void testPropertySupportForGroupingUsed() {
    setGroupingUsed(true);
    P_PropertyTracker formatTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_DECIMAL_FORMAT, formatTracker);

    setGroupingUsed(true);
    assertFalse("expected tracker not to be notified, when new value is same as old value", formatTracker.m_notified);

    DecimalFormat oldFormat = getFormat();

    setGroupingUsed(false);
    assertTrue("expected tracker to be notified, when value changed", formatTracker.m_notified);
    assertFalse("expected getter to return new setting", isGroupingUsed());
    assertFalse("expected new setting in property change notification", ((DecimalFormat) formatTracker.m_cachedProperty).isGroupingUsed());

    DecimalFormat newFormat = (DecimalFormat) ((DecimalFormat) formatTracker.m_cachedProperty).clone();
    newFormat.setGroupingUsed(true);
    assertEquals("expected no other difference in new format", oldFormat, newFormat);

  }

  @Test
  public void testPropertySupportForRoundingMode() {
    setRoundingMode(RoundingMode.HALF_UP);
    P_PropertyTracker formatTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_DECIMAL_FORMAT, formatTracker);

    setRoundingMode(RoundingMode.HALF_UP);
    assertFalse("expected tracker not to be notified, when new value is same as old value", formatTracker.m_notified);

    DecimalFormat oldFormat = getFormat();

    setRoundingMode(RoundingMode.HALF_EVEN);
    assertTrue("expected tracker to be notified, when value changed", formatTracker.m_notified);
    assertEquals("expected getter to return new setting", RoundingMode.HALF_EVEN, getRoundingMode());
    assertEquals("expected new setting in property change notification", RoundingMode.HALF_EVEN, ((DecimalFormat) formatTracker.m_cachedProperty).getRoundingMode());

    DecimalFormat newFormat = (DecimalFormat) ((DecimalFormat) formatTracker.m_cachedProperty).clone();
    newFormat.setRoundingMode(RoundingMode.HALF_UP);
    assertEquals("expected no other difference in new format", oldFormat, newFormat);

  }

  @Test
  public void testSetMaxValueSmallerThanMin() {
    setMinValue(BigDecimal.valueOf(42));
    assertComparableEquals("expected minValue to be as set before", BigDecimal.valueOf(42), getMinValue());

    setMaxValue(BigDecimal.valueOf(9));
    assertComparableEquals("expected minValue to be adapted when setting maxValue to a smaller value", BigDecimal.valueOf(9), getMinValue());
  }

  @Test
  public void testSetMinValueBiggerThanMax() {
    setMaxValue(BigDecimal.valueOf(7));
    assertComparableEquals("expected maxValue to be as set before", BigDecimal.valueOf(7), getMaxValue());

    setMinValue(BigDecimal.valueOf(9));
    assertComparableEquals("expected maxValue to be adapted when setting minValue to a bigger value", BigDecimal.valueOf(9), getMinValue());
  }

  public static class P_PropertyTracker implements PropertyChangeListener {

    public Object m_cachedProperty;
    public boolean m_notified = false;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      m_notified = true;
      m_cachedProperty = evt.getNewValue();
    }
  }

  @Test
  public void testIsWithinNumberFormatLimits() {
    for (Locale locale : Locale.getAvailableLocales()) {
      DecimalFormat format = (DecimalFormat) DecimalFormat.getNumberInstance(locale);
      format.setMaximumIntegerDigits(3);
      format.setMaximumFractionDigits(2);
      char decimalSeparator = format.getDecimalFormatSymbols().getDecimalSeparator();

      assertTrue(AbstractNumberField.isWithinNumberFormatLimits(format, "123", 2, 0, null));
      assertTrue(AbstractNumberField.isWithinNumberFormatLimits(format, null, 2, 0, null));
      assertTrue(AbstractNumberField.isWithinNumberFormatLimits(format, null, 2, 0, ""));
      assertTrue(AbstractNumberField.isWithinNumberFormatLimits(format, null, 0, 0, "123"));

      assertFalse(AbstractNumberField.isWithinNumberFormatLimits(format, "123", 2, 0, "45"));
      assertTrue(AbstractNumberField.isWithinNumberFormatLimits(format, "1", 1, 0, "23"));

      assertFalse(AbstractNumberField.isWithinNumberFormatLimits(format, "123", 2, 0, decimalSeparator + "456"));
      assertTrue(AbstractNumberField.isWithinNumberFormatLimits(format, "1", 1, 0, decimalSeparator + "23"));

      assertFalse(AbstractNumberField.isWithinNumberFormatLimits(format, "123", 1, 2, "567"));
      assertTrue(AbstractNumberField.isWithinNumberFormatLimits(format, "123", 1, 2, "56"));

      assertFalse(AbstractNumberField.isWithinNumberFormatLimits(format, "123", 1, 2, "567" + decimalSeparator + "7"));
      assertTrue(AbstractNumberField.isWithinNumberFormatLimits(format, "123", 1, 2, "56" + decimalSeparator + "78"));
    }
  }

  @Test
  public void testCreateNumberWithinFormatLimits() {
    for (Locale locale : Locale.getAvailableLocales()) {
      DecimalFormat format = (DecimalFormat) DecimalFormat.getNumberInstance(locale);
      char decimalSeparator = format.getDecimalFormatSymbols().getDecimalSeparator();

      format.setMaximumIntegerDigits(2);
      format.setMaximumFractionDigits(2);
      assertEquals(format("", decimalSeparator), AbstractNumberField.createNumberWithinFormatLimits(format, null, 0, 0, null));
      assertEquals(format("21.12", decimalSeparator), AbstractNumberField.createNumberWithinFormatLimits(format, format("21.12", decimalSeparator), 0, 0, null));
      assertEquals(format("21.12", decimalSeparator), AbstractNumberField.createNumberWithinFormatLimits(format, format("21.12", decimalSeparator), 0, 0, ""));
      assertEquals(format("21.12", decimalSeparator), AbstractNumberField.createNumberWithinFormatLimits(format, null, 0, 0, format("21.12", decimalSeparator)));
      assertEquals(format("21.12", decimalSeparator), AbstractNumberField.createNumberWithinFormatLimits(format, format("21.00", decimalSeparator), 3, 2, "12"));
      assertEquals(format("12.98", decimalSeparator), AbstractNumberField.createNumberWithinFormatLimits(format, format("12.12", decimalSeparator), 3, 2, "987"));
      assertEquals(format("12", decimalSeparator), AbstractNumberField.createNumberWithinFormatLimits(format, format("80", decimalSeparator), 0, 2, "12"));

      format.setMaximumIntegerDigits(2);
      format.setMaximumFractionDigits(5);
      assertEquals(format("12.12345", decimalSeparator), AbstractNumberField.createNumberWithinFormatLimits(format, format("12.12", decimalSeparator), 3, 2, "123456789"));

      format.setMaximumFractionDigits(2);
      format.setMaximumIntegerDigits(4);
      try {
        AbstractNumberField.createNumberWithinFormatLimits(format, format("12.12", decimalSeparator), 0, 2, "12345");
        fail("Exception should be thrown");
      }
      catch (RuntimeException expected) {
        //okay, expected
      }

      try {
        AbstractNumberField.createNumberWithinFormatLimits(format, null, 0, 0, "12345678.1234");
        fail("Exception should be thrown");
      }
      catch (RuntimeException expected) {
        //okay, expected
      }
    }
  }

  @Test
  public void testValidateValueInternalMaxMin() {
    // expect default for maxValue=getMaxPossibleValue() and minValue=getMinPossibleValue()
    assertEquals("expected to pass validation", getMaxPossibleValue(), validateValueInternal(getMaxPossibleValue()));
    assertEquals("expected to pass validation", getMinPossibleValue(), validateValueInternal(getMinPossibleValue()));

    setMaxValue(BigDecimal.valueOf(99));
    setMinValue(BigDecimal.valueOf(-99));
    AbstractNumberFieldTest.assertValidateValueInternalThrowsRuntimeException("Expected an exception when parsing a string representing a too big number.", this, BigDecimal.valueOf(100));
    AbstractNumberFieldTest.assertValidateValueInternalThrowsRuntimeException("Expected an exception when parsing a string representing a too small number.", this, BigDecimal.valueOf(-100));
    assertEquals("expected to pass validation", BigDecimal.valueOf(99), validateValueInternal(BigDecimal.valueOf(99)));
    assertEquals("expected to pass validation", BigDecimal.valueOf(-99), validateValueInternal(BigDecimal.valueOf(-99)));
  }

  private String format(String s, char decimalSeparator) {
    return s.replace('.', decimalSeparator);
  }

  @Test
  public void testSetMaxAndMinValueNull() {
    assertEquals("expect default for maxValue=getMaxPossibleValue()", getMaxPossibleValue(), getMaxValue());
    assertEquals("expect default for minValue=getMinPossibleValue()", getMinPossibleValue(), getMinValue());

    setMaxValue(BigDecimal.valueOf(99));
    setMinValue(BigDecimal.valueOf(-99));
    assertEquals("maxValue not as set above", BigDecimal.valueOf(99), getMaxValue());
    assertEquals("minValue not as set above", BigDecimal.valueOf(-99), getMinValue());

    setMaxValue(null);
    setMinValue(null);
    assertEquals("expect default for maxValue=getMaxPossibleValue() after calling setter with null-param", getMaxPossibleValue(), getMaxValue());
    assertEquals("expect default for minValue=getMinPossibleValue() after calling setter with null-param", getMinPossibleValue(), getMinValue());
  }

  @Test
  public void testDisplayTextInitialState() throws Exception {
    assertEquals("", getDisplayText());
    assertEquals(0, m_displayTextChangedCounter.get());
    assertArrayEquals(new String[]{}, m_displayTextChangedHistory.toArray());
  }

  @Test
  public void testDisplayTextSameTextTwiceUnformatted() throws Exception {
    getUIFacade().parseAndSetValueFromUI("12345");
    assertEquals("12'345", getDisplayText());
    getUIFacade().parseAndSetValueFromUI("12345"); // input does not match display text
    assertEquals("12'345", getDisplayText());

    assertEquals(2, m_displayTextChangedCounter.get());
    assertArrayEquals(new String[]{"12'345", "12'345"}, m_displayTextChangedHistory.toArray());
  }

  @Test
  public void testDisplayTextSameTextTwiceFormatted() throws Exception {
    getUIFacade().parseAndSetValueFromUI("12'345");
    assertEquals("12'345", getDisplayText());
    getUIFacade().parseAndSetValueFromUI("12'345"); // input matches display text
    assertEquals("12'345", getDisplayText());

    assertEquals(1, m_displayTextChangedCounter.get());
    assertArrayEquals(new String[]{"12'345"}, m_displayTextChangedHistory.toArray());
  }

  @Test
  public void testDisplayTextNoValueChangeOnEmptyText() throws Exception {
    getUIFacade().parseAndSetValueFromUI("123");
    assertEquals("123", getDisplayText());
    getUIFacade().parseAndSetValueFromUI("");
    assertEquals("", getDisplayText());
    getUIFacade().parseAndSetValueFromUI("");
    assertEquals("", getDisplayText());
    getUIFacade().parseAndSetValueFromUI(null);
    assertEquals("", getDisplayText());

    assertEquals(2, m_displayTextChangedCounter.get());
    assertArrayEquals(new String[]{"123", ""}, m_displayTextChangedHistory.toArray());
  }
}
