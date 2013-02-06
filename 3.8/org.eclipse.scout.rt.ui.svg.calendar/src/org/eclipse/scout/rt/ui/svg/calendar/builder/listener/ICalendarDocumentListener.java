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
package org.eclipse.scout.rt.ui.svg.calendar.builder.listener;

import java.util.Date;
import java.util.EventListener;

import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;

public interface ICalendarDocumentListener extends EventListener {

  void displayModeMenuActivated(int displayMode);

  void popupMenuActivated();

  void selectionChanged(Date selectedDate, CalendarComponent selectedComponent);

  void visibleRangeChanged(Date start, Date end);
}
