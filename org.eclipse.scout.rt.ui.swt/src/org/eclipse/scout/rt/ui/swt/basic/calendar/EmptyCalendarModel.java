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
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;


/**
 * Void calendar model returning always dummy values.
 *
 */
public class EmptyCalendarModel implements CalendarModel{
	public Collection<CalendarComponent> getItemsAt(Date dateTruncatedToDay){return null;}
	public String getTooltip(Object item,Date d){return null;}
	public String getLabel(Object item,Date d){return null;}
	public Date getFromDate(Object item){return null;}
	public Date getToDate(Object item){return null;}
	public Color getColor(Object item){return null;}
	public boolean isFullDay(Object item){return false;}
	public boolean isDraggable(Object item){return false;}
	public void updateItem(Object item, double delta) {}
  public void moveItem(Object item, Date newDate){}
}
