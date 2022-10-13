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
import {Event as ScoutEvent, PropertyChangeEvent, WidgetEventMap} from '@eclipse-scout/core';
import {AbstractChartRenderer, Chart} from '../index';
import {ChartConfig, ChartData, ClickObject} from './Chart';

export interface ChartValueClickEvent<C extends Chart = Chart> extends ScoutEvent<C> {
  data: ClickObject;
  originalEvent?: Event;
}

export default interface ChartEventMap extends WidgetEventMap {
  'chartRender': ScoutEvent<Chart>;
  'valueClick': ChartValueClickEvent;
  'propertyChange:chartRenderer': PropertyChangeEvent<AbstractChartRenderer, Chart>;
  'propertyChange:checkedItems': PropertyChangeEvent<ClickObject[], Chart>;
  'propertyChange:config': PropertyChangeEvent<ChartConfig, Chart>;
  'propertyChange:data': PropertyChangeEvent<ChartData, Chart>;
}
