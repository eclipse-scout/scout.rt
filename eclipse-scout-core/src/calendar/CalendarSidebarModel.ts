/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {CalendarSidebarSplitter, CalendarsPanel, WidgetModel, YearPanel} from '../index';

export interface CalendarSidebarModel extends WidgetModel {
  /**
   * Year panel widget
   */
  yearPanel?: YearPanel;
  /**
   * Splitter widget
   */
  splitter?: CalendarSidebarSplitter;
  /**
   * Calendars panel widget
   */
  calendarsPanel?: CalendarsPanel;
  /**
   * Defines, whether the calendars panel is displayable.
   * For example, the calendars panel is not visible when the calendar widget has only one calendar descriptor.
   */
  calendarsPanelDisplayable?: boolean;
}
