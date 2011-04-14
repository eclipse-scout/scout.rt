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
package org.eclipse.scout.rt.ui.swing.ext.calendar;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class DefaultCalendarModel implements CalendarModel {
  private HashMap<Date, ArrayList<Object>> m_map = new HashMap<Date, ArrayList<Object>>();

  public void addItem(SimpleCalendarItem item) {
    Date a = getIndexDate(item.getFromDate(), null);
    Date b = getIndexDate(item.getToDate(), a);
    Calendar cal = Calendar.getInstance();
    cal.setTime(a);
    while (a.compareTo(b) <= 0) {
      ArrayList<Object> items = m_map.get(a);
      if (items == null) {
        items = new ArrayList<Object>();
        m_map.put(a, items);
      }
      items.add(item);
      cal.add(Calendar.DATE, 1);
      a = cal.getTime();
    }
  }

  @Override
  public void moveItem(Object item, Date newDate) {

  }

  private Date getIndexDate(Date d, Date defaultDate) {
    if (d == null) return defaultDate;
    return DateChooser.truncDate(d);
  }

  @Override
  public Collection<Object> getItemsAt(Date dateTruncatedToDay) {
    return m_map.get(dateTruncatedToDay);
  }

  @Override
  public String getTooltip(Object item, Date d) {
    SimpleCalendarItem ci = (SimpleCalendarItem) item;
    return ci.getTooltipText();
  }

  @Override
  public String getLabel(Object item, Date d) {
    SimpleCalendarItem ci = (SimpleCalendarItem) item;
    return ci.getLabel();
  }

  @Override
  public Date getFromDate(Object item) {
    SimpleCalendarItem ci = (SimpleCalendarItem) item;
    return ci.getFromDate();
  }

  @Override
  public Date getToDate(Object item) {
    SimpleCalendarItem ci = (SimpleCalendarItem) item;
    return ci.getToDate();
  }

  @Override
  public Color getColor(Object item) {
    SimpleCalendarItem ci = (SimpleCalendarItem) item;
    return ci.getColor();
  }

  @Override
  public boolean isFullDay(Object item) {
    SimpleCalendarItem ci = (SimpleCalendarItem) item;
    return ci.isFullDay();
  }

  @Override
  public boolean isDraggable(Object item) {
    SimpleCalendarItem ci = (SimpleCalendarItem) item;
    return ci.isDraggable();
  }
}
