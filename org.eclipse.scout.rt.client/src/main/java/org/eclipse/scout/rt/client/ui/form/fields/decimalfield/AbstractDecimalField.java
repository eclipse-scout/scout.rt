/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.decimalfield;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.eclipse.scout.rt.client.extension.ui.form.fields.decimalfield.IDecimalFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.eclipse.scout.rt.client.ui.valuecontainer.IDecimalValueContainer;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;

@ClassId("f4ec575e-f572-418d-a49c-3d0811ea7540")
public abstract class AbstractDecimalField<T extends Number> extends AbstractNumberField<T> implements IDecimalField<T> {

  public AbstractDecimalField() {
    this(true);
  }

  public AbstractDecimalField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */

  /**
   * Default for {@link IDecimalField#setFractionDigits(int)}
   * <p>
   * Used for rounding the text value coming from the GUI. If the text represents a number with more fraction digits the
   * value is rounded to this number of digits according to {@link #getRoundingMode()}<br>
   * <b>Note:</b> This property is only used when parsing text input from GUI, and not when setting the value over
   * {@link #setValue(Object)}.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(290)
  protected int getConfiguredFractionDigits() {
    return 2;
  }

  /**
   * Default for {@link IDecimalField#setMinFractionDigits(int)}
   * <p>
   * Used for formatting. For example if set to 2: 14.7 is displayed as "14.70", 14 is displayed as "14.00"<br>
   * Corresponds to {@link DecimalFormat#setMinimumFractionDigits(int)}
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(240)
  protected int getConfiguredMinFractionDigits() {
    return 2;
  }

  /**
   * Default for {@link IDecimalField#setMaxFractionDigits(int)}
   * <p>
   * Used for formatting and parsing.<br>
   * Corresponds to {@link DecimalFormat#setMaximumFractionDigits(int)}
   * <p>
   * <b>Note:</b> Always define the fraction digits for the displayed value even if you use a multiplier (
   * {@link #getConfiguredMultiplier()}) other than 1. In those cases precision is increased as needed during parsing.
   * (e.g. 53.84% with maxFractionDigits=2 and multiplier=100 is parsed to 0.5384)
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(250)
  protected int getConfiguredMaxFractionDigits() {
    return 2;
  }

  /**
   * Default for {@link IDecimalField#setPercent(boolean)}
   * <p>
   * When set to true, a percentage format (depending on {@link NlsLocale#get()}) is used for parsing an formatting.<br>
   * <b>Note:</b> This setting is independent from {@link #getConfiguredMultiplier()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected boolean getConfiguredPercent() {
    return false;
  }

  /**
   * Default for {@link IDecimalField#setMultiplier(int)}
   * <p>
   * Sets multiplier for parsing and formatting. Corresponds to {@link DecimalFormat#setMultiplier(int)}<br>
   * <b>Note:</b> For correct behavior the {@link IDecimalField} default implementations expect the multiplier to be a
   * power of ten.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(280)
  protected int getConfiguredMultiplier() {
    return 1;
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  @Override
  protected RoundingMode getConfiguredRoundingMode() {
    return RoundingMode.HALF_UP;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
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
      DecimalFormat format = getFormat();
      format.setMinimumFractionDigits(i);
      setFormat(format);
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public int getMinFractionDigits() {
    return getFormatInternal().getMinimumFractionDigits();
  }

  @Override
  public void setMaxFractionDigits(int i) {
    try {
      setFieldChanging(true);
      //
      DecimalFormat format = getFormat();
      format.setMaximumFractionDigits(i);
      setFormat(format);
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public int getMaxFractionDigits() {
    return getFormatInternal().getMaximumFractionDigits();
  }

  @Override
  public void setPercent(boolean b) {
    try {
      DecimalFormat percentDF = BEANS.get(NumberFormatProvider.class).getPercentInstance(NlsLocale.get());
      DecimalFormat format = getFormat();
      if (b) {
        format.setPositiveSuffix(percentDF.getPositiveSuffix());
        format.setNegativeSuffix(percentDF.getNegativeSuffix());
      }
      else {
        if (isPercent()) {
          format.setPositiveSuffix("");
          format.setNegativeSuffix("");
        }
      }
      setFormat(format);
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public boolean isPercent() {
    DecimalFormat percentDF = BEANS.get(NumberFormatProvider.class).getPercentInstance(NlsLocale.get());
    DecimalFormat internalDF = getFormatInternal();
    return internalDF.getPositiveSuffix().equals(percentDF.getPositiveSuffix()) && internalDF.getNegativeSuffix().equals(percentDF.getNegativeSuffix());
  }

  @Override
  public void setFractionDigits(int i) {
    try {
      propertySupport.setPropertyInt(IDecimalValueContainer.PROP_PARSING_FRACTION_DIGITS, i);
      if (isInitialized()) {
        setValue(getValue());
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public int getFractionDigits() {
    return propertySupport.getPropertyInt(IDecimalValueContainer.PROP_PARSING_FRACTION_DIGITS);
  }

  @Override
  public void setMultiplier(int i) {
    try {
      DecimalFormat format = getFormat();
      format.setMultiplier(i);
      setFormat(format);
      if (isInitialized()) {
        setValue(getValue());
      }
    }
    finally {
      setFieldChanging(false);
    }
  }

  @Override
  public int getMultiplier() {
    return getFormatInternal().getMultiplier();
  }

  /**
   * Rounds the parsed value according {@link #getRoundingMode()} and {@link #getFractionDigits()}. (The maximum
   * fraction digits used for parsing is adapted to {@link #getMultiplier()} if needed.)
   *
   * @throws ArithmeticException
   *           if roundingMode is {@link RoundingMode#UNNECESSARY} but rounding would be needed
   */
  @Override
  protected BigDecimal roundParsedValue(BigDecimal valBeforeRounding) {
    // multiplier requirements for fraction digits are considered
    int additionalFractionDigits = ("" + Math.abs(getMultiplier())).length() - 1;
    int precision = valBeforeRounding.toBigInteger().toString().length() + getFractionDigits() + additionalFractionDigits;
    return valBeforeRounding.round(new MathContext(precision, getRoundingMode()));
  }

  protected static class LocalDecimalFieldExtension<T extends Number, OWNER extends AbstractDecimalField<T>> extends LocalNumberFieldExtension<T, OWNER> implements IDecimalFieldExtension<T, OWNER> {

    public LocalDecimalFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IDecimalFieldExtension<T, ? extends AbstractDecimalField<T>> createLocalExtension() {
    return new LocalDecimalFieldExtension<>(this);
  }
}
