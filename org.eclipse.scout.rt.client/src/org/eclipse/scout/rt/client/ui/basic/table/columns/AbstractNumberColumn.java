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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;

/**
 * Column holding Number
 */
public abstract class AbstractNumberColumn<T extends Number> extends AbstractColumn<T> implements INumberColumn<T> {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private boolean m_validateOnAnyKey;

  public AbstractNumberColumn() {
    super();
  }

  /**
   * Default for {@link INumberColumnd#setMinValue(Number)}
   */
  protected abstract T getConfiguredMinValue();

  /**
   * Default for {@link INumberColumn#setMaxValue(Number)}
   */
  protected abstract T getConfiguredMaxValue();

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  /*
   * Configuration
   */

  /**
   * Configures the format used to render the value. See {@link DecimalFormat#applyPattern(String)} for more information
   * about the expected format.
   * <p>
   * If this configuration is not null, the pattern overrides other configurations that are delegated to the internal
   * {@link DecimalFormat} like for example {@link #setGroupingUsed(boolean)}
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   * 
   * @deprecated Will be removed with scout 3.11. For setting the format override {@link #initConfig()} and call
   *             {@link #setFormat(DecimalFormat)}.
   */
  @Deprecated
  protected String getConfiguredFormat() {
    return null;
  }

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
  protected void initConfig() {
    super.initConfig();
    initFormat();
    setRoundingMode(getConfiguredRoundingMode());
    setGroupingUsed(getConfiguredGroupingUsed());
    if (getConfiguredFormat() != null) {
      ((DecimalFormat) propertySupport.getProperty(INumberValueContainer.PROP_DECIMAL_FORMAT)).applyPattern(getConfiguredFormat());
    }
    setValidateOnAnyKey(getConfiguredValidateOnAnyKey());
    setMaxValue(getConfiguredMaxValue());
    setMinValue(getConfiguredMinValue());
  }

  protected void initFormat() {
    DecimalFormat format = (DecimalFormat) DecimalFormat.getNumberInstance(LocaleThreadLocal.get());
    format.setParseBigDecimal(true);
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(0);
    propertySupport.setProperty(INumberValueContainer.PROP_DECIMAL_FORMAT, format);
  }

  /*
   * Runtime
   */
  @Override
  public void setFormat(DecimalFormat format) {
    if (format == null) {
      throw new IllegalArgumentException("Format may not be null.");
    }

    DecimalFormat newFormat = (DecimalFormat) format.clone();
    newFormat.setParseBigDecimal(true);
    propertySupport.setProperty(INumberValueContainer.PROP_DECIMAL_FORMAT, newFormat);
    validateColumnValues();
  }

  @Override
  public DecimalFormat getFormat() {
    return (DecimalFormat) ((DecimalFormat) propertySupport.getProperty(INumberValueContainer.PROP_DECIMAL_FORMAT)).clone();
  }

  /**
   * @deprecated Will be removed with scout 3.11. Use {@link #setFormat()}.
   */
  @Deprecated
  protected final void setNumberFormat(NumberFormat fmt) {
    if (fmt instanceof DecimalFormat) {
      setFormat((DecimalFormat) fmt);
    }
    validateColumnValues();
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public NumberFormat getNumberFormat() {
    return getFormat();
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
   * Set the maximum value. Value <code>null</code> means no limitation if supported by generic type else
   * the biggest possible value for the type.
   * <p>
   * used only for editing
   */
  @Override
  public void setMaxValue(T maxValue) {
    T min = getMinValue();
    if (maxValue != null && min != null && compareInternal(maxValue, min) < 0) {
      propertySupport.setProperty(PROP_MIN_VALUE, maxValue);
    }
    propertySupport.setProperty(PROP_MAX_VALUE, maxValue);
    validateColumnValues();
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getMaxValue() {
    return (T) propertySupport.getProperty(PROP_MAX_VALUE);
  }

  private int compareInternal(T a, T b) {
    return CompareUtility.compareTo(NumberUtility.numberToBigDecimal(a), NumberUtility.numberToBigDecimal(b));
  }

  /**
   * Set the minimum value. Value <code>null</code> means no limitation if supported by generic type
   * else the smallest possible value for the type.
   * <p>
   * used only for editing
   */
  @Override
  public void setMinValue(T minValue) {
    T max = getMaxValue();
    if (minValue != null && max != null && compareInternal(minValue, max) > 0) {
      propertySupport.setProperty(PROP_MAX_VALUE, minValue);
    }
    propertySupport.setProperty(PROP_MIN_VALUE, minValue);
    validateColumnValues();
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getMinValue() {
    return (T) propertySupport.getProperty(PROP_MIN_VALUE);
  }

  @Override
  public void setValidateOnAnyKey(boolean b) {
    m_validateOnAnyKey = b;
  }

  @Override
  public boolean isValidateOnAnyKey() {
    return m_validateOnAnyKey;
  }

  protected abstract INumberField<T> getEditorField();

  @Override
  protected IFormField prepareEditInternal(ITableRow row) throws ProcessingException {
    INumberField<T> f = getEditorField();
    mapEditorFieldProperties(f);
    return f;
  }

  protected void mapEditorFieldProperties(INumberField<T> f) {
    super.mapEditorFieldProperties(f);
    f.setFormat(getFormat());
    f.setMinValue(getMinValue());
    f.setMaxValue(getMaxValue());
    f.setValidateOnAnyKey(isValidateOnAnyKey());
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    if (cell.getValue() != null) {
      cell.setText(getFormat().format(cell.getValue()));
    }
    else {
      cell.setText("");
    }
  }

}
