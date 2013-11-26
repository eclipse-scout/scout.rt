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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.shared.ScoutTexts;

public abstract class AbstractNumberField<T extends Number> extends AbstractBasicField<T> implements INumberField<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractNumberField.class);

  @SuppressWarnings("deprecation")
  private INumberFieldUIFacade m_uiFacade;
  private String m_format;
  private boolean m_groupingUsed;
  private T m_minValue;
  private T m_maxValue;
  private RoundingMode m_roundingMode;

  public AbstractNumberField() {
    this(true);
  }

  public AbstractNumberField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(230)
  protected String getConfiguredFormat() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(240)
  protected boolean getConfiguredGroupingUsed() {
    return true;
  }

  /**
   * Default used for {@link INumberField#setRoundingMode(RoundingMode)}
   * <p>
   * Sets the rounding mode used for formatting and parsing. When set to ROUND_UNNECESSARY the parsing accepts only
   * values that can be assigned without rounding to the field's generic type and respect the maxFractionDigits property
   * for decimal number fields.
   * <p>
   * When set to an invalid value, an {@link IllegalArgumentException} is thrown during {@link #initConfig()} Valid
   * values are {@link INumberField#ROUND_DOWN}, {@link INumberField#ROUND_CEILING}, {@link INumberField#ROUND_FLOOR},
   * {@link INumberField#ROUND_HALF_UP}, {@link INumberField#ROUND_HALF_DOWN}, {@link INumberField#ROUND_HALF_EVEN},
   * {@link INumberField#ROUND_UNNECESSARY}
   */
  @ConfigProperty(ConfigProperty.ROUNDING_MODE)
  @Order(250)
  protected int getConfiguredRoundingMode() {
    return INumberField.ROUND_UNNECESSARY;
  }

  /**
   * Default for {@link INumberField#setMinValue(Number)}
   */
  protected abstract T getConfiguredMinValue();

  /**
   * Default for {@link INumberField#setMaxValue(Number)}
   */
  protected abstract T getConfiguredMaxValue();

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setFormat(getConfiguredFormat());
    setGroupingUsed(getConfiguredGroupingUsed());
    setRoundingMode(RoundingMode.valueOf(getConfiguredRoundingMode()));
    setMinValue(getConfiguredMinValue());
    setMaxValue(getConfiguredMaxValue());
  }

  @Override
  public void setRoundingMode(RoundingMode roundingMode) {
    m_roundingMode = roundingMode;
  }

  @Override
  public RoundingMode getRoundingMode() {
    return m_roundingMode;
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

  @Override
  protected String formatValueInternal(T validValue) {
    if (validValue == null) {
      return "";
    }
    String displayValue = createDecimalFormat().format(validValue);
    return displayValue;
  }

  /**
   * @deprecated Will be removed with scout 3.11, use {@link #createDecimalFormat()}.
   */
  @Deprecated
  protected NumberFormat createNumberFormat() {
    return createDecimalFormat();
  }

  /**
   * create a DecimalFormat instance for formatting and parsing
   */
  protected DecimalFormat createDecimalFormat() {
    DecimalFormat fmt = null;
    if (getFormat() != null) {
      DecimalFormat x = (DecimalFormat) DecimalFormat.getNumberInstance(LocaleThreadLocal.get());
      x.applyPattern(getFormat());
      fmt = x;
    }
    else {
      fmt = (DecimalFormat) DecimalFormat.getNumberInstance(LocaleThreadLocal.get());
      fmt.setGroupingUsed(isGroupingUsed());
    }
    fmt.setMinimumFractionDigits(0);
    fmt.setMaximumFractionDigits(0);
    fmt.setRoundingMode(getRoundingMode());
    return fmt;
  }

  @SuppressWarnings("deprecation")
  @Override
  public INumberFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /**
   * When {@link INumberFieldUIFacade} is removed, this class will implements IBasicFieldUIFacade.
   */
  @SuppressWarnings("deprecation")
  private class P_UIFacade implements INumberFieldUIFacade {
    @Override
    public boolean setTextFromUI(String newText) {
      if (newText != null && newText.length() == 0) {
        newText = null;
      }
      // parse always, validity might change even if text is same
      return parseValue(newText);
    }
  }

  @Override
  protected T parseValueInternal(String text) throws ProcessingException {
    throw new ProcessingException("Not implemented");
  }

  /**
   * Parses text input into a BigDecimal.
   * <p>
   * Callers can expect the resulting BigDecimal to lie in the range {@link #getMinValue()} --> {@link #getMaxValue()}
   * and to respect {@link #createDecimalFormat()}'s {@link #getMaximumFractionDigits()}. (The maximum fraction digits
   * used for parsing is adapted to {@link #createDecimalFormat()}'s {@link DecimalFormat#getMultiplier()} if needed.)
   * <p>
   * If the parsing cannot be done complying these rules and considering {@link #getRoundingMode()} an exception is
   * thrown.
   * 
   * @param text
   * @return
   * @throws ProcessingException
   */
  protected BigDecimal parseToBigDecimalInternal(String text) throws ProcessingException {
    BigDecimal retVal = null;
    text = StringUtility.nvl(text, "").trim();
    if (text.length() > 0) {
      text = ensureSuffix(text);
      DecimalFormat df = createDecimalFormat();
      df.setParseBigDecimal(true);
      ParsePosition p = new ParsePosition(0);
      BigDecimal valBeforeRounding = (BigDecimal) df.parse(text, p);
      // check for bad syntax
      if (p.getErrorIndex() >= 0 || p.getIndex() != text.length()) {
        throw new ProcessingException(ScoutTexts.get("InvalidNumberMessageX", text));
      }
      // rounding (multiplier requirements for fraction digits are considered)
      int additionalFractionDigits = ("" + Math.abs(df.getMultiplier())).length() - 1;
      try {
        int precision = valBeforeRounding.toBigInteger().toString().length() + df.getMaximumFractionDigits() + additionalFractionDigits;
        retVal = valBeforeRounding.round(new MathContext(precision, getRoundingMode()));
      }
      catch (ArithmeticException e) {
        throw new ProcessingException(ScoutTexts.get("InvalidNumberMessageX", text));
      }
      // check for bad range
      if (getMinValue() != null && retVal.compareTo(NumberUtility.numberToBigDecimal(getMinValue())) < 0) {
        throw new ProcessingException(ScoutTexts.get("NumberTooSmallMessageXY", String.valueOf(getMinValue()), String.valueOf(getMaxValue())));
      }
      if (getMaxValue() != null && retVal.compareTo(NumberUtility.numberToBigDecimal(getMaxValue())) > 0) {
        throw new ProcessingException(ScoutTexts.get("NumberTooLargeMessageXY", String.valueOf(getMinValue()), String.valueOf(getMaxValue())));
      }
    }
    return retVal;
  }

  private String ensureSuffix(String text) {
    DecimalFormat df = createDecimalFormat();
    String positiveSuffix = df.getPositiveSuffix();
    String negativeSuffix = df.getNegativeSuffix();

    if (positiveSuffix.equals(negativeSuffix)) {
      String trimmedSuffix = StringUtility.trim(positiveSuffix);
      if (text.endsWith(trimmedSuffix)) {
        text = StringUtility.trim(text.substring(0, text.length() - trimmedSuffix.length()));
      }
      text = StringUtility.concatenateTokens(text, positiveSuffix);
    }
    return text;
  }
}
