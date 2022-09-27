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
import {DateRange, Event, PropertyChangeEvent, WidgetEventMap, YearPanel} from '../index';
import {PlannerDisplayMode} from '../planner/Planner';

export interface YearPanelDateSelectEvent<Y extends YearPanel = YearPanel> extends Event<Y> {
  date: Date;
}

export default interface YearPanelEventMap extends WidgetEventMap {
  'dateSelect': YearPanelDateSelectEvent;
  'propertyChange:displayMode': PropertyChangeEvent<PlannerDisplayMode>;
  'propertyChange:viewRange': PropertyChangeEvent<DateRange>;
}
