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

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.IDecimalField;

/**
 * Field type representing a non-fractional number such as Integer, Long,
 * BigInteger
 *
 * @see IDecimalField for floating point numbers
 */
/**
 * @param <T>
 */
public interface INumberField<T extends Number> extends IBasicField<T> {

  /**
   * Sets the pattern used for formatting. Corresponds to {@link DecimalFormat#applyPattern(String)}. <br>
   * <b>Note:</b> If a format is set, the formatting properties (e.g. {@link #setGroupingUsed(boolean)},
   * {@link IDecimalField#setMinFractionDigits(int) and {@link IDecimalField#setMaxFractionDigits(int)})are ignoered.
   */
  void setFormat(String s);

  String getFormat();

  /**
   * Sets whether or not grouping will be used for formatting. Corresponds to
   * {@link DecimalFormat#setGroupingUsed(boolean)}.<br>
   * <b>Note:</b> If a format is set over {@link #setFormat(String)}, this property is ignoered.
   */
  void setGroupingUsed(boolean b);

  boolean isGroupingUsed();

  /**
   * Set the minimum value for this field. Value <code>null</code> means no limitation if supported by generic type else
   * the smallest possible value for the type.
   */
  void setMinValue(T value);

  T getMinValue();

  /**
   * Set the maximum value for this field. Value <code>null</code> means no limitation if supported by generic type else
   * the biggest possible value for the type.
   */
  void setMaxValue(T value);

  T getMaxValue();

  /**
   * set the rounding mode used for formatting and parsing
   * 
   * @param roundingMode
   */
  void setRoundingMode(RoundingMode roundingMode);

  RoundingMode getRoundingMode();

  /**
   * @deprecated use the facade defined by {@link IBasicField#getUIFacade()}.
   *             Will be removed with the M-Release
   */
  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  INumberFieldUIFacade getUIFacade();

}
