/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateRange, Event, PlannerDisplayMode, PropertyChangeEvent, WidgetEventMap, YearPanel} from '../index';

export interface YearPanelDateSelectEvent<Y extends YearPanel = YearPanel> extends Event<Y> {
  date: Date;
}

export interface YearPanelEventMap extends WidgetEventMap {
  'dateSelect': YearPanelDateSelectEvent;
  'propertyChange:displayMode': PropertyChangeEvent<PlannerDisplayMode>;
  'propertyChange:viewRange': PropertyChangeEvent<DateRange>;
}
