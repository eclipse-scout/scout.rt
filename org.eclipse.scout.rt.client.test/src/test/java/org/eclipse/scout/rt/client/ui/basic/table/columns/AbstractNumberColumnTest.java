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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(PlatformTestRunner.class)
public class AbstractNumberColumnTest extends AbstractNumberColumn<Integer> {

  @Override
  protected Integer getConfiguredMinValue() {
    return null;
  }

  @Override
  protected Integer getConfiguredMaxValue() {
    return null;
  }

  @Override
  protected AbstractNumberField<Integer> createDefaultEditor() {
    return new AbstractIntegerField() {
    };
  }

  @Test
  public void testPrepareEditInternal() {
    setGroupingUsed(false);
    String bColor = "469406";
    setBackgroundColor(bColor);
    String fColor = "FAAAF1";
    setForegroundColor(fColor);
    FontSpec fontSpec = new FontSpec("Arial", FontSpec.STYLE_ITALIC | FontSpec.STYLE_BOLD, 0);
    setFont(fontSpec);
    Integer minValue = Integer.valueOf(-42);
    setMinValue(minValue);
    Integer maxValue = Integer.valueOf(42);
    setMaxValue(maxValue);
    ITableRow row = Mockito.mock(ITableRow.class);
    AbstractIntegerField field = (AbstractIntegerField) prepareEditInternal(row);
    assertFalse("expected groupingUsed property to be propagated to field", field.isGroupingUsed());

    setGroupingUsed(true);
    field = (AbstractIntegerField) prepareEditInternal(row);
    assertTrue("expected groupingUsed property to be propagated to field", field.isGroupingUsed());
    assertEquals("expected backgroundColor property to be progagated to field", bColor, field.getBackgroundColor());
    assertEquals("expected foregroundColor property to be progagated to field", fColor, field.getForegroundColor());
    assertEquals("expected font property to be progagated to field", fontSpec, field.getFont());
    assertEquals("expected minValue property to be progagated to field", minValue, field.getMinValue());
    assertEquals("expected maxValue property to be progagated to field", maxValue, field.getMaxValue());
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
  public void testUpdateFormat() {
    TestTable table = new TestTable();
    table.addRow(table.createRow());
    table.getTestNumberColumn().setValue(0, BigDecimal.TEN);
    DecimalFormat format = table.getTestNumberColumn().getFormat();
    format.setMinimumFractionDigits(3);
    table.getTestNumberColumn().setFormat(format);
    assertEquals("10.000", table.getCell(0, 0).getText());
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
    setMinValue(Integer.valueOf(10));
    P_PropertyTracker propertyTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_MIN_VALUE, propertyTracker);

    setMinValue(Integer.valueOf(10));
    assertFalse("expected tracker not to be notified, when new value is same as old value", propertyTracker.m_notified);

    setMinValue(Integer.valueOf(1));
    assertTrue("expected tracker to be notified, when value changed", propertyTracker.m_notified);
    ScoutAssert.assertComparableEquals("expected getter to return new setting", Integer.valueOf(1), getMinValue());
    ScoutAssert.assertComparableEquals("expected new setting in property change notification", Integer.valueOf(1), (Integer) propertyTracker.m_cachedProperty);
  }

  @Test
  public void testPropertySupportForMaxValue() {
    setMaxValue(Integer.valueOf(10));
    P_PropertyTracker propertyTracker = new P_PropertyTracker();
    addPropertyChangeListener(INumberValueContainer.PROP_MAX_VALUE, propertyTracker);

    setMaxValue(Integer.valueOf(10));
    assertFalse("expected tracker not to be notified, when new value is same as old value", propertyTracker.m_notified);

    setMaxValue(Integer.valueOf(1));
    assertTrue("expected tracker to be notified, when value changed", propertyTracker.m_notified);
    ScoutAssert.assertComparableEquals("expected getter to return new setting", Integer.valueOf(1), getMaxValue());
    ScoutAssert.assertComparableEquals("expected new setting in property change notification", Integer.valueOf(1), (Integer) propertyTracker.m_cachedProperty);
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
    setMinValue(Integer.valueOf(42));
    assertEquals("expected minValue to be as set before", Integer.valueOf(42), getMinValue());

    setMaxValue(Integer.valueOf(9));
    assertEquals("expected minValue to be adapted when setting maxValue to a smaller value", Integer.valueOf(9), getMinValue());
  }

  @Test
  public void testSetMinValueBiggerThanMax() {
    setMaxValue(Integer.valueOf(7));
    assertEquals("expected maxValue to be as set before", Integer.valueOf(7), getMaxValue());

    setMinValue(Integer.valueOf(9));
    assertEquals("expected maxValue to be adapted when setting minValue to a bigger value", Integer.valueOf(9), getMinValue());
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

  public class TestTable extends AbstractTable {

    public TestNumberColumn getTestNumberColumn() {
      return getColumnSet().getColumnByClass(TestNumberColumn.class);
    }

    @Order(10)
    public class TestNumberColumn extends AbstractBigDecimalColumn {
    }

  }

}
