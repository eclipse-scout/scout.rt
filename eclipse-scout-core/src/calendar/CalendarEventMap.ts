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
import {Calendar, CalendarComponent, CalendarDisplayMode, DateRange, Event, GroupBox, Menu, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface CalendarComponentMoveEvent<C extends Calendar = Calendar> extends Event<C> {
  component: CalendarComponent;
}

export interface CalendarEventMap extends WidgetEventMap {
  'componentMove': CalendarComponentMoveEvent;
  'modelChange': Event<Calendar>;
  'selectionChange': Event<Calendar>;
  'selectedRangeChange': Event<Calendar>;
  'propertyChange:displayMode': PropertyChangeEvent<CalendarDisplayMode>;
  'propertyChange:menuInjectionTarget': PropertyChangeEvent<GroupBox>;
  'propertyChange:menus': PropertyChangeEvent<Menu[]>;
  'propertyChange:selectedDate': PropertyChangeEvent<Date>;
  'propertyChange:viewRange': PropertyChangeEvent<DateRange>;
}
