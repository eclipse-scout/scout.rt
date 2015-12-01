/************************0******************************************************
 * Copyright (c) 2010 BSI B0usiness Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.IDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberFieldTest.P_PropertyTracker;
import org.eclipse.scout.rt.client.ui.valuecontainer.IDecimalValueContainer;
import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(PlatformTestRunner.class)
public class AbstractDecimalColumnTest extends AbstractDecimalColumn<BigDecimal> {

  @Override
  protected IDecimalField<BigDecimal> createDefaultEditor() {
    return new AbstractBigDecimalField() {
    };
  }

  @Override
  protected BigDecimal getConfiguredMinValue() {
    return null;
  }

  @Override
  protected BigDecimal getConfiguredMaxValue() {
    return null;
  }

  @Test
  public void testDecimalFormatHandling() {
    DecimalFormat format = getFormat();
    String percentSuffix = (BEANS.get(NumberFormatProvider.class).getPercentInstance(NlsLocale.get())).getPositiveSuffix();
    assertTrue("expected groupingUsed-property set to true as default", format.isGroupingUsed());
    assertTrue("expected groupingUsed-property set to true as default", isGroupingUsed());
    assertEquals("expected minFractionDigits-property set to 2 as default", 2, getMinFractionDigits());
    assertEquals("expected minFractionDigits-property set to 2 as default", 2, format.getMinimumFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 2 as default", 2, getMaxFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 2 as default", 2, format.getMaximumFractionDigits());
    assertFalse("expected percent-property set to false as default", isPercent());
    assertFalse("expected percent-property set to false as default", format.getPositiveSuffix().endsWith(percentSuffix) && format.getPositiveSuffix().equals(format.getNegativeSuffix()));
    assertEquals("expected multiplier-property set to 1 as default", 1, getMultiplier());
    assertEquals("expected multiplier-property set to 1 as default", 1, format.getMultiplier());
    assertEquals("expected fractionDigits-property set to 2 as default", 2, getFractionDigits());

    format.setGroupingUsed(false);
    format.setMinimumFractionDigits(4);
    format.setMaximumFractionDigits(7);
    format.setPositiveSuffix(percentSuffix);
    format.setNegativeSuffix(percentSuffix);
    format.setMultiplier(100);
    setFormat(format);
    format = getFormat();
    assertFalse("expected groupingUsed-property set to false after setting format", format.isGroupingUsed());
    assertFalse("expected groupingUsed-property set to false after setting format", isGroupingUsed());
    assertEquals("expected minFractionDigits-property set to 4 after setting format", 4, getMinFractionDigits());
    assertEquals("expected minFractionDigits-property set to 4 after setting format", 4, format.getMinimumFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 7 after setting format", 7, getMaxFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 7 after setting format", 7, format.getMaximumFractionDigits());
    assertTrue("expected percent-property set to true after setting format", isPercent());
    assertTrue("expected percent-property set to true after setting format", format.getPositiveSuffix().endsWith(percentSuffix) && format.getPositiveSuffix().equals(format.getNegativeSuffix()));
    assertEquals("expected multiplier-property set to 100 after setting format", 100, getMultiplier());
    assertEquals("expected multiplier-property set to 100 after setting format", 100, format.getMultiplier());

    setGroupingUsed(true);
    setMinFractionDigits(5);
    setMaxFractionDigits(6);
    setPercent(false);
    setMultiplier(1000);
    setFractionDigits(3);
    format = getFormat();
    assertTrue("expected groupingUsed-property set to true after using convenience setter", format.isGroupingUsed());
    assertTrue("expected groupingUsed-property set to true after using convenience setter", isGroupingUsed());
    assertEquals("expected minFractionDigits-property set to 5 after using convenience setter", 5, getMinFractionDigits());
    assertEquals("expected minFractionDigits-property set to 5 after using convenience setter", 5, format.getMinimumFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 6 after using convenience setter", 6, getMaxFractionDigits());
    assertEquals("expected maxFractionDigits-property set to 6 after using convenience setter", 6, format.getMaximumFractionDigits());
    assertFalse("expected percent-property set to false after using convenience setter", format.getPositiveSuffix().endsWith(percentSuffix) && format.getPositiveSuffix().equals(format.getNegativeSuffix()));
    assertFalse("expected percent-property set to false after using convenience setter", isPercent());
    assertEquals("expected multiplier-property set to 1000 after using convenience setter", 1000, getMultiplier());
    assertEquals("expected multiplier-property set to 1000 after using convenience setter", 1000, format.getMultiplier());
    assertEquals("expected fractionDigits-property set to 3 after using setter", 3, getFractionDigits());

  }

  @Test
  public void testPrepareEditInternal() {
    setGroupingUsed(false);
    setMinFractionDigits(5);
    setMaxFractionDigits(6);
    setPercent(true);
    setMultiplier(100);
    setFractionDigits(3);

    ITableRow row = Mockito.mock(ITableRow.class);
    AbstractBigDecimalField field = (AbstractBigDecimalField) prepareEditInternal(row);
    assertFalse("expected groupingUsed-property to be propagated to field", field.isGroupingUsed());
    assertEquals("expected minFractionDigits-property to be propagated to field", 5, field.getMinFractionDigits());
    assertEquals("expected maxFractionDigits-property to be propagated to field", 6, field.getMaxFractionDigits());
    assertTrue("expected percent-property to be propagated to field", field.isPercent());
    assertEquals("expected multiplier-property to be propagated to field", 100, field.getMultiplier());
    assertEquals("expected fractionDigits-property to be propagated to field", 3, field.getFractionDigits());

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
    ScoutAssert.assertComparableEquals("expected getter to return new setting", 6, getFractionDigits());
    ScoutAssert.assertComparableEquals("expected new setting in property change notification", 6, (Integer) propertyTracker.m_cachedProperty);
  }

}
