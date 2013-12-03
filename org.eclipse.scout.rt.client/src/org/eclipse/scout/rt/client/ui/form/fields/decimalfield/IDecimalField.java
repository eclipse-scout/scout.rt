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
   * Sets the minimum fraction digits used for formatting. If value has less fraction digits '0' are appended.
   * Corresponds to {@link DecimalFormat#setMinimumFractionDigits(int)}.<br>
   * <b>Note:</b> If a format is set over {@link #setFormat(String)}, this property is ignoered.
   */
  void setMinFractionDigits(int i);

  int getMinFractionDigits();

  /**
   * Sets the maximum fraction digits used for formatting. Corresponds to
   * {@link DecimalFormat#setMaximumFractionDigits(int)}.<br>
   * <b>Note:</b> If a format is set over {@link #setFormat(String)}, this property is ignoered.
   */
  void setMaxFractionDigits(int i);

  int getMaxFractionDigits();

  /**
   * When set to true, a percentage format (depending on {@link LocaleThreadLocal#get()}) is used for parsing and
   * formatting.<br>
   * <b>Note:</b> This setting is independent from {@link #setMultiplier(int)}
   */
  void setPercent(boolean b);

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
