/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.calendar;

import java.util.Date;

import org.eclipse.scout.rt.platform.util.Range;

public interface ICalendarUIFacade {

  boolean isUIProcessing();

  void fireComponentActionFromUI();

  void fireComponentMoveFromUI(CalendarComponent comp, Date fromDate, Date toDate);

  void fireReloadFromUI();

  void setViewRangeFromUI(Range<Date> viewRange);

  void setViewRangeFromUI(Date from, Date to);

  void setSelectedRangeFromUI(Range<Date> selectedRange);

  void setSelectedRangeFromUI(Date from, Date to);

  void setDisplayModeFromUI(int displayMode);

  void setSelectionFromUI(Date date, CalendarComponent comp);

  void setSelectedDateFromUI(Date date);

  void fireAppLinkActionFromUI(String ref);

  void setCalendarVisibilityFromUI(Long calendarId, Boolean visible);
}
