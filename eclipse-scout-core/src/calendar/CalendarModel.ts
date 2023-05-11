/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CalendarComponent, CalendarDescriptor, CalendarDisplayMode, GroupBox, Menu, ObjectOrChildModel, WidgetModel} from '../index';

export interface CalendarModel extends WidgetModel {
  monthViewNumberOfWeeks?: number;
  numberOfHourDivisions?: number;
  heightPerDivision?: number;
  startHour?: number;
  spaceBeforeScrollTop?: number;
  displayMode?: CalendarDisplayMode;
  components?: ObjectOrChildModel<CalendarComponent>[];
  selectedComponent?: CalendarComponent | string;
  displayCondensed?: boolean;
  selectedDate?: Date | string;
  showDisplayModeSelection?: boolean;
  rangeSelectionAllowed?: boolean;
  calendars?: CalendarDescriptor[];
  title?: string;
  useOverflowCells?: boolean;
  calendarToggleListWidth?: number;
  calendarToggleYearWidth?: number;
  menuInjectionTarget?: GroupBox;
  menus?: ObjectOrChildModel<Menu>[];
  defaultMenuTypes?: string[];
}
