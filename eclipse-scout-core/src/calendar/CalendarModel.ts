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
import {CalendarComponent, CalendarDisplayMode, GroupBox, Menu, ObjectOrChildModel, WidgetModel} from '../index';

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
  title?: string;
  useOverflowCells?: boolean;
  calendarToggleListWidth?: number;
  calendarToggleYearWidth?: number;
  menuInjectionTarget?: GroupBox;
  menus?: ObjectOrChildModel<Menu>[];
}
