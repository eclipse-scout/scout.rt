/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
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
  unitX?: number;
  unitY?: number;
  logicalX?: number;
  logicalY?: number;
  mode?: string;
  moving?: boolean;
  component?: CalendarComponent;
  containerOffset?: JQuery.Coordinates;
  containerScrollPosition?: Point;
  distance?: Point;
  startCursorPosition?: Point;
  currentCursorPosition?: Point;
  viewportScroller?: ViewportScroller;
  rafId?: number;
  onMove?: (event: JQuery.MouseMoveEvent) => void;
  onUp?: (event: JQuery.MouseUpEvent) => void;
};
