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
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class RecurrencePattern implements java.io.Serializable {
  private static final long serialVersionUID = 1L;
  //
  public static final int TYPE_DAILY = 119535;
  public static final int TYPE_WEEKLY = 119536;
  public static final int TYPE_MONTHLY = 119537;
  public static final int TYPE_MONTHLY_SPEC = 119538;
  public static final int TYPE_YEARLY = 119539;
  public static final int TYPE_YEARLY_SPEC = 119540;
  //
  public static final int INST_FIRST = 119530;
  public static final int INST_SECOND = 119531;
  public static final int INST_THIRD = 119532;
  public static final int INST_FOURTH = 119533;
  public static final int INST_LAST = 119534;
  public static final int INST_NONE = 0;
  //
  /**
   * same as {@value Calendar#SUNDAY}
   */
  public static final int MASK_SUN = 1;
  /**
   * same as {@value Calendar#MONDAY}
   */
  public static final int MASK_MON = 2;
  /**
   * same as {@value Calendar#TUESDAY}
   */
  public static final int MASK_TUE = 4;
  /**
   * same as {@value Calendar#WEDNESDAY}
   */
  public static final int MASK_WED = 8;
  /**
   * same as {@value Calendar#THURSDAY}
   */
  public static final int MASK_THU = 16;
  /**
   * same as {@value Calendar#FRIDAY}
   */
  public static final int MASK_FRI = 32;
  /**
   * same as {@value Calendar#SATURDAY}
   */
  public static final int MASK_SAT = 64;

  private long m_lastModified;
  private boolean m_regenerate;
  private int m_startTimeMinutes;
  private int m_endTimeMinutes;
  private int m_durationMinutes;
  private Date m_firstDate;
  private Date m_lastDate;
  private int m_occurrences;
  private boolean m_noEndDate;
  private int m_type;
  private int m_interval;
  private int m_instance;
  private int m_dayOfWeekBits;
  /**
   * value startw with 1
   */
  private int m_dayOfMonth;
  /**
   * value startw with 1
   */
  private int m_monthOfYear;
  private ArrayList<RecurrenceException> m_recurrenceExceptions = new ArrayList<RecurrenceException>();

  public long getLastModified() {
    return m_lastModified;
  }

  public void setLastModified(long b) {
    m_lastModified = b;
  }

  public void setRegenerate(boolean v) {
    m_regenerate = v;
  }

  public boolean isRegenerate() {
    return m_regenerate;
  }

  public void setStartTimeMinutes(int v) {
    m_startTimeMinutes = v;
  }

  public int getStartTimeMinutes() {
    return m_startTimeMinutes;
  }

  public void setEndTimeMinutes(int v) {
    m_endTimeMinutes = v;
  }

  public int getEndTimeMinutes() {
    return m_endTimeMinutes;
  }

  public void setStartTimeAsDate(Date d) {
    if (d != null) {
      Calendar c = Calendar.getInstance();
      c.setTime(d);
      m_startTimeMinutes = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }
  }

  public Date getStartTimeAsDate() {
    Calendar c = Calendar.getInstance();
    c.set(2000, 1, 1, m_startTimeMinutes / 60, m_startTimeMinutes % 60, 0);
    return c.getTime();
  }

  public void setEndTimeAsDate(Date d) {
    if (d != null) {
      Calendar c = Calendar.getInstance();
      c.setTime(d);
      m_endTimeMinutes = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }
  }

  public Date getEndTimeAsDate() {
    Calendar c = Calendar.getInstance();
    c.set(2000, 1, 1, m_endTimeMinutes / 60, m_endTimeMinutes % 60, 0);
    return c.getTime();
  }

  public void setDurationMinutes(int v) {
    if (v > 0x10000000) {
      v = 0;
    }
    m_durationMinutes = v;
  }

  public int getDurationMinutes() {
    return m_durationMinutes;
  }

  public void setFirstDate(Date v) {
    m_firstDate = v;
  }

  public Date getFirstDate() {
    return m_firstDate;
  }

  public void setLastDate(Date v) {
    m_lastDate = v;
  }

  public Date getLastDate() {
    return m_lastDate;
  }

  public void setOccurrences(int v) {
    m_occurrences = v;
  }

  public int getOccurrences() {
    return m_occurrences;
  }

  public void setNoEndDate(boolean v) {
    m_noEndDate = v;
  }

  public boolean getNoEndDate() {
    return m_noEndDate;
  }

  public void setType(int v) {
    if (v < TYPE_DAILY || v > TYPE_YEARLY_SPEC) {
      throw new IllegalArgumentException("type (" + v + ") must be in [TYPE_DAILY,TYPE_YEARLY_SPEC]");
    }
    m_type = v;
  }

  public int getType() {
    return m_type;
  }

  public void setInterval(int v) {
    m_interval = v;
  }

  public int getInterval() {
    return m_interval;
  }

  public void setInstance(int v) {
    if (v != 0 && (v < INST_FIRST || v > INST_LAST)) {
      throw new IllegalArgumentException("instance (" + v + ") must be in [INST_FIRST,INST_LAST]");
    }
    m_instance = v;
  }

  public int getInstance() {
    return m_instance;
  }

  public void setDayOfWeek(int bits) {
    m_dayOfWeekBits = bits;
  }

  public int getDayOfWeek() {
    return m_dayOfWeekBits;
  }

  /**
   * value startw with 1
   */
  public void setDayOfMonth(int v) {
    m_dayOfMonth = v;
  }

  /**
   * value startw with 1
   */
  public int getDayOfMonth() {
    return m_dayOfMonth;
  }

  /**
   * value startw with 1
   */
  public void setMonthOfYear(int v) {
    m_monthOfYear = v;
  }

  /**
   * value startw with 1
   */
  public int getMonthOfYear() {
    return m_monthOfYear;
  }

  public List<RecurrenceException> getRecurrenceExceptions() {
    return m_recurrenceExceptions;
  }

  public void addRecurrenceException(RecurrenceException ex) {
    m_recurrenceExceptions.add(ex);
  }

  /**
   * Recurrence series provider
   */
  public Set<Date> createStartDates(Date startDate, Date endDate) {
    if (m_firstDate != null && m_firstDate.after(startDate)) {
      startDate = m_firstDate;
    }
    if (m_lastDate != null && m_lastDate.before(endDate)) {
      endDate = m_lastDate;
    }
    // startDate has time=00:00:00
    startDate = applyTime(startDate, 0, 0, 0);
    // endDate has time=23:59:59
    endDate = applyTime(endDate, 23, 59, 59);
    // build first date/time
    int min = getStartTimeMinutes() % (24 * 60);
    GregorianCalendar startRecCal = new GregorianCalendar();
    startRecCal.setTime(applyTime(m_firstDate, min / 60, min % 60, 0));
    TreeSet<Date> list = new TreeSet<Date>();
    if (m_interval <= 0) {
      m_interval = 1;
    }
    switch (getType()) {
      case TYPE_DAILY: {
        createDailyStartDates(startRecCal, startDate, endDate, list);
        break;
      }
      case TYPE_WEEKLY: {
        createWeeklyStartDates(startRecCal, startDate, endDate, list);
        break;
      }
      case TYPE_MONTHLY: {
        createMonthlyStartDates(startRecCal, startDate, endDate, list);
        break;
      }
      case TYPE_MONTHLY_SPEC: {
        createMonthlySpecStartDates(startRecCal, startDate, endDate, list);
        break;
      }
      case TYPE_YEARLY: {
        createYearlyStartDates(startRecCal, startDate, endDate, list);
        break;
      }
      case TYPE_YEARLY_SPEC: {
        createYearlySpecStartDates(startRecCal, startDate, endDate, list);
        break;
      }
    }
    // remove exceptions
    for (Iterator<RecurrenceException> it = m_recurrenceExceptions.iterator(); it.hasNext();) {
      RecurrenceException recEx = it.next();
      Calendar cal = Calendar.getInstance();
      cal.setTime(recEx.getOriginalStartDate());
      cal.set(Calendar.HOUR, 0);
      cal.set(Calendar.HOUR_OF_DAY, min / 60);
      cal.set(Calendar.MINUTE, min % 60);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      list.remove(cal.getTime());
    }
    return list;
  }

  private void createDailyStartDates(Calendar cal, Date startDate, Date endDate, Set<Date> list) {
    // uses: occurrences,interval(only if lastdate set)
    // loop from beginning of series (not beginning of interval!)
    Date d = cal.getTime();
    int count = 0;// of occurences
    while (d.compareTo(endDate) <= 0) {
      // valid
      count++;
      // add, if in requested interval
      if (d.compareTo(startDate) >= 0) {
        list.add(d);
      }
      // next
      cal.add(Calendar.DATE, m_interval);
      d = cal.getTime();
      /**
       * @rn sle, 05.07.2006,
       * @since Build 204 Bugfix to ensure value 'occurrences' is realy true reported by MBR, ORS / SSC, BAP PK
       */
      if (!m_noEndDate && m_lastDate == null && count >= m_occurrences) {
        break;
      }
    }
  }

  private boolean isInWeekMask(int bitMask, Calendar cal) {
    return (bitMask & (1 << (cal.get(Calendar.DAY_OF_WEEK) - 1))) != 0;
  }

  private void createWeeklyStartDates(Calendar cal, Date startDate, Date endDate, Set<Date> list) {
    // uses: occurrences,interval(only if lastdate set),dayOfWeekBits
    // loop from beginning of series (not beginning of interval!)
    Date d = cal.getTime();
    int count = 0;// of occurences
    while (d.compareTo(endDate) <= 0) {
      // check weekdays
      Calendar weekCal = Calendar.getInstance();
      weekCal.setTime(d);
      for (int i = 0; i < 7; i++) {
        if (isInWeekMask(m_dayOfWeekBits, weekCal)) {
          // valid
          Date dWeek = weekCal.getTime();
          count++;
          // add, if in requested interval
          if (dWeek.compareTo(startDate) >= 0) {
            list.add(dWeek);
          }
          /**
           * @rn sle, 05.07.2006,
           * @since Build 204 Bugfix to ensure value 'occurrences' is realy true reported by MBR, ORS / SSC, BAP PK
           */
          if (!m_noEndDate && m_lastDate == null && count >= m_occurrences) {
            break;
          }
        }
        // next weekday
        weekCal.add(Calendar.DATE, 1);
      }
      // next
      cal.add(Calendar.WEEK_OF_YEAR, m_interval);
      d = cal.getTime();
      /**
       * @rn sle, 05.07.2006,
       * @since Build 204 Bugfix to ensure value 'occurrences' is realy true reported by MBR, ORS / SSC, BAP PK
       */
      if (!m_noEndDate && m_lastDate == null && count >= m_occurrences) {
        break;
      }
    }
  }

  private void createMonthlyStartDates(Calendar cal, Date startDate, Date endDate, Set<Date> list) {
    // uses: occurrences,interval(only if lastdate set),dayOfMonth
    // loop from beginning of series (not beginning of interval!)
    Date d = cal.getTime();
    int count = 0;// of occurences
    while (d.compareTo(endDate) <= 0) {
      // check dayOfMonth
      Calendar monthCal = Calendar.getInstance();
      monthCal.setTime(d);
      // shift day back if necessary (31,30,29)
      int dayOfCurrentMonth = m_dayOfMonth;
      if (dayOfCurrentMonth > monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
        dayOfCurrentMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH);
      }
      for (int i = 0; i < 31; i++) {
        if (monthCal.get(Calendar.DAY_OF_MONTH) == dayOfCurrentMonth) {
          // valid
          Date dMonth = monthCal.getTime();
          count++;
          // add, if in requested interval
          if (dMonth.compareTo(startDate) >= 0) {
            list.add(dMonth);
          }
          break;
        }
        // next monthday
        monthCal.add(Calendar.DAY_OF_MONTH, 1);
      }
      // next
      cal.add(Calendar.MONTH, m_interval);
      d = cal.getTime();
      /**
       * @rn sle, 05.07.2006,
       * @since Build 204 Bugfix to ensure value 'occurrences' is realy true reported by MBR, ORS / SSC, BAP PK
       */
      if (!m_noEndDate && m_lastDate == null && count >= m_occurrences) {
        break;
      }
    }
  }

  private void createMonthlySpecStartDates(Calendar cal, Date startDate, Date endDate, Set<Date> list) {
    // uses: occurrences,interval(only if lastdate set),instance,dayOfWeekBits
    // loop from beginning of series (not beginning of interval!)
    Date d = cal.getTime();
    int count = 0;// of occurences
    int inst = 0;
    switch (m_instance) {
      case INST_FIRST:
        inst = 1;
        break;
      case INST_SECOND:
        inst = 2;
        break;
      case INST_THIRD:
        inst = 3;
        break;
      case INST_FOURTH:
        inst = 4;
        break;
      case INST_LAST:
        inst = 1000000;
        break;
    }
    while (d.compareTo(endDate) <= 0) {
      // check dayOfMonth
      Calendar monthCal = Calendar.getInstance();
      monthCal.setTime(d);
      int instCount = 0;
      Date lastValid = null;
      for (int i = 1, ni = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH); i <= ni; i++) {
        monthCal.set(Calendar.DATE, i);
        if (isInWeekMask(m_dayOfWeekBits, monthCal)) {
          lastValid = monthCal.getTime();
          instCount++;
          if (instCount == inst) {
            break;
          }
        }
      }
      if (lastValid != null && lastValid.compareTo(m_firstDate) >= 0) {
        // valid
        // Date dMonth=monthCal.getTime();
        count++;
        // add, if in requested interval
        if (lastValid.compareTo(startDate) >= 0) {
          list.add(lastValid);
        }
      }
      // next
      cal.add(Calendar.MONTH, m_interval);
      d = cal.getTime();
      /**
       * @rn sle, 05.07.2006,
       * @since Build 204 Bugfix to ensure value 'occurrences' is realy true reported by MBR, ORS / SSC, BAP PK
       */
      if (!m_noEndDate && m_lastDate == null && count >= m_occurrences) {
        break;
      }
    }
  }

  private void createYearlyStartDates(Calendar cal, Date startDate, Date endDate, Set<Date> list) {
    // uses: occurrences,interval(only if lastdate set),dayOfMonth,monthOfYear
    // loop from beginning of series (not beginning of interval!)
    Date d = cal.getTime();
    int count = 0;// of occurences
    while (d.compareTo(endDate) <= 0) {
      // check monthOfYear
      Calendar monthCal = Calendar.getInstance();
      monthCal.setTime(d);
      monthCal.set(Calendar.DAY_OF_MONTH, 1);
      monthCal.set(Calendar.MONTH, m_monthOfYear - 1);
      monthCal.set(Calendar.DAY_OF_MONTH, m_dayOfMonth);
      if (monthCal.getTime().compareTo(m_firstDate) >= 0) {
        // valid
        Date dMonth = monthCal.getTime();
        count++;
        // add, if in requested interval
        if (dMonth.compareTo(startDate) >= 0) {
          list.add(dMonth);
        }
      }
      // next
      cal.add(Calendar.YEAR, m_interval);
      d = cal.getTime();
      /**
       * @rn sle, 05.07.2006,
       * @since Build 204 Bugfix to ensure value 'occurrences' is realy true reported by MBR, ORS / SSC, BAP PK
       */
      if (!m_noEndDate && m_lastDate == null && count >= m_occurrences) {
        break;
      }
    }
  }

  private void createYearlySpecStartDates(Calendar cal, Date startDate, Date endDate, Set<Date> list) {
    // uses: occurrences,interval(only if lastdate
    // set),instance,dayOfWeekBits,monthOfYear
    // loop from beginning of series (not beginning of interval!)
    Date d = cal.getTime();
    int count = 0;// of occurences
    int inst = 0;
    switch (m_instance) {
      case INST_FIRST:
        inst = 1;
        break;
      case INST_SECOND:
        inst = 2;
        break;
      case INST_THIRD:
        inst = 3;
        break;
      case INST_FOURTH:
        inst = 4;
        break;
      case INST_LAST:
        inst = 100000;
        break;
    }
    while (d.compareTo(endDate) <= 0) {
      // check monthOfYear
      Calendar monthCal = Calendar.getInstance();
      monthCal.setTime(d);
      monthCal.set(Calendar.DAY_OF_MONTH, 1);
      monthCal.set(Calendar.MONTH, m_monthOfYear - 1);
      int instCount = 0;
      Date lastValid = null;
      for (int i = 1, ni = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH); i <= ni; i++) {
        monthCal.set(Calendar.DATE, i);
        if (isInWeekMask(m_dayOfWeekBits, monthCal)) {
          lastValid = monthCal.getTime();
          instCount++;
          if (instCount == inst) {
            break;
          }
        }
      }
      if (lastValid != null && lastValid.compareTo(m_firstDate) >= 0) {
        // valid
        count++;
        // add, if in requested interval
        if (lastValid.compareTo(startDate) >= 0) {
          list.add(lastValid);
        }
      }
      // next
      cal.add(Calendar.YEAR, m_interval);
      d = cal.getTime();
      /**
       * @rn sle, 05.07.2006,
       * @since Build 204 Bugfix to ensure value 'occurrences' is realy true reported by MBR, ORS / SSC, BAP PK
       */
      if (!m_noEndDate && m_lastDate == null && count >= m_occurrences) {
        break;
      }
    }
  }

  public static Date applyTime(Date d, int hour, int minute, int second) {
    if (d != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(d);
      cal.set(Calendar.HOUR, 0);
      cal.set(Calendar.HOUR_OF_DAY, hour);
      cal.set(Calendar.MINUTE, minute);
      cal.set(Calendar.SECOND, second);
      cal.set(Calendar.MILLISECOND, 0);
      d = cal.getTime();
    }
    return d;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder("Pattern[");
    buf.append("startTimeMinutes=" + m_startTimeMinutes + ",");
    buf.append("endTimeMinutes=" + m_endTimeMinutes + ",");
    buf.append("durationMinutes=" + m_durationMinutes + ",");
    buf.append("firstDate=" + m_firstDate + ",");
    buf.append("lastDate=" + m_lastDate + ",");
    buf.append("occurrences=" + m_occurrences + ",");
    buf.append("type=" + m_type + ",");
    buf.append("interval=" + m_interval + ",");
    buf.append("instance=" + m_instance + ",");
    buf.append("dayOfWeekBits=" + Integer.toBinaryString(m_dayOfWeekBits) + ",");
    buf.append("dayOfMonth=" + m_dayOfMonth + ",");
    buf.append("regenerate=" + m_regenerate + ",");
    buf.append("]");
    return buf.toString();
  }

}
