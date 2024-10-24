/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Calendar, CalendarItem, DateRange, JsonDateRange, WidgetModel} from '../index';

export interface CalendarComponentModel extends WidgetModel {
  parent?: Calendar;
  fromDate?: string;
  toDate?: string;
  /**
   * Selected is a GUI only property (the model doesn't have it)
   */
  selected?: boolean;
  fullDay?: boolean;
  fullDayIndex?: number;
  item?: CalendarItem;
  coveredDaysRange?: DateRange | JsonDateRange;
  /**
   * Specifies, how long (in minutes) a component should appear when no toDate is specified. Default is 60 (one hour).
   * This is a GUI only property.
   */
  defaultComponentDuration?: number;
}
