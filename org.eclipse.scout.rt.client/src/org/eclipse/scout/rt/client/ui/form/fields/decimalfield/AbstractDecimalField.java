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
package org.eclipse.scout.rt.client.ui.form.fields.decimalfield;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.ScoutTexts;

public abstract class AbstractDecimalField<T extends Number> extends AbstractValueField<T> implements IDecimalField<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDecimalField.class);

  private IDecimalFieldUIFacade m_uiFacade;
  private String m_format;
  private boolean m_groupingUsed;
  private int m_fractionDigits;
  private int m_minFractionDigits;
  private int m_maxFractionDigits;
  private int m_multiplier;
  private boolean m_percent;
  private T m_minValue;
  private T m_maxValue;

  public AbstractDecimalField() {
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

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(290)
  @ConfigPropertyValue("2")
  protected int getConfiguredFractionDigits() {
    return 2;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(240)
  @ConfigPropertyValue("2")
  protected int getConfiguredMinFractionDigits() {
    return 2;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(250)
  @ConfigPropertyValue("2")
  protected int getConfiguredMaxFractionDigits() {
    return 2;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(260)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredGroupingUsed() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredPercent() {
    return false;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(280)
  @ConfigPropertyValue("1")
  protected int getConfiguredMultiplier() {
    return 1;
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
    setMinFractionDigits(getConfiguredMinFractionDigits());
    setMaxFractionDigits(getConfiguredMaxFractionDigits());
    setGroupingUsed(getConfiguredGroupingUsed());
    setPercent(getConfiguredPercent());
    setFractionDigits(getConfiguredFractionDigits());
    setMultiplier(getConfiguredMultiplier());
  }

  @Override
  public void setMinFractionDigits(int i) {
    try {
      setFieldChanging(true);
      //
      if (i > getMaxFractionDigits()) {
        m_maxFractionDigits = i;
      }
      m_minFractionDigits = i;
      if (isInitialized()) {
        if (isAutoDisplayText()) {
          setDisplayText(execFormatValue(getValue()));
        }
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public int getMinFractionDigits() {
    return m_minFractionDigits;
  }

  @Override
  public void setMaxFractionDigits(int i) {
    try {
      setFieldChanging(true);
      //
      if (i < getMinFractionDigits()) {
        m_minFractionDigits = i;
      }
      m_maxFractionDigits = i;
      if (isInitialized()) {
        if (isAutoDisplayText()) {
          setDisplayText(execFormatValue(getValue()));
        }
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public int getMaxFractionDigits() {
    return m_maxFractionDigits;
  }

  @Override
  public void setPercent(boolean b) {
    m_percent = b;
    if (isInitialized()) {
      if (isAutoDisplayText()) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  @Override
  public boolean isPercent() {
    return m_percent;
  }

  @Override
  public void setFractionDigits(int i) {
    m_fractionDigits = i;
    if (isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public int getFractionDigits() {
    return m_fractionDigits;
  }

  @Override
  public void setFormat(String s) {
    m_format = s;
    if (isInitialized()) {
      if (isAutoDisplayText()) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  @Override
  public String getFormat() {
    return m_format;
  }

  @Override
  public void setGroupingUsed(boolean b) {
    m_groupingUsed = b;
    if (isInitialized()) {
      if (isAutoDisplayText()) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  @Override
  public boolean isGroupingUsed() {
    return m_groupingUsed;
  }

  @SuppressWarnings("unchecked")
  private int compareInternal(T a, T b) {
    return CompareUtility.compareTo((Comparable) a, (Comparable) b);
  }

  @Override
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

  @Override
  public T getMinValue() {
    return m_minValue;
  }

  @Override
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

  @Override
  public T getMaxValue() {
    return m_maxValue;
  }

  @Override
  public void setMultiplier(int i) {
    m_multiplier = i;
    if (isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public int getMultiplier() {
    return m_multiplier;
  }

  @Override
  public IDecimalFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  protected String formatValueInternal(T validValue) {
    if (validValue == null) {
      return "";
    }
    String displayValue = createNumberFormat().format(validValue);
    return displayValue;
  }

  @Override
  protected T validateValueInternal(T rawValue) throws ProcessingException {
    T validValue = null;
    rawValue = super.validateValueInternal(rawValue);
    if (rawValue == null) {
      validValue = null;
    }
    else {
      if (getMaxValue() != null && compareInternal(rawValue, getMaxValue()) > 0) {
        throw new VetoException(ScoutTexts.get("NumberTooLargeMessageXY", "" + formatValueInternal(getMinValue()), "" + formatValueInternal(getMaxValue())));
      }
      if (getMinValue() != null && compareInternal(rawValue, getMinValue()) < 0) {
        throw new VetoException(ScoutTexts.get("NumberTooSmallMessageXY", "" + formatValueInternal(getMinValue()), "" + formatValueInternal(getMaxValue())));
      }
      validValue = rawValue;
    }
    return validValue;
  }

  protected NumberFormat createNumberFormat() {
    NumberFormat fmt;
    if (isPercent()) {
      fmt = NumberFormat.getPercentInstance(LocaleThreadLocal.get());
    }
    else {
      fmt = NumberFormat.getNumberInstance(LocaleThreadLocal.get());
    }
    if (fmt instanceof DecimalFormat) {
      ((DecimalFormat) fmt).setMultiplier(getMultiplier());
      if (getFormat() != null) {
        ((DecimalFormat) fmt).applyPattern(getFormat());
      }
      else {
        fmt.setMinimumFractionDigits(getMinFractionDigits());
        fmt.setMaximumFractionDigits(getMaxFractionDigits());
        fmt.setGroupingUsed(isGroupingUsed());
      }
    }
    else {
      fmt.setMinimumFractionDigits(getMinFractionDigits());
      fmt.setMaximumFractionDigits(getMaxFractionDigits());
      fmt.setGroupingUsed(isGroupingUsed());
    }
    return fmt;
  }

  private class P_UIFacade implements IDecimalFieldUIFacade {
    @Override
    public boolean setTextFromUI(String newText) {
      if (newText != null && newText.length() == 0) {
        newText = null;
      }
      // parse always, validity might change even if text is same
      return parseValue(newText);
    }
  }
}
