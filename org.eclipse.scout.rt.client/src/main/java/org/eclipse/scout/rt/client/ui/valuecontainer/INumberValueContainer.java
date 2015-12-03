/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
   * This property is fired on format changes. Can be triggerd either through {@link #setFormat(DecimalFormat)} directly
   * or through a conveniece setter for DecimalFormat's properties (e.g. {@link #setGroupingUsed(boolean)})
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
   * DecimalFormat instance. Therefore changes on the provided formats instance after this method returns have no
   * influence to the internal instance.)
   * 
   * @param format
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
   * Sets whether or not grouping will be used for formatting.
   */
  void setGroupingUsed(boolean b);

  boolean isGroupingUsed();

  /**
   * Set the minimum value. Value <code>null</code> means no limitation if supported by generic type else the smallest
   * possible value for the type.
   * <p>
   * If new value is bigger than {@link #getMaxValue()} maxValue is set to the same new value.
   */
  void setMinValue(T value);

  T getMinValue();

  /**
   * Set the maximum value. Value <code>null</code> means no limitation if supported by generic type else the biggest
   * possible value for the type.
   * <p>
   * If new value is smaller than {@link #getMinValue()} minValue is set to the same new value.
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
