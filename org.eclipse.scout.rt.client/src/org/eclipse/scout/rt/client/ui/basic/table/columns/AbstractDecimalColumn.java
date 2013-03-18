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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.AbstractDecimalField;

/**
 * Column holding Decimal number
 */
public abstract class AbstractDecimalColumn<T extends Number> extends AbstractColumn<T> implements IDecimalColumn<T> {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private String m_format;
  private boolean m_groupingUsed;
  private NumberFormat m_fmt;
  private int m_maxFractionDigits;
  private int m_minFractionDigits;
  private int m_fractionDigits;
  private boolean m_percent;
  private int m_multiplier;
  private T m_minValue;
  private T m_maxValue;

  public AbstractDecimalColumn() {
    super();
  }

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
   * Subclasses can override this method. Default is {@code null}.
   * 
   * @return Format of this column.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(140)
  @ConfigPropertyValue("null")
  protected String getConfiguredFormat() {
    return null;
  }

  /**
   * Configures whether grouping is used for this column. If grouping is used, the values may be displayed with a digit
   * group separator.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   * 
   * @return {@code true} if grouping is used for this column, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(150)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredGroupingUsed() {
    return true;
  }

  /**
   * Configures the minimum number of fraction digits used to display the value. To use an exact number of fraction
   * digits, the same number as for {@link #getConfiguredMaxFractionDigits()} must be returned.
   * <p>
   * This property only has an effect if no format is specified by {@link #getConfiguredFormat()}.
   * <p>
   * Subclasses can override this method. Default is {@code 2}.
   * 
   * @return Minimum number of fraction digits of this column.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(160)
  @ConfigPropertyValue("2")
  protected int getConfiguredMinFractionDigits() {
    return 2;
  }

  /**
   * Configures the maximum number of fraction digits used to display the value. To use an exact number of fraction
   * digits, the same number as for {@link #getConfiguredMinFractionDigits()} must be returned.
   * <p>
   * This property only has an effect if no format is specified by {@link #getConfiguredFormat()}.
   * <p>
   * Subclasses can override this method. Default is {@code 2}.
   * 
   * @return maximum number of fraction digits of this column.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(170)
  @ConfigPropertyValue("2")
  protected int getConfiguredMaxFractionDigits() {
    return 2;
  }

  /**
   * Configures whether the value is a percentage and is displayed with the appropriate sign. A value of 12 is displayed
   * as 12 % (depending on the locale). Use {@link #getConfiguredMultiplier()} to handle the value differently (e.g.
   * display a value of 0.12 as 12 %).
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   * 
   * @return {@code true} if the column represents a percentage value, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(180)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredPercent() {
    return false;
  }

  /**
   * Configures the multiplier used to display the value. See {@link DecimalFormat#setMultiplier(int)} for more
   * information about multipliers.
   * <p>
   * Subclasses can override this method. Default is {@code 1}.
   * 
   * @return The multiplier used to display the value.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(190)
  @ConfigPropertyValue("1")
  protected int getConfiguredMultiplier() {
    return 1;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(200)
  @ConfigPropertyValue("2")
  protected int getConfiguredFractionDigits() {
    return 2;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setFormat(getConfiguredFormat());
    setGroupingUsed(getConfiguredGroupingUsed());
    setMinFractionDigits(getConfiguredMinFractionDigits());
    setMaxFractionDigits(getConfiguredMaxFractionDigits());
    setPercent(getConfiguredPercent());
    setFractionDigits(getConfiguredFractionDigits());
    setMultiplier(getConfiguredMultiplier());
  }

  /*
   * Runtime
   */
  @Override
  public void setFormat(String s) {
    m_format = s;
    setNumberFormat(null);
  }

  protected final void setNumberFormat(NumberFormat fmt) {
    m_fmt = fmt;
    validateColumnValues();
  }

  @Override
  public NumberFormat getNumberFormat() {
    return m_fmt;
  }

  @Override
  public String getFormat() {
    return m_format;
  }

  @Override
  public void setGroupingUsed(boolean b) {
    m_groupingUsed = b;
    setNumberFormat(null);
  }

  @Override
  public boolean isGroupingUsed() {
    return m_groupingUsed;
  }

  @Override
  public void setMinFractionDigits(int i) {
    if (i > getMaxFractionDigits()) {
      m_maxFractionDigits = i;
    }
    m_minFractionDigits = i;
    setNumberFormat(null);
  }

  @Override
  public int getMinFractionDigits() {
    return m_minFractionDigits;
  }

  @Override
  public void setMaxFractionDigits(int i) {
    if (i < getMinFractionDigits()) {
      m_minFractionDigits = i;
    }
    m_maxFractionDigits = i;
    setNumberFormat(null);
  }

  @Override
  public int getMaxFractionDigits() {
    return m_maxFractionDigits;
  }

  @Override
  public void setPercent(boolean b) {
    m_percent = b;
    setNumberFormat(null);
  }

  @Override
  public boolean isPercent() {
    return m_percent;
  }

  @Override
  public void setFractionDigits(int i) {
    m_fractionDigits = i;
  }

  @Override
  public int getFractionDigits() {
    return m_fractionDigits;
  }

  @Override
  public void setMultiplier(int i) {
    m_multiplier = i;
    setNumberFormat(null);
  }

  @Override
  public int getMultiplier() {
    return m_multiplier;
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

  protected abstract AbstractDecimalField<T> getEditorField();

  @Override
  protected IFormField prepareEditInternal(ITableRow row) throws ProcessingException {
    AbstractDecimalField<T> f = getEditorField();
    f.setFormat(getFormat());
    f.setMaxFractionDigits(getMaxFractionDigits());
    f.setMinFractionDigits(getMinFractionDigits());
    f.setFractionDigits(getNumberFormat().getMaximumFractionDigits());
    f.setMultiplier(getMultiplier());
    f.setGroupingUsed(isGroupingUsed());
    f.setPercent(isPercent());
    f.setMinValue(getMinValue());
    f.setMaxValue(getMaxValue());
    return f;
  }

}
