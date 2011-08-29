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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarAppointment;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarTask;

public class CalendarComponent {
  private ICalendar m_calendar;
  private ICalendarItemProvider m_producer;
  private ICalendarItem m_item;
  private ICell m_cell;
  // cache
  private Date m_fromDate;
  private Date m_toDate;
  private Date[] m_coveredDays;
  private boolean m_fullDay;

  protected CalendarComponent(ICalendar calendar, ICalendarItemProvider producer, ICalendarItem item, ICell cell) {
    m_calendar = calendar;
    m_producer = producer;
    m_item = item;
    m_cell = cell;
    // cache FROM date
    Date d = null;
    if (m_item instanceof ICalendarAppointment) {
      ICalendarAppointment app = (ICalendarAppointment) m_item;
      d = DateUtility.min(app.getStart(), app.getEnd());
    }
    if (m_item instanceof ICalendarTask) {
      ICalendarTask task = (ICalendarTask) m_item;
      d = DateUtility.min(task.getStart(), task.getDue(), task.getComplete());
    }
    //
    if (d == null) {
      d = new Date(0);
    }
    m_fromDate = d;
    // cache TO date
    d = null;
    if (m_item instanceof ICalendarAppointment) {
      ICalendarAppointment app = (ICalendarAppointment) m_item;
      d = DateUtility.max(app.getStart(), app.getEnd());
    }
    if (m_item instanceof ICalendarTask) {
      ICalendarTask task = (ICalendarTask) m_item;
      d = DateUtility.max(task.getStart(), task.getDue(), task.getComplete());
    }
    //
    if (d == null) {
      d = new Date(0);
    }
    m_toDate = d;
    // cache covered days
    ArrayList<Date> dayList = new ArrayList<Date>();
    Calendar a = Calendar.getInstance();
    a.setTime(m_fromDate);
    DateUtility.truncCalendar(a);
    Calendar b = Calendar.getInstance();
    b.setTime(m_toDate);
    DateUtility.truncCalendar(b);
    while (a.compareTo(b) <= 0) {
      dayList.add(a.getTime());
      a.add(Calendar.DATE, 1);
    }
    m_coveredDays = DateUtility.getCoveredDays(m_fromDate, m_toDate);
    // cache full day flag
    if (m_item instanceof ICalendarAppointment) {
      m_fullDay = ((ICalendarAppointment) m_item).isFullDay();
    }
    else if (m_item instanceof ICalendarTask) {
      m_fullDay = true;
    }
    else {
      m_fullDay = true;
    }
  }

  public ICell getCell() {
    return m_cell;
  }

  public ICalendarItem getItem() {
    return m_item;
  }

  /**
   * producer that created the item in this component
   */
  public ICalendarItemProvider getProvider() {
    return m_producer;
  }

  public ICalendar getCalendar() {
    return m_calendar;
  }

  /**
   * Convenience for getting the start date of the item Guaranteed to be never
   * null
   */
  public Date getFromDate() {
    return m_fromDate;
  }

  /**
   * Convenience for getting the end date of the item Guaranteed to be never
   * null
   */
  public Date getToDate() {
    return m_toDate;
  }

  /**
   * Convenience for getting the specific (composite) displayed label of an item
   * for a specific day This includes the start date, the end date and the label
   * of the item
   */
  public String getLabel(Date day) {
    /* day=DateUtility.truncDate(day); */
    switch (m_calendar.getDisplayMode()) {
      case ICalendar.DISPLAY_MODE_MONTH: {
        return m_cell.getText();
      }
      case ICalendar.DISPLAY_MODE_WEEK:
      case ICalendar.DISPLAY_MODE_WORKWEEK: {
        return m_cell.getText();
      }
      case ICalendar.DISPLAY_MODE_DAY: {
        return m_cell.getText();
      }
      default: {
        return m_cell.getText();
      }
    }
  }

  /**
   * Convenience for getting the specific (composite) displayed tooltip of an
   * item for a specific day This includes the start date, the end date and the
   * label of the item
   */
  public String getTooltip(Date day) {
    day = DateUtility.truncDate(day);
    String s = createDayTooltip(day);
    String s2 = m_cell.getTooltipText();
    if (s2 != null && s2.length() > 0) {
      s = s + "\n" + s2;
    }
    return s;
  }

  /**
   * Convenience for getting all days this item is covering the dates returned
   * have all time 00:00:00
   */
  public Date[] getCoveredDays() {
    return m_coveredDays;
  }

  private String createDayTooltip(Date dayTruncated) {
    Date a = getFromDate();
    Date b = getToDate();
    DateFormat timeFmt = m_calendar.getDateTimeFormatFactory().getHourMinute();
    DateFormat dayFmt = m_calendar.getDateTimeFormatFactory().getDayMonth(DateFormat.MEDIUM);
    if (m_coveredDays.length == 1) {
      if (isFullDay()) {
        return m_cell.getText();
      }
      else if (DateUtility.equals(a, b)) {
        if (DateUtility.equals(a, dayTruncated)) {
          // the date is at 00:00 so probably time is irrelevant
          return m_cell.getText();
        }
        else {
          return timeFmt.format(a) + " " + m_cell.getText();
        }
      }
      else {
        return timeFmt.format(a) + "-" + timeFmt.format(b) + " " + m_cell.getText();
      }
    }
    else {// not just one day
      if (isFullDay()) {
        return dayFmt.format(a) + " - " + dayFmt.format(b) + "  " + m_cell.getText();
      }
      else {
        return dayFmt.format(a) + " " + timeFmt.format(a) + " - " + dayFmt.format(b) + " " + timeFmt.format(b) + "  " + m_cell.getText();
      }
    }
  }

  /**
   * Convenience for getting the full day flag (if applicable) of the item
   */
  public boolean isFullDay() {
    return m_fullDay;
  }

  /**
   * Convenience for getting the draggable property from the item producer
   */
  public boolean isDraggable() {
    return m_producer.isMoveItemEnabled();
  }
}
