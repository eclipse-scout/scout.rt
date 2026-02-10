/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Calendar, CalendarComponent, EnumObject, Point, ViewportScroller} from '../index';

export type CalendarDisplayMode = EnumObject<typeof Calendar.DisplayMode>;
export type CalendarMenuType = EnumObject<typeof Calendar.MenuType>;
export type CalendarDirection = EnumObject<typeof Calendar.Direction>;
export type CalendarMoveData = {
  event?: JQuery.MouseEventBase;
  cancel?: () => void;
  cancelled?: boolean;
  moving?: boolean;
  $movePart?: JQuery;
  component?: CalendarComponent;
  containerOffset?: JQuery.Coordinates;
  containerScrollPosition?: Point;
  startCursorPosition?: Point;
  currentCursorPosition?: Point;
  viewportScroller?: ViewportScroller;
  rafId?: number;
  onMove?: (event: JQuery.MouseMoveEvent) => void;
  onUp?: (event: JQuery.MouseUpEvent) => void;
  /**
   * Describes the X position of the virtual (preview) component when dragging.
   * X means the x-th day of the week, starting on Monday.
   * This means when `x = 3`, Wednesday is referenced.
   */
  virtualX?: number;
  /**
   * Describes the Y position of the virtual component when dragging.
   * Be aware that Y describes different properties, depending on the view mode.
   *
   * - MONTH: Y refers to the y-th week of the month
   * - WEEK, WORK_WEEK, DAY: Y refers to the y-th minute in the day.
   */
  virtualY?: number;
  /**
   * The virtual resource does only come into play when on day. This allows dragging the component between resources.
   */
  virtualResourceId?: string;
  /**
   * The virtual offset stores the distance between the cursor and the top of the appointment.
   * This allows to drag a larger component in the middle.
   */
  virtualOffset?: number;
};
