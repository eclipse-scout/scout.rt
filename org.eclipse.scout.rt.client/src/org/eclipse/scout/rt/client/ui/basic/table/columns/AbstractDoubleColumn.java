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
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;

/**
 * Column holding Double
 */
public abstract class AbstractDoubleColumn extends AbstractColumn<Double> implements IDoubleColumn {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private String m_format = null;
  private boolean m_groupingUsed;
  private int m_maxFractionDigits;
  private int m_minFractionDigits;
  private boolean m_percent;
  private int m_multiplier;
  private NumberFormat m_fmt;

  public AbstractDoubleColumn() {
    super();
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(140)
  @ConfigPropertyValue("null")
  protected String getConfiguredFormat() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(150)
  @ConfigPropertyValue("2")
  protected int getConfiguredMinFractionDigits() {
    return 2;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(160)
  @ConfigPropertyValue("2")
  protected int getConfiguredMaxFractionDigits() {
    return 2;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(170)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredGroupingUsed() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(180)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredPercent() {
    return false;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(190)
  @ConfigPropertyValue("1")
  protected int getConfiguredMultiplier() {
    return 1;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setFormat(getConfiguredFormat());
    setMinFractionDigits(getConfiguredMinFractionDigits());
    setMaxFractionDigits(getConfiguredMaxFractionDigits());
    setGroupingUsed(getConfiguredGroupingUsed());
    setPercent(getConfiguredPercent());
    setMultiplier(getConfiguredMultiplier());
  }

  /*
   * Runtime
   */
  public void setFormat(String s) {
    m_format = s;
    m_fmt = null;
  }

  public String getFormat() {
    return m_format;
  }

  public void setMinFractionDigits(int i) {
    if (i > getMaxFractionDigits()) {
      m_maxFractionDigits = i;
    }
    m_minFractionDigits = i;
    m_fmt = null;
  }

  public int getMinFractionDigits() {
    return m_minFractionDigits;
  }

  public void setMaxFractionDigits(int i) {
    if (i < getMinFractionDigits()) {
      m_minFractionDigits = i;
    }
    m_maxFractionDigits = i;
    m_fmt = null;
  }

  public int getMaxFractionDigits() {
    return m_maxFractionDigits;
  }

  public void setGroupingUsed(boolean b) {
    m_groupingUsed = b;
    m_fmt = null;
  }

  public boolean isGroupingUsed() {
    return m_groupingUsed;
  }

  public void setPercent(boolean b) {
    m_percent = b;
    m_fmt = null;
  }

  public boolean isPercent() {
    return m_percent;
  }

  public void setMultiplier(int i) {
    m_multiplier = i;
    m_fmt = null;
  }

  public int getMultiplier() {
    return m_multiplier;
  }

  @Override
  protected Double parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    Double validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof Double) {
      validValue = (Double) rawValue;
    }
    else if (rawValue instanceof Number) {
      validValue = ((Number) rawValue).doubleValue();
    }
    else {
      throw new ProcessingException("invalid Double value in column '" + getClass().getSimpleName() + "': " + rawValue + " class=" + rawValue.getClass());
    }
    return validValue;
  }

  @Override
  protected IFormField prepareEditInternal(ITableRow row) throws ProcessingException {
    AbstractDoubleField f = new AbstractDoubleField() {
    };
    f.setFormat(getFormat());
    f.setMaxFractionDigits(getMaxFractionDigits());
    f.setMinFractionDigits(getMinFractionDigits());
    f.setMultiplier(getMultiplier());
    f.setGroupingUsed(isGroupingUsed());
    f.setPercent(isPercent());
    f.setLabelVisible(false);
    f.setValue(getValue(row));
    f.markSaved();
    return f;
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    if (cell.getValue() != null) {
      cell.setText(getNumberFormat().format(((Double) cell.getValue()).doubleValue()));
    }
    else {
      cell.setText("");
    }
  }

  public NumberFormat getNumberFormat() {
    if (m_fmt == null) {
      if (isPercent()) {
        m_fmt = NumberFormat.getPercentInstance();
      }
      else {
        m_fmt = NumberFormat.getNumberInstance();
      }
      if (m_fmt instanceof DecimalFormat) {
        ((DecimalFormat) m_fmt).setMultiplier(getMultiplier());
        if (getFormat() != null) {
          ((DecimalFormat) m_fmt).applyPattern(getFormat());
        }
        else {
          m_fmt.setMinimumFractionDigits(getMinFractionDigits());
          m_fmt.setMaximumFractionDigits(getMaxFractionDigits());
          m_fmt.setGroupingUsed(isGroupingUsed());
        }
      }
      else {
        m_fmt.setMinimumFractionDigits(getMinFractionDigits());
        m_fmt.setMaximumFractionDigits(getMaxFractionDigits());
        m_fmt.setGroupingUsed(isGroupingUsed());
      }
    }
    return m_fmt;
  }

}
