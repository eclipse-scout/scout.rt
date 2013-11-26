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
import java.math.RoundingMode;

import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.IDecimalField;

/**
 * Field type representing a non-fractional number such as Integer, Long,
 * BigInteger
 * 
 * @see IDecimalField for floating point numbers
 */
public interface INumberField<T extends Number> extends IBasicField<T> {

  // Rounding Modes
  /** corresponds to {@link BigDecimal#ROUND_UP} */
  int ROUND_UP = BigDecimal.ROUND_UP;
  /** corresponds to {@link BigDecimal#ROUND_DOWN} */
  int ROUND_DOWN = BigDecimal.ROUND_DOWN;
  /** corresponds to {@link BigDecimal#ROUND_CEILING} */
  int ROUND_CEILING = BigDecimal.ROUND_CEILING;
  /** corresponds to {@link BigDecimal#ROUND_FLOOR} */
  int ROUND_FLOOR = BigDecimal.ROUND_FLOOR;
  /** corresponds to {@link BigDecimal#ROUND_HALF_UP} */
  int ROUND_HALF_UP = BigDecimal.ROUND_HALF_UP;
  /** corresponds to {@link BigDecimal#ROUND_HALF_DOWN} */
  int ROUND_HALF_DOWN = BigDecimal.ROUND_HALF_DOWN;
  /** corresponds to {@link BigDecimal#ROUND_HALF_EVEN} */
  int ROUND_HALF_EVEN = BigDecimal.ROUND_HALF_EVEN;
  /** corresponds to {@link BigDecimal#ROUND_UNNECESSARY} */
  int ROUND_UNNECESSARY = BigDecimal.ROUND_UNNECESSARY;

  void setFormat(String s);

  String getFormat();

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
