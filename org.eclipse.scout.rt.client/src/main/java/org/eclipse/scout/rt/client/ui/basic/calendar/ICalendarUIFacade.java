/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.util.Range;

public interface ICalendarUIFacade {

  boolean isUIProcessing();

  void fireComponentActionFromUI();

  void fireComponentMovedFromUI(CalendarComponent comp, Date newDate);

  void fireReloadFromUI();

  void setViewRangeFromUI(Range<Date> viewRange);

  void setViewRangeFromUI(Date from, Date to);

  void setDisplayModeFromUI(int displayMode);

  void setSelectionFromUI(Date date, CalendarComponent comp);

  // FIXME awe: (calendar) talk about selection in new Html UI, currently we can only select an entire day, but not a single item (= component)
  // depending on the outcome we must remove/refactor either this method or setSelectionFromUI
  void setSelectedDateFromUI(Date date);

}
