/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ActionEventMap, PropertyChangeEvent} from '@eclipse-scout/core';
import {TableControlChartAggregation, TableControlChartGroup, TableControlChartType} from './ChartTableControl';

export interface ChartTableControlEventMap extends ActionEventMap {
  'propertyChange:chartAggregation': PropertyChangeEvent<TableControlChartAggregation>;
  'propertyChange:chartGroup1': PropertyChangeEvent<TableControlChartGroup>;
  'propertyChange:chartGroup2': PropertyChangeEvent<TableControlChartGroup>;
  'propertyChange:chartType': PropertyChangeEvent<TableControlChartType>;
}
