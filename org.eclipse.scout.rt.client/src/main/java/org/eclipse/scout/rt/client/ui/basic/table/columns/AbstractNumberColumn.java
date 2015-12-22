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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.INumberColumnExtension;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.platform.util.NumberUtility;

/**
 * Column holding Number
 */
@ClassId("6b77a24f-8685-4023-b353-cbbe7d4bf22a")
public abstract class AbstractNumberColumn<NUMBER extends Number> extends AbstractColumn<NUMBER> implements INumberColumn<NUMBER> {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private boolean m_validateOnAnyKey;

  private String m_initialAggregationFunction;
  private String m_initialBackgroundEffect;

  public AbstractNumberColumn() {
    super();
  }

  /**
   * Default for {@link INumberColumnd#setMinValue(Number)}
   */
  protected abstract NUMBER getConfiguredMinValue();

  /**
   * Default for {@link INumberColumn#setMaxValue(Number)}
   */
  protected abstract NUMBER getConfiguredMaxValue();

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(280)
  protected int getConfiguredMaxIntegerDigits() {
    return 32;
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  /*
   * Configuration
   */

  /**
   * Configures whether grouping is used for this column. If grouping is used, the values may be displayed with a digit
   * group separator.
   * <p>
   * Default used for {@link #setGroupingUsed(boolean)}
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if grouping is used for this column, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(150)
  protected boolean getConfiguredGroupingUsed() {
    return true;
  }

  /**
   * Causes the ui to send a validate event every time the text field content is changed.
   * <p>
   * Be careful when using this property since this can influence performance and the characteristics of text input.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(160)
  protected boolean getConfiguredValidateOnAnyKey() {
    return false;
  }

  /**
   * Default used for {@link INumberColumnd#setRoundingMode(RoundingMode)}
   * <p>
   * Sets the rounding mode used for formatting and parsing. When set to UNNECESSARY the parsing accepts only values
   * that can be assigned without rounding to the field's generic type and respect the maxFractionDigits property for
   * decimal number fields.
   */
  @ConfigProperty(ConfigProperty.ROUNDING_MODE)
  @Order(170)
  protected RoundingMode getConfiguredRoundingMode() {
    return RoundingMode.UNNECESSARY;
  }

  @Override
  protected boolean getConfiguredUiSortPossible() {
    return true;
  }

  /**
   * Configure the aggregation function for this column
   *
   * @return one of the constant values in {@link INumberColumn#AggregationFunction}
   * @since 5.2
   */
  protected String getConfiguredAggregationFunction() {
    return INumberColumn.AggregationFunction.SUM;
  }

  /**
   * Configure the background effect for this column
   *
   * @return one of the constant values in {@link INumberColumn#BackgroundEffect} or null
   * @since 5.2
   */
  @ConfigProperty(ConfigProperty.STRING)
  protected String getConfiguredBackgroundEffect() {
    return null;
  }

  @Override
  public void initColumn() {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    setBackgroundEffect(prefs.getTableColumnBackgroundEffect(this, getBackgroundEffect(), null));
    super.initColumn();
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    initFormat();
    setRoundingMode(getConfiguredRoundingMode());
    setGroupingUsed(getConfiguredGroupingUsed());
    setValidateOnAnyKey(getConfiguredValidateOnAnyKey());
    setMaxValue(getConfiguredMaxValue());
    setMinValue(getConfiguredMinValue());
    setInitialAggregationFunction(getConfiguredAggregationFunction());
    setAggregationFunction(getConfiguredAggregationFunction());
    setInitialBackgroundEffect(getConfiguredBackgroundEffect());
    setBackgroundEffect(getConfiguredBackgroundEffect());
  }

  protected void initFormat() {
    DecimalFormat format = BEANS.get(NumberFormatProvider.class).getNumberInstance(NlsLocale.get());
    format.setParseBigDecimal(true);
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(0);
    format.setMaximumIntegerDigits(getConfiguredMaxIntegerDigits());
    propertySupport.setProperty(INumberValueContainer.PROP_DECIMAL_FORMAT, format);
  }

  /*
   * Runtime
   */
  @Override
  public void setFormat(DecimalFormat format) {
    Assertions.assertNotNull(format);
    DecimalFormat newFormat = (DecimalFormat) format.clone();
    newFormat.setParseBigDecimal(true);
    propertySupport.setProperty(INumberValueContainer.PROP_DECIMAL_FORMAT, newFormat);
    updateDisplayTexts();
  }

  @Override
  public DecimalFormat getFormat() {
    return (DecimalFormat) ((DecimalFormat) propertySupport.getProperty(INumberValueContainer.PROP_DECIMAL_FORMAT)).clone();
  }

  @Override
  public void setMaxIntegerDigits(int maxIntegerDigits) {
    DecimalFormat format = getFormat();
    format.setMaximumIntegerDigits(maxIntegerDigits);
    setFormat(format);
  }

  @Override
  public int getMaxIntegerDigits() {
    return getFormatInternal().getMaximumIntegerDigits();
  }

  @Override
  public void setRoundingMode(RoundingMode roundingMode) {
    DecimalFormat format = getFormat();
    format.setRoundingMode(roundingMode);
    setFormat(format);
  }

  @Override
  public RoundingMode getRoundingMode() {
    return getFormatInternal().getRoundingMode();
  }

  /**
   * @return the internal {@link DecimalFormat} instance.
   *         <p>
   *         <b> use with care:</b> Only use for read-access. Never change a property directly on the returned instance
   *         and never pass a reference outside subclasses!
   */
  protected DecimalFormat getFormatInternal() {
    return ((DecimalFormat) propertySupport.getProperty(INumberValueContainer.PROP_DECIMAL_FORMAT));
  }

  @Override
  public void setGroupingUsed(boolean b) {
    DecimalFormat format = getFormat();
    format.setGroupingUsed(b);
    setFormat(format);
  }

  @Override
  public boolean isGroupingUsed() {
    return getFormatInternal().isGroupingUsed();
  }

  /**
   * Set the maximum value. Value <code>null</code> means no limitation if supported by generic type else the biggest
   * possible value for the type.
   * <p>
   * used only for editing
   */
  @Override
  public void setMaxValue(NUMBER maxValue) {
    NUMBER min = getMinValue();
    if (maxValue != null && min != null && compareInternal(maxValue, min) < 0) {
      propertySupport.setProperty(PROP_MIN_VALUE, maxValue);
    }
    propertySupport.setProperty(PROP_MAX_VALUE, maxValue);
    refreshValues();
  }

  @SuppressWarnings("unchecked")
  @Override
  public NUMBER getMaxValue() {
    return (NUMBER) propertySupport.getProperty(PROP_MAX_VALUE);
  }

  private int compareInternal(NUMBER a, NUMBER b) {
    return CompareUtility.compareTo(NumberUtility.numberToBigDecimal(a), NumberUtility.numberToBigDecimal(b));
  }

  /**
   * Set the minimum value. Value <code>null</code> means no limitation if supported by generic type else the smallest
   * possible value for the type.
   * <p>
   * used only for editing
   */
  @Override
  public void setMinValue(NUMBER minValue) {
    NUMBER max = getMaxValue();
    if (minValue != null && max != null && compareInternal(minValue, max) > 0) {
      propertySupport.setProperty(PROP_MAX_VALUE, minValue);
    }
    propertySupport.setProperty(PROP_MIN_VALUE, minValue);
    refreshValues();
  }

  @SuppressWarnings("unchecked")
  @Override
  public NUMBER getMinValue() {
    return (NUMBER) propertySupport.getProperty(PROP_MIN_VALUE);
  }

  @Override
  public String getInitialAggregationFunction() {
    return m_initialAggregationFunction;
  }

  @Override
  public void setInitialAggregationFunction(String f) {
    m_initialAggregationFunction = f;
  }

  @Override
  public String getAggregationFunction() {
    return propertySupport.getPropertyString(PROP_AGGREGATION_FUNCTION);
  }

  @Override
  public void setAggregationFunction(String f) {
    propertySupport.setPropertyString(PROP_AGGREGATION_FUNCTION, f);
  }

  @Override
  public String getBackgroundEffect() {
    return propertySupport.getPropertyString(PROP_BACKGROUND_EFFECT);
  }

  @Override
  public void setBackgroundEffect(String effect) {
    propertySupport.setPropertyString(PROP_BACKGROUND_EFFECT, effect);
  }

  @Override
  public void setInitialBackgroundEffect(String effect) {
    m_initialBackgroundEffect = effect;
  }

  @Override
  public String getInitialBackgroundEffect() {
    return m_initialBackgroundEffect;
  }

  @Override
  public void setValidateOnAnyKey(boolean b) {
    m_validateOnAnyKey = b;
  }

  @Override
  public boolean isValidateOnAnyKey() {
    return m_validateOnAnyKey;
  }

  @Override
  protected IFormField prepareEditInternal(ITableRow row) {
    INumberField<NUMBER> f = (INumberField<NUMBER>) getDefaultEditor();
    mapEditorFieldProperties(f);
    return f;
  }

  protected void mapEditorFieldProperties(INumberField<NUMBER> f) {
    super.mapEditorFieldProperties(f);
    f.setFormat(getFormat());
    f.setMinValue(getMinValue());
    f.setMaxValue(getMaxValue());
  }

  @Override
  protected String formatValueInternal(ITableRow row, NUMBER value) {
    return (value != null) ? getFormat().format(value) : "";
  }

  protected static class LocalNumberColumnExtension<NUMBER extends Number, OWNER extends AbstractNumberColumn<NUMBER>> extends LocalColumnExtension<NUMBER, OWNER> implements INumberColumnExtension<NUMBER, OWNER> {

    public LocalNumberColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected INumberColumnExtension<NUMBER, ? extends AbstractNumberColumn<NUMBER>> createLocalExtension() {
    return new LocalNumberColumnExtension<NUMBER, AbstractNumberColumn<NUMBER>>(this);
  }
}
