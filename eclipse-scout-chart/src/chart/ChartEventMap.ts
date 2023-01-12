/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event as ScoutEvent, PropertyChangeEvent, WidgetEventMap} from '@eclipse-scout/core';
import {AbstractChartRenderer, Chart} from '../index';
import {ChartConfig, ChartData, ClickObject} from './Chart';

export interface ChartValueClickEvent<C = Chart> extends ScoutEvent<C> {
  data: ClickObject;
  originalEvent?: Event;
}

export interface ChartEventMap extends WidgetEventMap {
  'chartRender': ScoutEvent<Chart>;
  'valueClick': ChartValueClickEvent;
  'propertyChange:chartRenderer': PropertyChangeEvent<AbstractChartRenderer>;
  'propertyChange:checkedItems': PropertyChangeEvent<ClickObject[]>;
  'propertyChange:config': PropertyChangeEvent<ChartConfig>;
  'propertyChange:data': PropertyChangeEvent<ChartData>;
}
