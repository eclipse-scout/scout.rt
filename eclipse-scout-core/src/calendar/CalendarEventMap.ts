/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Calendar, CalendarComponent, CalendarDisplayMode, DateRange, Event, GroupBox, Menu, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface CalendarComponentMoveEvent<C extends Calendar = Calendar> extends Event<C> {
  component: CalendarComponent;
}

export interface ResourceVisibilityChangeEvent<C extends Calendar = Calendar> extends Event<C> {
  resourceId: number;
  visible: boolean;
}

export interface SelectedResourceChangeEvent<C extends Calendar = Calendar> extends Event<C> {
  resourceId: string;
}

export interface CalendarEventMap extends WidgetEventMap {
  'componentMove': CalendarComponentMoveEvent;
  'modelChange': Event<Calendar>;
  'selectionChange': Event<Calendar>;
  'selectedRangeChange': Event<Calendar>;
  'resourceVisibilityChange': ResourceVisibilityChangeEvent;
  'selectedResourceChange': SelectedResourceChangeEvent;
  'propertyChange:displayMode': PropertyChangeEvent<CalendarDisplayMode>;
  'propertyChange:menuInjectionTarget': PropertyChangeEvent<GroupBox>;
  'propertyChange:menus': PropertyChangeEvent<Menu[]>;
  'propertyChange:selectedDate': PropertyChangeEvent<Date>;
  'propertyChange:viewRange': PropertyChangeEvent<DateRange>;
  'propertyChange:showCalendarSidebar': PropertyChangeEvent<boolean>;
  'propertyChange:showResourcePanel': PropertyChangeEvent<boolean>;
  'propertyChange:showListPanel': PropertyChangeEvent<boolean>;
}
