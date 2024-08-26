/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.valuecontainer;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * common interface for number fields and number columns
 *
 * @param <T>
 */
public interface INumberValueContainer<T extends Number> {

  /**
   * This property is fired on format changes. Can be triggered either through {@link #setFormat(DecimalFormat)} directly
   * or through a convenience setter for DecimalFormat's properties (e.g. {@link #setGroupingUsed(boolean)})
   */
  String PROP_DECIMAL_FORMAT = "decimalFormat";

  /**
   * Property fired when minValue changes. See {@link #setMinValue(Number)}
   */
  String PROP_MIN_VALUE = "minValue";

  /**
   * Property fired when maxValue changes. See {@link #setMaxValue(Number)}
   */
  String PROP_MAX_VALUE = "maxValue";

  /**
   * Sets the format used for formatting and parsing. (The properties of the provided format are applied to the internal
   * DecimalFormat instance. Therefore, changes on the provided formats instance after this method returns have no
   * influence to the internal instance.)
   *
   * @throws IllegalArgumentException
   *           if format is null
   */
  void setFormat(DecimalFormat format);

  /**
   * @return A copy of the internal DecimalFormat instance. Changes on the returned instance have no effect on the
   *         internal instance.
   */
  DecimalFormat getFormat();

  /**
   * Sets whether grouping will be used for formatting.
   */
  void setGroupingUsed(boolean b);

  boolean isGroupingUsed();

  /**
   * Sets the minimum value. Value <code>null</code> means no limitation if supported by generic type else the smallest
   * possible value for the type.
   * <p>
   * If new value is bigger than {@link #getMaxValue()} maxValue is set to the same new value.
   */
  void setMinValue(T value);

  T getMinValue();

  /**
   * Sets the maximum value. Value <code>null</code> means no limitation if supported by generic type else the biggest
   * possible value for the type.
   * <p>
   * If new value is smaller than {@link #getMinValue()} minValue is set to the same new value.
   */
  void setMaxValue(T value);

  T getMaxValue();

  /**
   * Sets the rounding mode used for formatting and parsing.
   */
  void setRoundingMode(RoundingMode roundingMode);

  RoundingMode getRoundingMode();

  /**
   * Sets the maximum number of digits allowed in the integer portion of a number.
   *
   * @param maxIntegerDigits
   *          the maximum number of digits allowed
   */
  void setMaxIntegerDigits(int maxIntegerDigits);

  /**
   * @return The maximum number of digits allowed in the integer portion of a number.
   */
  int getMaxIntegerDigits();
}
