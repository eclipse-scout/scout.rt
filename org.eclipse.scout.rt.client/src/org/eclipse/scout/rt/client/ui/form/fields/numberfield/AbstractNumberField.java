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
package org.eclipse.scout.rt.client.ui.form.fields.numberfield;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.ScoutTexts;

public abstract class AbstractNumberField<T extends Number> extends AbstractValueField<T> implements INumberField<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractNumberField.class);

  private INumberFieldUIFacade m_uiFacade;
  private String m_format;
  private boolean m_groupingUsed;
  private T m_minValue;
  private T m_maxValue;

  public AbstractNumberField() {
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(230)
  @ConfigPropertyValue("null")
  protected String getConfiguredFormat() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(240)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredGroupingUsed() {
    return true;
  }

  @Override
  @ConfigPropertyValue("1")
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setFormat(getConfiguredFormat());
    setGroupingUsed(getConfiguredGroupingUsed());
  }

  public void setFormat(String s) {
    m_format = s;
    if (isInitialized()) {
      if (isAutoDisplayText()) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  public String getFormat() {
    return m_format;
  }

  public void setGroupingUsed(boolean b) {
    m_groupingUsed = b;
    if (isInitialized()) {
      if (isAutoDisplayText()) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  public boolean isGroupingUsed() {
    return m_groupingUsed;
  }

  public void setMinValue(T n) {
    try {
      setFieldChanging(true);
      //
      T max = getMaxValue();
      if (n != null && max != null && compareInternal(n, max) > 0) {
        m_maxValue = n;
      }
      m_minValue = n;
      if (isInitialized()) {
        setValue(getValue());
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  public T getMinValue() {
    return m_minValue;
  }

  public void setMaxValue(T n) {
    try {
      setFieldChanging(true);
      //
      T min = getMinValue();
      if (n != null && min != null && compareInternal(n, min) < 0) {
        m_minValue = n;
      }
      m_maxValue = n;
      if (isInitialized()) {
        setValue(getValue());
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  public T getMaxValue() {
    return m_maxValue;
  }

  @SuppressWarnings("unchecked")
  private int compareInternal(T a, T b) {
    return CompareUtility.compareTo((Comparable) a, (Comparable) b);
  }

  @Override
  protected T validateValueInternal(T rawValue) throws ProcessingException {
    T validValue = null;
    rawValue = super.validateValueInternal(rawValue);
    if (rawValue == null) {
      validValue = null;
    }
    else {
      if (getMaxValue() != null && compareInternal(rawValue, getMaxValue()) > 0) throw new VetoException(ScoutTexts.get("NumberTooLargeMessageXY", "" + getMinValue(), "" + getMaxValue()));
      if (getMinValue() != null && compareInternal(rawValue, getMinValue()) < 0) throw new VetoException(ScoutTexts.get("NumberTooSmallMessageXY", "" + getMinValue(), "" + getMaxValue()));
      validValue = rawValue;
    }
    return validValue;
  }

  @Override
  protected String formatValueInternal(T validValue) {
    if (validValue == null) return "";
    String displayValue = createNumberFormat().format(validValue);
    return displayValue;
  }

  protected NumberFormat createNumberFormat() {
    NumberFormat fmt = null;
    if (getFormat() != null) {
      DecimalFormat x = (DecimalFormat) DecimalFormat.getNumberInstance();
      x.applyPattern(getFormat());
      x.setMinimumFractionDigits(0);
      x.setMaximumFractionDigits(0);
      fmt = x;
    }
    else {
      fmt = NumberFormat.getNumberInstance();
      fmt.setMinimumFractionDigits(0);
      fmt.setMaximumFractionDigits(0);
      fmt.setGroupingUsed(isGroupingUsed());
    }
    return fmt;
  }

  public INumberFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  private class P_UIFacade implements INumberFieldUIFacade {
    public boolean setTextFromUI(String newText) {
      if (newText != null && newText.length() == 0) newText = null;
      // parse always, validity might change even if text is same
      return parseValue(newText);
    }
  }
}
