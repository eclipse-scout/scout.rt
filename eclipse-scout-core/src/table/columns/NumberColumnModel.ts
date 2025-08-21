/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ColumnModel, DecimalFormat, DecimalFormatOptions, NumberColumnAggregationFunction} from '../../index';

export interface NumberColumnModel extends ColumnModel<number> {
  aggregationFunction?: NumberColumnAggregationFunction;
  backgroundEffect?: 'colorGradient1' | 'colorGradient2' | 'barChart';
  decimalFormat?: DecimalFormat | string | DecimalFormatOptions;
  /**
   * Has no effect on the column itself, but is passed to the cell editor if the column is editable.
   *
   * @see NumberField.fractionDigits
   */
  fractionDigits?: number;
  /**
   * Defines the {@link NumberFieldModel.minValue} of the cell editor if the column is {@link editable}.
   *
   * The property will only be passed to the cell editor and has no effect on the column itself.
   *
   * Default is null.
   */
  minValue?: number;
  /**
   * Defines the {@link NumberFieldModel.maxValue} of the cell editor if the column is {@link editable}.
   *
   * The property will only be passed to the cell editor and has no effect on the column itself.
   *
   * Default is null.
   */
  maxValue?: number;
}
