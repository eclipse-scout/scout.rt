/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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

  void fireComponentMoveFromUI(CalendarComponent comp, Date newDate);

  void fireReloadFromUI();

  void setViewRangeFromUI(Range<Date> viewRange);

  void setViewRangeFromUI(Date from, Date to);

  void setDisplayModeFromUI(int displayMode);

  void setSelectionFromUI(Date date, CalendarComponent comp);

  void setSelectedDateFromUI(Date date);

}
