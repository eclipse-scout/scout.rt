/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ColumnModel, DecimalFormat} from '../../index';
import {DecimalFormatOptions} from '../../text/DecimalFormat';
import {NumberColumnAggregationFunction} from './NumberColumn';

export default interface NumberColumnModel extends ColumnModel {
  aggregationFunction?: NumberColumnAggregationFunction;
  backgroundEffect?: 'colorGradient1' | 'colorGradient2' | 'barChart';
  decimalFormat?: DecimalFormat | string | DecimalFormatOptions;
  minValue?: number;
  maxValue?: number;
}
