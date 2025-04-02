/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, IChartTableControlConfigDo, typeName} from '@eclipse-scout/core';

@typeName('scout.ChartTableControlConfig')
export class ChartTableControlConfigDo extends BaseDoEntity implements IChartTableControlConfigDo {
  chartTypeId?: string;
  chartGroup1ColumnId?: string;
  chartGroup1Modifier?: number;
  chartGroup2ColumnId?: string;
  chartGroup2Modifier?: number;
  chartAggregationColumnId?: string;
  chartAggregationModifier?: number;
}
