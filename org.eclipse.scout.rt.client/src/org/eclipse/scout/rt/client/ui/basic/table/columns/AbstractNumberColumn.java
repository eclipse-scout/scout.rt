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
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;

/**
 * Column holding Number
 */
public abstract class AbstractNumberColumn<T extends Number> extends AbstractColumn<T> implements INumberColumn<T> {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private String m_format;
  private boolean m_groupingUsed;
  private NumberFormat m_fmt;
  private T m_minValue;
  private T m_maxValue;

  public AbstractNumberColumn() {
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

  @Override
  protected void initConfig() {
    super.initConfig();
    setFormat(getConfiguredFormat());
    setGroupingUsed(getConfiguredGroupingUsed());
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

  protected abstract AbstractNumberField<T> getEditorField();

  @Override
  protected IFormField prepareEditInternal(ITableRow row) throws ProcessingException {
    AbstractNumberField<T> f = getEditorField();
    f.setFormat(getFormat());
    f.setGroupingUsed(isGroupingUsed());
    f.setMinValue(getMinValue());
    f.setMaxValue(getMaxValue());
    return f;
  }

}
