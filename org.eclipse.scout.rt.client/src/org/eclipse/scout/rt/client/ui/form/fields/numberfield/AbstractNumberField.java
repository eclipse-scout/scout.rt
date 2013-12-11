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
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.AbstractDecimalField;
import org.eclipse.scout.rt.shared.ScoutTexts;

public abstract class AbstractNumberField<T extends Number> extends AbstractBasicField<T> implements INumberField<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractNumberField.class);

  @SuppressWarnings("deprecation")
  private INumberFieldUIFacade m_uiFacade;
  private DecimalFormat m_format;
  private T m_minValue;
  private T m_maxValue;

  public AbstractNumberField() {
    this(true);
  }

  public AbstractNumberField(boolean callInitializer) {
    super(callInitializer);
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
  @Order(230)
  protected String getConfiguredFormat() {
    return null;
  }

  /**
   * Default used for {@link INumberField#setGroupingUsed(boolean)}
   */
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
   * values are {@link BigDecimal#ROUND_DOWN}, {@link BigDecimal#ROUND_CEILING}, {@link BigDecimal#ROUND_FLOOR},
   * {@link BigDecimal#ROUND_HALF_UP}, {@link BigDecimal#ROUND_HALF_DOWN}, {@link BigDecimal#ROUND_HALF_EVEN},
   * {@link BigDecimal#ROUND_UNNECESSARY}
   */
  @ConfigProperty(ConfigProperty.ROUNDING_MODE)
  @Order(250)
  protected int getConfiguredRoundingMode() {
    return BigDecimal.ROUND_UNNECESSARY;
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
    initFormat();
    setRoundingMode(RoundingMode.valueOf(getConfiguredRoundingMode()));
    setGroupingUsed(getConfiguredGroupingUsed());
    if (getConfiguredFormat() != null) {
      m_format.applyPattern(getConfiguredFormat());
    }
    setMinValue(getConfiguredMinValue());
    setMaxValue(getConfiguredMaxValue());
  }

  protected void initFormat() {
    m_format = (DecimalFormat) DecimalFormat.getNumberInstance(LocaleThreadLocal.get());
    m_format.setParseBigDecimal(true);
    m_format.setMinimumFractionDigits(0);
    m_format.setMaximumFractionDigits(0);
  }

  @Override
  public void setRoundingMode(RoundingMode roundingMode) {
    m_format.setRoundingMode(roundingMode);
  }

  @Override
  public RoundingMode getRoundingMode() {
    return m_format.getRoundingMode();
  }

  @Override
  public void setFormat(DecimalFormat format) {
    if (format == null) {
      throw new IllegalArgumentException("Format may not be null.");
    }

    DecimalFormat newFormat = (DecimalFormat) format.clone();
    newFormat.setParseBigDecimal(true);

    m_format = newFormat;
    if (isInitialized()) {
      if (isAutoDisplayText()) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  @Override
  public DecimalFormat getFormat() {
    return (DecimalFormat) m_format.clone();
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
    if (isInitialized()) {
      if (isAutoDisplayText()) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  @Override
  public boolean isGroupingUsed() {
    return m_format.isGroupingUsed();
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
    String displayValue = m_format.format(validValue);
    return displayValue;
  }

  /**
   * @deprecated Will be removed with scout 3.11, use {@link #getFormat()}.
   */
  @Deprecated
  protected NumberFormat createNumberFormat() {
    return getFormat();
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
  protected abstract T parseValueInternal(String text) throws ProcessingException;

  /**
   * Parses text input into a BigDecimal.
   * <p>
   * Callers can expect the resulting BigDecimal to lie in the range {@link #getMinValue()} --> {@link #getMaxValue()}
   * and for subclasses of {@link AbstractDecimalField} to respect
   * {@link AbstractDecimalField#getParsingFractionDigits()}. (The maximum fraction digits used for parsing is adapted
   * to {@link AbstractDecimalField#getMultiplier()} if needed.)
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
      ParsePosition p = new ParsePosition(0);
      BigDecimal valBeforeRounding = (BigDecimal) m_format.parse(text, p);
      // check for bad syntax
      if (p.getErrorIndex() >= 0 || p.getIndex() != text.length()) {
        throw new ProcessingException(ScoutTexts.get("InvalidNumberMessageX", text));
      }
      try {
        retVal = roundParsedValue(valBeforeRounding);
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

  /**
   * Rounds the parsed value according {@link #getRoundingMode()}.
   * 
   * @throws ArithmeticException
   *           if roundingMode is {@link RoundingMode#UNNECESSARY} but rounding would be needed
   */
  protected BigDecimal roundParsedValue(BigDecimal valBeforeRounding) {
    int precision = valBeforeRounding.toBigInteger().toString().length();
    return valBeforeRounding.round(new MathContext(precision, getRoundingMode()));
  }

  private String ensureSuffix(String text) {
    String positiveSuffix = m_format.getPositiveSuffix();
    String negativeSuffix = m_format.getNegativeSuffix();

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
