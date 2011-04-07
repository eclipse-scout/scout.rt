/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.basic.calendar;

import java.util.Collection;
import java.util.Date;

import org.eclipse.swt.graphics.Color;

/** 
 * Interface representing basic operation we can execute
 * on a calendar.
 *
 */
public interface ICalendarModel {
	Collection<CalendarItemContainer> getItemsAt (Date dateTruncatedToDay);
	String getTooltip (Object item, Date representedDate);
	String getLabel (Object item, Date representedDate);
	Date getFromDate (Object item);
	Date getToDate (Object item);
	Color getColor (Object item);
  boolean isFullDay (Object item);
//  TODO: implement drag'n drop
//  boolean isDraggable (Object item);
//  void updateItem (Object item, double delta);
}
