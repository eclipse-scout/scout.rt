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

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;

/**
 * Field type representing a fractional, decimal number such as Float, Double,
 * BigDecimal
 */
@SuppressWarnings("deprecation")
public interface IDecimalField<T extends Number> extends INumberField<T> {

  /**
   * Sets the minimum fraction digits used for formatting. If value has less fraction digits '0' are appended. Delegates
   * to {@link DecimalFormat#setMinimumFractionDigits(int)} of the internal {@link DecimalFormat} instance.
   * <p>
   * If new value is bigger than {@link #getMaxFractionDigits()} maxFractionDigits is set to the same new value.
   */
  void setMinFractionDigits(int i);

  int getMinFractionDigits();

  /**
   * Sets the maximum fraction digits used for formatting. Delegates to
   * {@link DecimalFormat#setMaximumFractionDigits(int)} of the internal {@link DecimalFormat} instance.<br>
   * <p>
   * If new value is smaller than {@link #getMinFractionDigits()} minFractionDigits is set to the same new value.
   */
  void setMaxFractionDigits(int i);

  int getMaxFractionDigits();

  /**
   * When set to true, the local specific (depending on {@link LocaleThreadLocal#get()}) positive and negative
   * percentage suffixes are set to the internal {@link DecimalFormat}.<br>
   * <b>Note:</b> This setting is independent from {@link #setMultiplier(int)}. For example if the parsed value for
   * "18 %" should be 0.18, set the multiplier to 100.
   */
  void setPercent(boolean b);

  /**
   * @return true when both positive and negative suffixes correspond to the local specific (depending on
   *         {@link LocaleThreadLocal#get()}) percentage symbol
   */
  boolean isPercent();

  /**
   * Sets the number of fraction digits used for parsing. If the text represents a number with more fraction digits the
   * value is rounded according to {@link #getRoundingMode()}<br>
   * <b>Note:</b> This property is only used when parsing text input from GUI, and not when setting the value over
   * {@link #setValue(Object)}.
   * 
   * @param i
   */
  void setFractionDigits(int i);

  int getFractionDigits();

  /**
   * Sets multiplier for parsing and formatting. Corresponds to {@link DecimalFormat#setMultiplier(int)}<br>
   * <b>Note:</b> For correct behavior the {@link IDecimalField} default implementations expect the multiplier to be a
   * power of ten.
   */
  void setMultiplier(int i);

  int getMultiplier();

  /**
   * @deprecated use the facade defined by {@link IBasicField#getUIFacade()}.
   *             Will be removed with the M-Release
   */
  @Override
  @Deprecated
  IDecimalFieldUIFacade getUIFacade();

}
