/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CalendarComponent, CalendarDisplayMode, CalendarResourceDo, GroupBox, Menu, ObjectOrChildModel, WidgetModel} from '../index';

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
  /**
   * This property enables support for multiple resources on the calendar widget
   */
  resources?: CalendarResourceDo[];
  /**
   * Indicates which resources is currently selected
   */
  selectedResource?: CalendarResourceDo;
  title?: string;
  useOverflowCells?: boolean;
  calendarToggleListWidth?: number;
  calendarToggleYearWidth?: number;
  menuInjectionTarget?: GroupBox;
  menus?: ObjectOrChildModel<Menu>[];
  defaultMenuTypes?: string[];
  /**
   * Indicates, if the sidebar is shown
   */
  showCalendarSidebar?: boolean;
  /**
   * Indicates, if the resource panel is shown
   */
  showResourcePanel?: boolean;
  /**
   * Indicates, if the list panel is expanded
   */
  showListPanel?: boolean;
}
