/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.calendar;

import java.util.Date;

import org.eclipse.scout.commons.Range;

public interface ICalendarUIFacade {

  boolean isUIProcessing();

  void fireComponentActionFromUI();

  void fireComponentMovedFromUI(CalendarComponent comp, Date newDate);

  void fireReloadFromUI();

  /**
   * @param dateRange
   */
  void setVisibleRangeFromUI(Range<Date> dateRange);

  void setVisibleRangeFromUI(Date minDate, Date maxDate);

  void setSelectionFromUI(Date date, CalendarComponent comp);

}
