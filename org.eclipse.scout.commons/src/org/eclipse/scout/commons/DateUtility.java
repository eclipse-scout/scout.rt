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
package org.eclipse.scout.commons;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtility {

  private DateUtility() {
  }

  public static final long DAY_MILLIS = 24L * 3600L * 1000L;

  /**
   * format date with {@value DateFormat#DEFAULT} pattern
   */
  public static String formatDate(Date d) {
    if (d == null) {
      return "";
    }
    Locale loc = LocaleThreadLocal.get();
    return DateFormat.getDateInstance(DateFormat.DEFAULT, loc).format(d);
  }

  /**
   * format time with {@value DateFormat#SHORT} pattern
   */
  public static String formatTime(Date d) {
    if (d == null) {
      return "";
    }
    Locale loc = LocaleThreadLocal.get();
    return DateFormat.getTimeInstance(DateFormat.SHORT, loc).format(d);
  }

  /**
   * format time with {@value DateFormat#SHORT}, {@value DateFormat#SHORT} patterns
   */
  public static String formatDateTime(Date d) {
    if (d == null) {
      return "";
    }
    Locale loc = LocaleThreadLocal.get();
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, loc).format(d);
  }

  /**
   * format date with specific pattern as defined in {@link java.text.SimpleDateFormat}
   */
  public static String format(Date d, String pattern) {
    if (d == null || !StringUtility.hasText(pattern)) {
      return "";
    }
    Locale loc = LocaleThreadLocal.get();
    return new SimpleDateFormat(pattern, loc).format(d);
  }

  public static Date parse(String s, String pattern) {
    if (s == null) {
      return null;
    }
    try {
      Locale loc = LocaleThreadLocal.get();
      return new SimpleDateFormat(pattern, loc).parse(s);
    }
    catch (ParseException e) {
      throw new IllegalArgumentException("parse(\"" + s + "\",\"" + pattern + "\") failed", e);
    }
  }

  public static Date addHours(Date d, int hours) {
    if (d == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    cal.add(Calendar.HOUR_OF_DAY, hours);
    return cal.getTime();
  }

  /**
   * add count days days is truncated to second and can be negative
   */
  public static Date addDays(Date d, double count) {
    if (d == null) {
      return null;
    }
    int sec = (int) (count * 3600 * 24);
    int sign = 1;
    if (sec < 0) {
      sec = -sec;
      sign = -1;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    cal.add(Calendar.DATE, sign * (sec / 3600 / 24));
    cal.add(Calendar.HOUR_OF_DAY, sign * ((sec / 3600) % 24));
    cal.add(Calendar.MINUTE, sign * ((sec / 60) % 60));
    cal.add(Calendar.SECOND, sign * (sec % 60));
    return cal.getTime();
  }

  public static Date addMonths(Date d, int count) {
    if (d == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    cal.add(Calendar.MONTH, count);
    return cal.getTime();
  }

  public static Date addYears(Date d, int count) {
    if (d == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    cal.add(Calendar.YEAR, count);
    return cal.getTime();
  }

  /**
   * determines the day of the week
   * 
   * @param d
   * @return int with the the day of the week (sunday=1)
   */
  public static int getWeekday(Date d) {
    if (d == null) {
      return -1;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    int w = cal.get(Calendar.DAY_OF_WEEK);
    return w;
  }

  /**
   * truncate the date to a day with time 00:00:00.000
   */
  public static Date truncDate(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    truncCalendar(c);
    return new Date(c.getTime().getTime());
  }

  public static Date truncDateToMinute(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return new Date(c.getTime().getTime());
  }

  public static Date truncDateToSecond(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    c.set(Calendar.MILLISECOND, 0);
    return new Date(c.getTime().getTime());
  }

  /**
   * truncate the date to month
   */
  public static Date truncDateToWeek(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    truncCalendarToWeek(c, -1);
    return new Date(c.getTime().getTime());
  }

  /**
   * truncate the date to month
   */
  public static Date truncDateToMonth(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    truncCalendarToMonth(c);
    return new Date(c.getTime().getTime());
  }

  /**
   * truncate the date to year
   */
  public static Date truncDateToYear(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    truncCalendarToYear(c);
    return new Date(c.getTime().getTime());
  }

  /**
   * truncate the calendar to a day with time 00:00:00.000
   */
  public static void truncCalendar(Calendar c) {
    c.set(Calendar.HOUR, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
  }

  /**
   * truncate the calendar to week
   * 
   * @param adjustIncrement
   *          +1 or -1
   */
  public static void truncCalendarToWeek(Calendar c, int adjustIncrement) {
    if (adjustIncrement < -1) {
      adjustIncrement = -1;
    }
    if (adjustIncrement > 1) {
      adjustIncrement = 1;
    }
    if (adjustIncrement == 0) {
      adjustIncrement = -1;
    }
    c.set(Calendar.HOUR, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    int firstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
    while (c.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
      c.add(Calendar.DATE, adjustIncrement);
    }
  }

  /**
   * truncate the calendar to month
   */
  public static void truncCalendarToMonth(Calendar c) {
    c.set(Calendar.DATE, 1);
    c.set(Calendar.HOUR, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
  }

  /**
   * truncate the calendar to year
   */
  public static void truncCalendarToYear(Calendar c) {
    c.set(Calendar.MONTH, Calendar.JANUARY);
    c.set(Calendar.DATE, 1);
    c.set(Calendar.HOUR, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
  }

  /**
   * @return true if d is in the range [minDate,maxDate]
   */
  public static boolean isInRange(Date minDate, Date d, Date maxDate) {
    if (d == null || minDate == null || maxDate == null) {
      return false;
    }
    return minDate.compareTo(d) <= 0 && d.compareTo(maxDate) <= 0;
  }

  /**
   * @return true if the ranges intersect minDate and maxDate must not be null
   *         fromDate or toDate may be null
   */
  public static boolean intersects(Date fromDate, Date toDate, Date minDate, Date maxDate) {
    if (minDate == null || maxDate == null) {
      return false;
    }
    if (fromDate == null && toDate == null) {
      return false;
    }
    if (fromDate == null) {
      return toDate.compareTo(minDate) >= 0;
    }
    else if (toDate == null) {
      return fromDate.compareTo(maxDate) <= 0;
    }
    else {
      return fromDate.compareTo(maxDate) <= 0 && toDate.compareTo(minDate) >= 0;
    }
  }

  /**
   * only compares the date, so doesn't care about time
   * 
   * @return true if d is in the date range [minDate,maxDate]
   */
  public static boolean isInDateRange(Date minDate, Date d, Date maxDate) {
    return isInRange(truncDate(minDate), truncDate(d), truncDate(maxDate));
  }

  public static Date nextDay(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    c.add(Calendar.DATE, 1);
    Date dNew = new Date(c.getTime().getTime());
    return dNew;
  }

  public static Date max(Date... a) {
    Date max = null;
    for (Date d : a) {
      if (d != null) {
        if (max == null) {
          max = d;
        }
        else if (d.compareTo(max) > 0) {
          max = d;
        }
      }
    }
    return max;
  }

  public static Date min(Date... a) {
    Date min = null;
    for (Date d : a) {
      if (d != null) {
        if (min == null) {
          min = d;
        }
        else if (d.compareTo(min) < 0) {
          min = d;
        }
      }
    }
    return min;
  }

  public static boolean equals(Date a, Date b) {
    return a == b || (a != null && b != null && a.compareTo(b) == 0);
  }

  public static boolean isSameDay(Date a, Date b) {
    a = truncDate(a);
    b = truncDate(b);
    return equals(a, b);
  }

  public static boolean isSameMonth(Date d1, Date d2) {
    Calendar c = Calendar.getInstance();
    c.setTime(d1);
    int m1 = c.get(Calendar.MONTH);
    c.setTime(d2);
    int m2 = c.get(Calendar.MONTH);
    return (m1 == m2);
  }

  public static boolean isWeekend(Date d) {
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
    return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
  }

  /**
   * Correctly calculate covered days of a day range. 13.3.2008 00:00 -
   * 14.3.2008 00:00 only covers 1 day (13.3.) 13.3.2008 12:00 - 14.3.2008 12:00
   * covers 2 days (13.3., 14.3.)
   * 
   * @return array of days that with time set to 00:00:00.000
   */
  public static Date[] getCoveredDays(Date from, Date to) {
    if (from == null) {
      from = to;
    }
    if (to == null) {
      to = from;
    }
    if (from.compareTo(to) > 0) {
      to = from;
    }
    //
    if (from.compareTo(to) == 0) {
      return new Date[]{truncDate(from)};
    }
    else {
      Calendar a = Calendar.getInstance();
      a.setTime(from);
      truncCalendar(a);
      Calendar b = Calendar.getInstance();
      b.setTime(to);
      b.add(Calendar.MILLISECOND, -1);
      truncCalendar(b);
      long dayCount = ((b.getTimeInMillis() + DAY_MILLIS / 2 - a.getTimeInMillis()) / DAY_MILLIS) + 1;
      Date[] array = new Date[(int) dayCount];
      for (int i = 0; i < array.length; i++) {
        array[i] = a.getTime();
        a.add(Calendar.DATE, 1);
      }
      return array;
    }
  }

  public static <T> T nvl(T value, T valueWhenNull) {
    if (value != null) {
      return value;
    }
    else {
      return valueWhenNull;
    }
  }

  public static Date convertCalendar(Calendar c) {
    if (c == null) {
      return null;
    }
    return new Date(c.getTimeInMillis());
  }

  public static Calendar convertDate(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    return c;
  }

  /**
   * convert a (possible) subclass of {@link Date} to {@link Date}
   */
  public static Date toUtilDate(Date d) {
    if (d != null && d.getClass() != Date.class) {
      d = new Date(d.getTime());
    }
    return d;
  }

  /**
   * combine a date (yy/mm/dd) and a time (hh:mm:ss) into a combined timestamp-date containing both date and time.
   */
  public static Date createDateTime(Date date, Date time) {
    Calendar cal1 = Calendar.getInstance();
    cal1.setTime(date);
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(time);
    cal1.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
    cal1.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
    cal1.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
    cal1.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
    return cal1.getTime();
  }

  /**
   * @param d
   *          a value in [0..1[ representing a day
   * @return the time value as date in the range from 00:00 - 23:59:59
   * @see #convertDateToDoubleTime(Date) inverse function
   */
  public static Date convertDoubleTimeToDate(Number d) {
    if (d == null) {
      return null;
    }
    int m;
    if (d.doubleValue() < 0) {
      m = (int) (((long) (d.doubleValue() * DAY_MILLIS - 0.5)) % DAY_MILLIS);
    }
    else {
      m = (int) (((long) (d.doubleValue() * DAY_MILLIS + 0.5)) % DAY_MILLIS);
    }

    Calendar c = Calendar.getInstance();
    c.clear();
    c.set(Calendar.MILLISECOND, m % 1000);
    m = m / 1000;
    c.set(Calendar.SECOND, m % 60);
    m = m / 60;
    c.set(Calendar.MINUTE, m % 60);
    m = m / 60;
    c.set(Calendar.HOUR_OF_DAY, m % 24);
    if (m < 0) {
      c.add(Calendar.DAY_OF_MONTH, 1);
    }
    return c.getTime();
  }

  /**
   * @param time
   *          a time (hh:mm:ss) in the interval 00:00:00 - 23:59:59
   * @return the time value as a double in the range from [0..1[
   * @see #convertDoubleTimeToDate(Number) inverse function
   */
  public static Double convertDateToDoubleTime(Date time) {
    if (time == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(time);
    double t = ((c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)) * 60 + c.get(Calendar.SECOND)) * 1000 + c.get(Calendar.MILLISECOND);
    Double d = new Double(t / DAY_MILLIS);
    // range check;
    if (d.doubleValue() < 0) {
      d = new Double(0);
    }
    if (d.doubleValue() > 1) {
      d = new Double(1);
    }
    return d;
  }
}
