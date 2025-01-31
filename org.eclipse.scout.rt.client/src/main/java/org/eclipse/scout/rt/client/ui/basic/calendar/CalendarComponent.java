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

import org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarAppointment;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarTask;

public class CalendarComponent implements Comparable<CalendarComponent> {
  private final ICalendar m_calendar;
  private final ICalendarItemProvider m_producer;
  private final ICalendarItem m_item;
  // cache
  private final Date m_fromDate;
  private final Date m_toDate;
  private final Range<Date> m_coveredDaysRange;
  private final boolean m_fullDay;

  protected CalendarComponent(ICalendar calendar, ICalendarItemProvider producer, ICalendarItem item) {
    m_calendar = calendar;
    m_producer = producer;
    m_item = item;
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
    m_coveredDaysRange = new Range<>(DateUtility.truncDate(m_fromDate), DateUtility.truncDate(m_toDate));

    // cache full day flag
    if (m_item instanceof ICalendarAppointment) {
      m_fullDay = ((ICalendarAppointment) m_item).isFullDay();
    }
    else {
      m_fullDay = true;
    }
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
   * Convenience for getting the start date of the item Guaranteed to be never null
   */
  public Date getFromDate() {
    return m_fromDate;
  }

  /**
   * Convenience for getting the end date of the item Guaranteed to be never null
   */
  public Date getToDate() {
    return m_toDate;
  }

  /**
   * Convenience for getting days range from start to end date
   */
  public Range<Date> getCoveredDaysRange() {
    return m_coveredDaysRange;
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

  @SuppressWarnings("unchecked")
  @Override
  public int compareTo(CalendarComponent o) {
    if (m_item instanceof Comparable) {
      return ((Comparable) m_item).compareTo(o.getItem());
    }
    else {
      int i = this.getFromDate().compareTo(o.getFromDate());
      if (i == 0) {
        String label1 = m_item.getSubject();
        String label2 = o.getItem().getSubject();
        i = StringUtility.compareIgnoreCase(label1, label2);
        if (i == 0) {
          i = Integer.compare(this.hashCode(), o.hashCode());
        }
      }
      return i;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_item == null) ? 0 : m_item.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CalendarComponent other = (CalendarComponent) obj;
    if (m_item == null) {
      if (other.m_item != null) {
        return false;
      }
    }
    else if (!m_item.equals(other.m_item)) {
      return false;
    }
    return true;
  }
}
