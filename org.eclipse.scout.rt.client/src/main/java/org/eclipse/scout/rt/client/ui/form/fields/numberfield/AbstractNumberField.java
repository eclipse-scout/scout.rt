/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.numberfield;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.numberfield.INumberFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicFieldUIFacade;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.AbstractDecimalField;
import org.eclipse.scout.rt.client.ui.valuecontainer.INumberValueContainer;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

@ClassId("05955664-a6c7-4b3a-8622-3e166fe8ff79")
public abstract class AbstractNumberField<NUMBER extends Number> extends AbstractBasicField<NUMBER> implements INumberField<NUMBER> {

  private IBasicFieldUIFacade m_uiFacade;

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
  protected abstract NUMBER getConfiguredMinValue();

  /**
   * Default for {@link INumberField#setMaxValue(Number)}
   */
  protected abstract NUMBER getConfiguredMaxValue();

  /**
   * Default used for {@link INumberField#setMaxIntegerDigits(int)}
   * <p>
   * Used for formatting and parsing. Specifies the maximum number of digits allowed in the integer portion of a number
   * (before the decimal separator).<br>
   * Corresponds to {@link DecimalFormat#setMaximumIntegerDigits(int)}
   * <p>
   *
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

  /**
   * Additional possible grouping separators for lenient parsing. Return an empty list to just use the locale's default.
   */
  protected Set<Character> getConfiguredLenientDecimalSeparators() {
    return CollectionUtility.hashSet(',', '.');
  }

  protected Set<Character> getConfiguredLenientGroupingSeparators() {
    return CollectionUtility.hashSet(
        '\'', '´', '`', '’', // apostrophe and variations
        ',', '.',
        "\u00B7".charAt(0), // middle dot
        "\u0020".charAt(0), // space
        "\u00A0".charAt(0), // no-break space
        "\u2009".charAt(0), // thin space
        "\u202F".charAt(0) // narrow no-break space
    );
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
    initFormat();
    setRoundingMode(getConfiguredRoundingMode());
    setGroupingUsed(getConfiguredGroupingUsed());
    setMinValue(getConfiguredMinValue());
    setMaxValue(getConfiguredMaxValue());
    setLenientDecimalSeparators(getConfiguredLenientDecimalSeparators());
    setLenientGroupingSeparators(getConfiguredLenientGroupingSeparators());
  }

  protected void initFormat() {
    DecimalFormat format = BEANS.get(NumberFormatProvider.class).getNumberInstance(NlsLocale.get());
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
      refreshDisplayText();
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
    Assertions.assertNotNull(format);

    try {
      DecimalFormat newFormat = (DecimalFormat) format.clone();
      newFormat.setParseBigDecimal(true);

      propertySupport.setProperty(INumberValueContainer.PROP_DECIMAL_FORMAT, newFormat);
      refreshDisplayText();
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
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public int getMaxIntegerDigits() {
    return getFormatInternal().getMaximumIntegerDigits();
  }

  /**
   * Set the minimum value for this field. If value is <code>null</code>, it is replaced by
   * {@link #getMinPossibleValue()}.
   */
  @Override
  public void setMinValue(NUMBER value) {
    NUMBER n = (value == null) ? getMinPossibleValue() : value;
    try {
      setFieldChanging(true);
      //
      NUMBER max = getMaxValue();
      if (n != null && max != null && compareInternal(n, max) > 0) {
        propertySupport.setProperty(PROP_MAX_VALUE, n);
      }
      propertySupport.setProperty(PROP_MIN_VALUE, n);
      if (isInitConfigDone()) {
        setValue(getValue());
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public NUMBER getMinValue() {
    return (NUMBER) propertySupport.getProperty(PROP_MIN_VALUE);
  }

  /**
   * Set the maximum value for this field. If value is <code>null</code>, it is replaced by
   * {@link #getMaxPossibleValue()}.
   */
  @Override
  public void setMaxValue(NUMBER value) {
    NUMBER n = (value == null) ? getMaxPossibleValue() : value;
    try {
      setFieldChanging(true);
      //
      NUMBER min = getMinValue();
      if (n != null && min != null && compareInternal(n, min) < 0) {
        propertySupport.setProperty(PROP_MIN_VALUE, n);
      }
      propertySupport.setProperty(PROP_MAX_VALUE, n);
      if (isInitConfigDone()) {
        setValue(getValue());
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public NUMBER getMaxValue() {
    return (NUMBER) propertySupport.getProperty(PROP_MAX_VALUE);
  }

  @Override
  public void setLenientDecimalSeparators(Set<Character> lenientDecimalSeparators) {
    propertySupport.setProperty(PROP_LENIENT_DECIMAL_SEPARATORS, lenientDecimalSeparators);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<Character> getLenientDecimalSeparators() {
    return (Set<Character>) propertySupport.getProperty(PROP_LENIENT_DECIMAL_SEPARATORS);
  }

  @Override
  public void setLenientGroupingSeparators(Set<Character> lenientGroupingSeparators) {
    propertySupport.setProperty(PROP_LENIENT_GROUPING_SEPARATORS, lenientGroupingSeparators);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<Character> getLenientGroupingSeparators() {
    return (Set<Character>) propertySupport.getProperty(PROP_LENIENT_GROUPING_SEPARATORS);
  }

  private int compareInternal(NUMBER a, NUMBER b) {
    return ObjectUtility.compareTo(NumberUtility.numberToBigDecimal(a), NumberUtility.numberToBigDecimal(b));
  }

  /**
   * Lower bound for the value (depending on the type)
   */
  protected abstract NUMBER getMinPossibleValue();

  /**
   * Upper bound for the value (depending on the type)
   */
  protected abstract NUMBER getMaxPossibleValue();

  @Override
  protected NUMBER validateValueInternal(NUMBER rawValue) {
    NUMBER validValue = null;
    rawValue = super.validateValueInternal(rawValue);
    if (rawValue == null) {
      validValue = null;
    }
    else {
      if (getMaxValue() != null && compareInternal(rawValue, getMaxValue()) > 0) {
        throwNumberTooLarge();
      }
      if (getMinValue() != null && compareInternal(rawValue, getMinValue()) < 0) {
        throwNumberTooSmall();
      }
      validValue = rawValue;
    }
    return validValue;
  }

  @Override
  protected String formatValueInternal(NUMBER validValue) {
    if (validValue == null) {
      return "";
    }
    String displayValue = getFormatInternal().format(validValue);
    return displayValue;
  }

  @Override
  public IBasicFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  protected abstract NUMBER parseValueInternal(String text);

  /**
   * Parses text input into a BigDecimal.
   * <p>
   * Callers can expect the resulting BigDecimal to lie in the range {@link #getMinValue()} --> {@link #getMaxValue()}
   * and for subclasses of {@link AbstractDecimalField} to respect
   * {@link AbstractDecimalField#getConfiguredFractionDigits()} ()}. (The maximum fraction digits used for parsing is
   * adapted to {@link AbstractDecimalField#getMultiplier()} if needed.)
   * <p>
   * If the parsing cannot be done complying these rules and considering {@link #getRoundingMode()} an exception is
   * thrown.
   */
  protected BigDecimal parseToBigDecimalInternal(String text) {
    BigDecimal retVal = null;
    if (text == null) {
      text = "";
    }
    else {
      text = StringUtility.replace(text.trim(), "٪", "%");
    }
    if (!text.isEmpty()) {
      text = ensureSuffix(text);
      ParsePosition p = new ParsePosition(0);
      BigDecimal valBeforeRounding = null;

      BigDecimal parsedVal = (BigDecimal) getFormatInternal().parse(text, p);
      // check for successful parsing
      if (p.getErrorIndex() == -1 && p.getIndex() == text.length()) {
        valBeforeRounding = parsedVal;
      }

      if (valBeforeRounding == null) {
        // try lenient
        valBeforeRounding = parseLenient(text);
      }

      if (valBeforeRounding == null) {
        throw new ProcessingException(TEXTS.get("InvalidNumberMessageX", text));
      }
      try {
        retVal = roundParsedValue(valBeforeRounding);
      }
      catch (ArithmeticException e) { // NOSONAR
        throw new ProcessingException(TEXTS.get("InvalidNumberMessageX", text));
      }
      // check for bad range
      if (getMinPossibleValue() != null && retVal.compareTo(NumberUtility.numberToBigDecimal(getMinPossibleValue())) < 0) {
        throwNumberTooSmall();
      }
      if (getMaxPossibleValue() != null && retVal.compareTo(NumberUtility.numberToBigDecimal(getMaxPossibleValue())) > 0) {
        throwNumberTooLarge();
      }
    }
    return retVal;
  }

  /**
   * Parses a text input to a BigDecimal while being lenient with the used decimal and grouping separators.
   *
   * @return A {@link BigDecimal} representing the parsed String or {@code null} in case parsing was not successful.
   */
  protected BigDecimal parseLenient(String text) {
    DecimalFormat f = getFormat();
    DecimalFormatSymbols dfs = f.getDecimalFormatSymbols();
    Set<Character> decimalSeparators = getLenientDecimalSeparators();
    Set<Character> groupingSeparators = getLenientGroupingSeparators();

    // first check for multiple occurrences of potential decimal separators (with respect to possible grouping) and try simplify it.
    // the idea here is to simplify inputs like "1.500.5" to "1500.5". To the eye it looks more like a mix of grouping/decimal separators.
    // inputs like "123.123.123" or "123,123,123", where it looks more like grouping are untouched.
    for (char d : decimalSeparators) {
      int first = text.indexOf(d);
      int last = text.lastIndexOf(d);
      if (first > -1 && first != last) {
        // check if it might be grouping by distance of 3
        boolean isGrouping = true;
        int currPos = first + 1;
        while (currPos < text.length()) {
          int newPos = text.indexOf(d, currPos);
          if (newPos == -1) {
            // no more occurrences, check if distance to end is valid
            newPos = text.length();
          }
          if ((newPos - currPos) != 3) {
            isGrouping = false;
            break;
          }
          currPos = newPos + 1;
        }
        if (isGrouping) {
          // if it's a clear case of grouping ("123.123.123") ignore it
          break;
        }

        // Only the right-most decimal separator should remain, the others are removed.
        String simplifiedText = text.substring(0, first)
            + text.substring(first, last).replace(d + "", "")
            + text.substring(last);

        ParsePosition p = new ParsePosition(0);
        dfs.setDecimalSeparator(d);
        f.setDecimalFormatSymbols(dfs);
        // try parsing this simplified string
        BigDecimal parsedVal = (BigDecimal) f.parse(simplifiedText, p);
        if (p.getErrorIndex() == -1 && p.getIndex() == simplifiedText.length()) {
          return parsedVal;
        }
      }
    }

    // otherwise try parsing with all combinations of decimal/grouping separators
    for (char d : decimalSeparators) {
      Set<Character> innerGroupingSeparators = CollectionUtility.hashSet(groupingSeparators);
      // the current decimal separator can't be a grouping separator
      innerGroupingSeparators.remove(d);
      dfs.setDecimalSeparator(d);
      for (char g : innerGroupingSeparators) {
        dfs.setGroupingSeparator(g);
        ParsePosition p = new ParsePosition(0);
        // use current grouping separator
        f.setDecimalFormatSymbols(dfs);
        // try parsing with the current DecimalFormat
        BigDecimal parsedVal = (BigDecimal) f.parse(text, p);
        // check for successful parsing
        if (p.getErrorIndex() == -1 && p.getIndex() == text.length()) {
          return parsedVal;
        }
      }
    }
    return null;
  }

  private void throwNumberTooLarge() {
    if (getMinValue() == null || ObjectUtility.equals(getMinValue(), getMinPossibleValue())) {
      throw new VetoException(TEXTS.get("NumberTooLargeMessageX", formatValueInternal(getMaxValue())));
    }
    else {
      throw new VetoException(TEXTS.get("NumberTooLargeMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())));
    }
  }

  private void throwNumberTooSmall() {
    if (getMaxValue() == null || ObjectUtility.equals(getMaxValue(), getMaxPossibleValue())) {
      throw new VetoException(TEXTS.get("NumberTooSmallMessageX", formatValueInternal(getMinValue())));
    }
    else {
      throw new VetoException(TEXTS.get("NumberTooSmallMessageXY", formatValueInternal(getMinValue()), formatValueInternal(getMaxValue())));
    }
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

  /**
   * Checks whether the given string modification still fulfills the given {@link DecimalFormat} max length constraints.
   *
   * @param format
   *          The {@link DecimalFormat} holding the constraints: {@link DecimalFormat#getMaximumIntegerDigits()},
   *          {@link DecimalFormat#getMaximumFractionDigits()}.
   * @param curText
   *          The current text (before the modification).
   * @param offset
   *          The offset of the modification relative to the curText parameter.
   * @param replaceLen
   *          How many characters that will be replaced starting at the given offset.
   * @param insertText
   *          The new text that should be inserted at the given replace range.
   * @return <code>true</code> if the given {@link DecimalFormat} length constraints are still fulfilled after the
   *         string modification has been applied or if the resulting string is no valid number. <code>false</code>
   *         otherwise. Also returns <code>true</code> if the String can not be parsed as number (e.g. when it contains
   *         alpha-numerical characters) or is <code>null</code>
   */
  public static boolean isWithinNumberFormatLimits(DecimalFormat format, String curText, int offset, int replaceLen, String insertText) {
    if (insertText == null || insertText.length() < 1) {
      return true;
    }

    String futureText = null;
    if (curText == null) {
      futureText = insertText;
    }
    else {
      StringBuilder docTxt = new StringBuilder(curText.length() + insertText.length());
      docTxt.append(curText);
      docTxt.replace(offset, offset + replaceLen, insertText);
      futureText = docTxt.toString();
    }

    Pattern pat = Pattern.compile("[^1-9" + format.getDecimalFormatSymbols().getZeroDigit() + "]");
    String decimalSeparator = String.valueOf(format.getDecimalFormatSymbols().getDecimalSeparator());
    String[] parts = futureText.split(Pattern.quote(decimalSeparator));
    if (parts.length >= 1) {
      String intPartDigits = pat.matcher(parts[0]).replaceAll("");
      boolean intPartValid = StringUtility.length(intPartDigits) <= format.getMaximumIntegerDigits();
      if (!intPartValid) {
        return false;
      }
    }
    if (parts.length == 2) {
      String fracPartDigits = pat.matcher(parts[1]).replaceAll("");
      boolean fracPartValid = StringUtility.length(fracPartDigits) <= format.getMaximumFractionDigits();
      if (!fracPartValid) {
        return false;
      }
    }
    return true;
  }

  /**
   * Creates a new string which fulfills the given {@link DecimalFormat} max length constraints. An exception is thrown
   * if the number's digits before the decimal point be cut off. It the number's digits after the decimal point would be
   * cut off, no exception is thrown.
   *
   * @param format
   *          The {@link DecimalFormat} holding the constraints: {@link DecimalFormat#getMaximumIntegerDigits()},
   *          {@link DecimalFormat#getMaximumFractionDigits()}.
   * @param curText
   *          The current text (before the modification).
   * @param offset
   *          The offset of the modification relative to the curText parameter.
   * @param replaceLen
   *          How many characters that will be replaced starting at the given offset.
   * @param insertText
   *          The new text that should be inserted at the given replace range.
   * @return String that fulfills the given {@link DecimalFormat} length constraints
   */
  public static String createNumberWithinFormatLimits(DecimalFormat format, String curText, int offset, int replaceLen, String insertText) {
    // !! IMPORTANT NOTE: There is also a JavaScript implementation of this method: org/eclipse/scout/rt/ui/rap/form/fields/numberfield/RwtScoutNumberField.js
    // When changing this implementation also consider updating the js version!
    if (insertText == null || insertText.length() < 1) {
      insertText = "";
    }
    StringBuilder result = new StringBuilder();

    String futureText = null;
    if (curText == null) {
      futureText = insertText;
    }
    else {
      StringBuilder docTxt = new StringBuilder(curText.length() + insertText.length());
      docTxt.append(curText);
      docTxt.replace(offset, offset + replaceLen, insertText);
      futureText = docTxt.toString();
    }

    Pattern pat = Pattern.compile("[^1-9" + format.getDecimalFormatSymbols().getZeroDigit() + "]");
    String decimalSeparator = String.valueOf(format.getDecimalFormatSymbols().getDecimalSeparator());
    String[] parts = futureText.split(Pattern.quote(decimalSeparator));
    if (parts.length >= 1) {
      String intPartDigits = pat.matcher(parts[0]).replaceAll("");
      boolean intPartValid = StringUtility.length(intPartDigits) <= format.getMaximumIntegerDigits();
      if (intPartValid) {
        result.append(intPartDigits);
      }
      else {
        throw new ProcessingException("Do not truncate integer digits!");
      }
    }
    if (parts.length == 2) {
      String fracPartDigits = pat.matcher(parts[1]).replaceAll("");
      boolean fracPartValid = StringUtility.length(fracPartDigits) <= format.getMaximumFractionDigits();
      if (fracPartValid) {
        result.append(decimalSeparator).append(fracPartDigits);
      }
      else {
        result.append(decimalSeparator).append(fracPartDigits, 0, format.getMaximumFractionDigits());
      }
    }
    return result.toString();
  }

  protected static class LocalNumberFieldExtension<NUMBER extends Number, OWNER extends AbstractNumberField<NUMBER>> extends LocalBasicFieldExtension<NUMBER, OWNER> implements INumberFieldExtension<NUMBER, OWNER> {

    public LocalNumberFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected INumberFieldExtension<NUMBER, ? extends AbstractNumberField<NUMBER>> createLocalExtension() {
    return new LocalNumberFieldExtension<>(this);
  }
}
