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


import java.util.Date;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.scout.rt.ui.swt.basic.calendar.widgets.AbstractCell;

/**
 * Wrapper for a calendar item with some adjustments to the start and end time,
 * etc.
 */

@SuppressWarnings("unchecked")

/* corresponds to inner class LargeCalendarCell.ItemWrapper in
 * the swt implementation */
public class CalendarItemContainer implements Comparable, CalendarConstants {
  protected Object m_item;
  protected boolean m_labeled;
  protected long m_fromRelative, m_toRelative;// truncated to actual DAY
  protected Rectangle m_bounds;
  protected boolean m_fullDay;
  protected float m_x0, m_x1;

  /** reference to the day cell */
  protected AbstractCell m_cell;

  public CalendarItemContainer(Object item, AbstractCell cell) {
    m_cell = cell;

    m_item = item;
    m_bounds = new Rectangle(1, 2, 3, 4);
    long repTimeOfDayStart = AbstractCell.getTimeOfDayMillis(m_cell.getDate().getTime());
    // TODO: why + 1
    long displayInterval = HOUR_MILLIS
        * (DAY_TIMELINE_END_TIME /* TODO need this? + 1 */- DAY_TIMELINE_START_TIME);
    CalendarModel model = m_cell.getCalendar().getModel();
    //
    m_fullDay = model.isFullDay(item);
    //
    m_fromRelative = AbstractCell.getTimeOfDayMillis(model.getFromDate(item)) - repTimeOfDayStart
        - HOUR_MILLIS * DAY_TIMELINE_START_TIME;
    if (m_fromRelative < 0)
      m_fromRelative = 0;
    if (m_fromRelative > displayInterval)
      m_fromRelative = displayInterval;
    //
    Date d2 = model.getToDate(item);
    if (d2 == null) {
      m_toRelative = m_fromRelative;
    } else {
      m_toRelative = AbstractCell.getTimeOfDayMillis(d2) - repTimeOfDayStart - HOUR_MILLIS
          * DAY_TIMELINE_START_TIME;
    }
    if (m_toRelative < 0)
      m_toRelative = 0;
    if (m_toRelative > displayInterval)
      m_toRelative = displayInterval;
    // check end of day set
    if (m_fromRelative >= displayInterval - HOUR_MILLIS
        && m_toRelative >= displayInterval - DAY_TIMELINE_END_TIME * HOUR_MILLIS) {
      m_fromRelative = displayInterval - HOUR_MILLIS;
      m_toRelative = displayInterval;
    }
    // check emty set
    if (m_toRelative == m_fromRelative) {
      m_toRelative = m_fromRelative + HOUR_MILLIS;
    }
  }

  public void setHorizontalExtents(float x0, float x1) {
    m_x0 = x0;
    m_x1 = x1;
  }

  public float getX0() {
    return m_x0;
  }

  public float getX1() {
    return m_x1;
  }

  public Object getItem() {
    return m_item;
  }

  public void setBounds(int x, int y, int w, int h) {
    m_bounds.x = x;
    m_bounds.y = y;
    m_bounds.width = w;
    m_bounds.height = h;
  }

  public boolean contains(int x, int y) {
    return m_bounds.contains(x, y);
  }

  public void setLabeled(boolean on) {
    m_labeled = on;
  }

  public boolean isLabeled() {
    return m_labeled;
  }

  public boolean isTimed() {
    return !m_fullDay;
  }

  @Override
  public String toString() {
    return m_item.toString();
  }

  public boolean intersects(CalendarItemContainer other) {
    if (this.m_fromRelative <= other.m_fromRelative && other.m_fromRelative < this.m_toRelative)
      return true;
    else if (this.m_fromRelative < other.m_toRelative && other.m_toRelative <= this.m_toRelative)
      return true;
    return false;
  }

  public long getFromRelative() {
    return m_fromRelative;
  }

  public long getToRelative() {
    return m_toRelative;
  }

  // wrappers
  public Color getColor() {
    Color c = m_cell.getCalendar().getModel().getColor(m_item);
    if (c == null)
      c = new Color(SwtColors.getStandardDisplay(),m_cell.getBackground().getRed(), m_cell.getBackground().getGreen(), m_cell.getBackground().getBlue());
    return c;
  }

  protected Date getCompareDate() {
    return m_cell.getCalendar().getModel().getFromDate(m_item);
  }

  protected Integer getCompareId() {
    return Integer.valueOf(m_item.hashCode());
  }

  @Override
  public int compareTo(Object o) {
    if (m_item instanceof Comparable) {
      return ((Comparable<Object>) m_item).compareTo(((CalendarItemContainer) o).getItem());
    } else {
      int i = this.getCompareDate().compareTo(((CalendarItemContainer) o).getCompareDate());
      if (i == 0)
        i = this.getCompareId().compareTo(((CalendarItemContainer) o).getCompareId());
      return i;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null && m_item == null) {
      return true;
    }
    if (o == null || m_item == null || !(o.getClass().isAssignableFrom(m_item.getClass()))) {
      return false;
    }
    return m_item.equals(o);
  }

  @Override
  public int hashCode() {
    return m_item.hashCode();
  }

}// end class
