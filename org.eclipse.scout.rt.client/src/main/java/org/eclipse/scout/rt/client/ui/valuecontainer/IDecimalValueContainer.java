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
package org.eclipse.scout.rt.client.ui.valuecontainer;

import java.text.DecimalFormat;

import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.IDecimalField;
import org.eclipse.scout.rt.platform.nls.NlsLocale;

/**
 * common interface for decimal fields and decimal columns
 * 
 * @param <T>
 */
public interface IDecimalValueContainer<T extends Number> extends INumberValueContainer<T> {

  /**
   * This property is fired on invocation of {@link #setFractionDigits(int)}
   */
  String PROP_PARSING_FRACTION_DIGITS = "parsingFractionDigits";

  /**
   * Sets the minimum fraction digits used for formatting. If value has less fraction digits '0' are appended.
   * <p>
   * If new value is bigger than {@link #getMaxFractionDigits()} maxFractionDigits is set to the same new value.
   */
  void setMinFractionDigits(int i);

  int getMinFractionDigits();

  /**
   * Sets the maximum fraction digits used for formatting and parsing.
   * <p>
   * If new value is smaller than {@link #getMinFractionDigits()} minFractionDigits is set to the same new value.
   */
  void setMaxFractionDigits(int i);

  int getMaxFractionDigits();

  /**
   * When set to true, the local specific (depending on {@link NlsLocale#get()}) positive and negative percentage
   * suffixes are set to the internal {@link DecimalFormat}.<br>
   * <b>Note:</b> This setting is independent from {@link #setMultiplier(int)}. For example if the parsed value for
   * "18 %" should be 0.18, set the multiplier to 100.
   */
  void setPercent(boolean b);

  /**
   * @return true when both positive and negative suffixes correspond to the local specific (depending on
   *         {@link NlsLocale#get()}) percentage symbol
   */
  boolean isPercent();

  /**
   * Sets the number of fraction digits used for rounding. If the text represents a number with more fraction digits the
   * value is rounded to this number of digits according to {@link #getRoundingMode()}<br>
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

}
