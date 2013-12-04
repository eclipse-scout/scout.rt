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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;

/**
 * Column holding Number
 */
public abstract class AbstractNumberColumn<T extends Number> extends AbstractColumn<T> implements INumberColumn<T> {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private DecimalFormat m_format;
  private T m_minValue;
  private T m_maxValue;
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
   * Sets the rounding mode used for formatting and parsing. When set to ROUND_UNNECESSARY the parsing accepts only
   * values that can be assigned without rounding to the field's generic type and respect the maxFractionDigits property
   * for decimal number fields.
   * <p>
   * When set to an invalid value, an {@link IllegalArgumentException} is thrown during {@link #initConfig()} Valid
   * values are {@link BigDecimal#ROUND_DOWN}, {@link BigDecimal#ROUND_CEILING}, {@link BigDecimal#ROUND_FLOOR},
   * {@link BigDecimal#ROUND_HALF_UP}, {@link BigDecimal#ROUND_HALF_DOWN}, {@link BigDecimal#ROUND_HALF_EVEN},
   * {@link BigDecimal#ROUND_UNNECESSARY}
   */
  @ConfigProperty(ConfigProperty.ROUNDING_MODE)
  @Order(170)
  protected int getConfiguredRoundingMode() {
    return BigDecimal.ROUND_UNNECESSARY;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    initFormat();
    setRoundingMode(RoundingMode.valueOf(getConfiguredRoundingMode()));
    setGroupingUsed(getConfiguredGroupingUsed());
    if (getConfiguredFormat() != null) {
      m_format.applyPattern(getConfiguredFormat());
    }
    setValidateOnAnyKey(getConfiguredValidateOnAnyKey());
    setMaxValue(getConfiguredMaxValue());
    setMinValue(getConfiguredMinValue());
  }

  protected void initFormat() {
    m_format = (DecimalFormat) DecimalFormat.getNumberInstance(LocaleThreadLocal.get());
    m_format.setParseBigDecimal(true);
    m_format.setMinimumFractionDigits(0);
    m_format.setMaximumFractionDigits(0);
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
    m_format = newFormat;

  }

  @Override
  public DecimalFormat getFormat() {
    return (DecimalFormat) m_format.clone();
  }

  /**
   * @deprecated Will be removed with scout 3.11. Use {@link #setFormat()}.
   */
  @Deprecated
  protected final void setNumberFormat(NumberFormat fmt) {
    if (fmt instanceof DecimalFormat) {
      m_format = (DecimalFormat) ((DecimalFormat) fmt).clone();
    }
    validateColumnValues();
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public NumberFormat getNumberFormat() {
    return (NumberFormat) m_format.clone();
  }

  @Override
  public void setRoundingMode(RoundingMode roundingMode) {
    m_format.setRoundingMode(roundingMode);
  }

  @Override
  public RoundingMode getRoundingMode() {
    return m_format.getRoundingMode();
  }

  /**
   * @return the internal {@link DecimalFormat} instance.
   */
  protected DecimalFormat getFormatInternal() {
    return m_format;
  }

  @Override
  public void setGroupingUsed(boolean b) {
    m_format.setGroupingUsed(b);
  }

  @Override
  public boolean isGroupingUsed() {
    return m_format.isGroupingUsed();
  }

  @Override
  public void setMaxValue(T value) {
    m_maxValue = value;
    validateColumnValues();
  }

  @Override
  public T getMaxValue() {
    return m_maxValue;
  }

  @Override
  public void setMinValue(T value) {
    m_minValue = value;
    validateColumnValues();
  }

  @Override
  public T getMinValue() {
    return m_minValue;
  }

  @Override
  public void setValidateOnAnyKey(boolean b) {
    m_validateOnAnyKey = b;
  }

  @Override
  public boolean isValidateOnAnyKey() {
    return m_validateOnAnyKey;
  }

  protected abstract AbstractNumberField<T> getEditorField();

  @Override
  protected IFormField prepareEditInternal(ITableRow row) throws ProcessingException {
    AbstractNumberField<T> f = getEditorField();
    mapEditorFieldProperties(f);
    return f;
  }

  protected void mapEditorFieldProperties(AbstractNumberField<T> f) {
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
