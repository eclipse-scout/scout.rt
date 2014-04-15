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
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.AbstractDecimalField;
import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;
import org.eclipse.scout.rt.shared.ScoutTexts;

@ClassId("05955664-a6c7-4b3a-8622-3e166fe8ff79")
public abstract class AbstractNumberField<T extends Number> extends AbstractBasicField<T> implements INumberField<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractNumberField.class);

  @SuppressWarnings("deprecation")
  private INumberFieldUIFacade m_uiFacade;

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
   * @deprecated Will be removed with scout 5.0. For setting the format override {@link #initConfig()} and call
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
   * Sets the rounding mode used for formatting and parsing. When set to UNNECESSARY the parsing accepts only values
   * that can be assigned without rounding to the field's generic type and respect the maxFractionDigits property for
   * decimal number fields.
   */
  @ConfigProperty(ConfigProperty.ROUNDING_MODE)
  @Order(250)
  protected RoundingMode getConfiguredRoundingMode() {
    return RoundingMode.UNNECESSARY;
  }

  /**
   * Default for {@link INumberField#setMinValue(Number)}
   */
  protected abstract T getConfiguredMinValue();

  /**
   * Default for {@link INumberField#setMaxValue(Number)}
   */
  protected abstract T getConfiguredMaxValue();

  /**
   * Default used for {@link INumberField#setMaxIntegerDigits(int)}
   * <p>
   * Used for formatting and parsing. Specifies the maximum number of digits allowed in the integer portion of a number
   * (before the decimal separator).<br>
   * Corresponds to {@link DecimalFormat#setMaximumIntegerDigits(int)}
   * <p>
   * 
   * @return
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(280)
  protected int getConfiguredMaxIntegerDigits() {
    return 309;
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    initFormat();
    setRoundingMode(getConfiguredRoundingMode());
    setGroupingUsed(getConfiguredGroupingUsed());
    if (getConfiguredFormat() != null) {
      getFormatInternal().applyPattern(getConfiguredFormat());
    }
    setMinValue(getConfiguredMinValue());
    setMaxValue(getConfiguredMaxValue());
  }

  protected void initFormat() {
    DecimalFormat format = (DecimalFormat) DecimalFormat.getNumberInstance(LocaleThreadLocal.get());
    format.setParseBigDecimal(true);
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(0);
    format.setMaximumIntegerDigits(getConfiguredMaxIntegerDigits());
    propertySupport.setProperty(INumberValueContainer.PROP_DECIMAL_FORMAT, format);
  }

  @Override
  public void setRoundingMode(RoundingMode roundingMode) {
    try {
      DecimalFormat format = getFormat();
      format.setRoundingMode(roundingMode);
      setFormat(format);
      if (isInitialized()) {
        if (shouldUpdateDisplayText(false)) {
          setDisplayText(execFormatValue(getValue()));
        }
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public RoundingMode getRoundingMode() {
    return getFormatInternal().getRoundingMode();
  }

  @Override
  public void setFormat(DecimalFormat format) {
    if (format == null) {
      throw new IllegalArgumentException("Format may not be null.");
    }

    try {
      DecimalFormat newFormat = (DecimalFormat) format.clone();
      newFormat.setParseBigDecimal(true);

      propertySupport.setProperty(INumberValueContainer.PROP_DECIMAL_FORMAT, newFormat);
      if (isInitialized()) {
        if (shouldUpdateDisplayText(false)) {
          setDisplayText(execFormatValue(getValue()));
        }
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public DecimalFormat getFormat() {
    return (DecimalFormat) getFormatInternal().clone();
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
    try {
      DecimalFormat format = getFormat();
      format.setGroupingUsed(b);
      setFormat(format);
      if (isInitialized()) {
        if (shouldUpdateDisplayText(false)) {
          setDisplayText(execFormatValue(getValue()));
        }
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public boolean isGroupingUsed() {
    return getFormatInternal().isGroupingUsed();
  }

  @Override
  public void setMaxIntegerDigits(int maxIntegerDigits) {
    try {
      DecimalFormat format = getFormat();
      format.setMaximumIntegerDigits(maxIntegerDigits);
      setFormat(format);
      if (isInitialized()) {
        if (shouldUpdateDisplayText(false)) {
          setDisplayText(execFormatValue(getValue()));
        }
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public int getMaxIntegerDigits() {
    return getFormatInternal().getMaximumIntegerDigits();
  }

  @Override
  public void setMinValue(T n) {
    try {
      setFieldChanging(true);
      //
      T max = getMaxValue();
      if (n != null && max != null && compareInternal(n, max) > 0) {
        propertySupport.setProperty(PROP_MAX_VALUE, n);
      }
      propertySupport.setProperty(PROP_MIN_VALUE, n);
      if (isInitialized()) {
        setValue(getValue());
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getMinValue() {
    return (T) propertySupport.getProperty(PROP_MIN_VALUE);
  }

  @Override
  public void setMaxValue(T n) {
    try {
      setFieldChanging(true);
      //
      T min = getMinValue();
      if (n != null && min != null && compareInternal(n, min) < 0) {
        propertySupport.setProperty(PROP_MIN_VALUE, n);
      }
      propertySupport.setProperty(PROP_MAX_VALUE, n);
      if (isInitialized()) {
        setValue(getValue());
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getMaxValue() {
    return (T) propertySupport.getProperty(PROP_MAX_VALUE);
  }

  private int compareInternal(T a, T b) {
    return CompareUtility.compareTo(NumberUtility.numberToBigDecimal(a), NumberUtility.numberToBigDecimal(b));
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
    String displayValue = getFormatInternal().format(validValue);
    return displayValue;
  }

  /**
   * @deprecated Will be removed with scout 5.0, use {@link #getFormat()}.
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
    public boolean setTextFromUI(String newText, boolean whileTyping) {
      if (newText != null && newText.length() == 0) {
        newText = null;
      }
      // parse always, validity might change even if text is same
      setWhileTyping(whileTyping);
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
    if (text == null) {
      text = "";
    }
    else {
      text = text.trim();
    }
    if (text.length() > 0) {
      text = ensureSuffix(text);
      ParsePosition p = new ParsePosition(0);
      BigDecimal valBeforeRounding = (BigDecimal) getFormatInternal().parse(text, p);
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
    String positiveSuffix = getFormatInternal().getPositiveSuffix();
    String negativeSuffix = getFormatInternal().getNegativeSuffix();

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
