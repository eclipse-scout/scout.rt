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
import {ActionEventMap, PropertyChangeEvent} from '@eclipse-scout/core/src';
import {TableControlChartAggregation, TableControlChartGroup, TableControlChartType} from './ChartTableControl';

export default interface ChartTableControlEventMap extends ActionEventMap {
  'propertyChange:chartAggregation': PropertyChangeEvent<TableControlChartAggregation>;
  'propertyChange:chartGroup1': PropertyChangeEvent<TableControlChartGroup>;
  'propertyChange:chartGroup2': PropertyChangeEvent<TableControlChartGroup>;
  'propertyChange:chartType': PropertyChangeEvent<TableControlChartType>;
}
