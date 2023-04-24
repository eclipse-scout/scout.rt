/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BasicFieldModel, DecimalFormat, DecimalFormatOptions} from '../../../index';

export interface NumberFieldModel extends BasicFieldModel<number, number | string> {
  /**
   * Specifies the minimum value. Value <code>null</code> means no limitation.
   *
   * If value is bigger than {@link maxValue}, the current maxValue is set to the same new value.
   *
   * Default is null.
   */
  minValue?: number;
  /**
   * Specifies the maximum value. Value <code>null</code> means no limitation.
   *
   * If value is smaller than {@link minValue}, the current minValue is set to the same new value.
   *
   * Default is null.
   */
  maxValue?: number;
  /**
   * Specifies the decimalFormat used to format the displayText and to parse user input.
   * @see DecimalFormat
   *
   * Default is {@link Locale.decimalFormatPatternDefault} of the current locale.
   */
  decimalFormat?: string | DecimalFormat | DecimalFormatOptions;
  /**
   * Specifies the number of fraction digits the value is rounded to. If not set, the value will not be rounded.
   * This will always be applied to the value even if the decimalFormat has a multiplier.
   *
   * Example: decimalFormat with multiplier 100, fractionDigits 1.
   * User input 42 will result in a value 0.4 and a displayText 40.
   *
   * Default is null.
   */
  fractionDigits?: number;
}
