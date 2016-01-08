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
package org.eclipse.scout.rt.platform.util.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.StringUtility;

public final class DateUtility {

  private DateUtility() {
  }

  public static final long DAY_MILLIS = 24L * 3600L * 1000L;

  //2 letter code countries for different weekends worldwide
  private static final List<String> SUN_WEEKEND_DAYS_COUNTRIES = Arrays.asList(new String[]{"GQ", "IN", "TH", "UG"});
  private static final List<String> FRY_WEEKEND_DAYS_COUNTRIES = Arrays.asList(new String[]{"DJ", "IR"});
  private static final List<String> FRY_SUN_WEEKEND_DAYS_COUNTRIES = Arrays.asList(new String[]{"BN"});
  private static final List<String> THU_FRY_WEEKEND_DAYS_COUNTRIES = Arrays.asList(new String[]{"AF"});
  private static final List<String> FRY_SAT_WEEKEND_DAYS_COUNTRIES = Arrays.asList(new String[]{"AE", "DZ", "BH", "BD", "EG", "IQ", "IL", "JO", "KW", "LY", "MV", "MR", "OM", "PS", "QA", "SA", "SD", "SY", "YE"});

  /**
   * format date with {@value DateFormat#DEFAULT} pattern
   */
  public static String formatDate(Date d) {
    if (d == null) {
      return "";
    }
    Locale loc = NlsLocale.get();
    return BEANS.get(DateFormatProvider.class).getDateInstance(DateFormat.DEFAULT, loc).format(d);
  }

  /**
   * format time with {@value DateFormat#SHORT} pattern
   */
  public static String formatTime(Date d) {
    if (d == null) {
      return "";
    }
    Locale loc = NlsLocale.get();
    return BEANS.get(DateFormatProvider.class).getTimeInstance(DateFormat.SHORT, loc).format(d);
  }

  /**
   * format time with {@value DateFormat#SHORT}, {@value DateFormat#SHORT} patterns
   */
  public static String formatDateTime(Date d) {
    if (d == null) {
      return "";
    }
    Locale loc = NlsLocale.get();
    return BEANS.get(DateFormatProvider.class).getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, loc).format(d);
  }

  /**
   * format date with specific pattern as defined in {@link java.text.SimpleDateFormat}
   */
  public static String format(Date d, String pattern) {
    if (d == null || !StringUtility.hasText(pattern)) {
      return "";
    }
    Locale loc = NlsLocale.get();
    return new SimpleDateFormat(pattern, loc).format(d);
  }

  public static Date parse(String s, String pattern) {
    if (s == null) {
      return null;
    }
    try {
      Locale loc = NlsLocale.get();
      return new SimpleDateFormat(pattern, loc).parse(s);
    }
    catch (ParseException e) {
      throw new IllegalArgumentException("parse(\"" + s + "\",\"" + pattern + "\") failed", e);
    }
  }

  /**
   * Returns <code>true</code> if, and only if the given String is a valid date according to the given date format.
   *
   * @param s
   *          date
   * @param pattern
   *          date format
   * @returns <code>true</code> if, and only if the given String is a valid date according to the given date format.
   */
  public static boolean isValidDate(String s, String pattern) {
    try {
      Locale loc = NlsLocale.get();
      SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
      df.setLenient(false);
      df.parse(s);
      return true;
    }
    catch (ParseException e) {
      return false;
    }
  }

  public static Date addMilliseconds(Date d, int milliseconds) {
    return addTime(d, Calendar.MILLISECOND, milliseconds);
  }

  public static Date addSeconds(Date d, int seconds) {
    return addTime(d, Calendar.SECOND, seconds);
  }

  public static Date addMinutes(Date d, int minutes) {
    return addTime(d, Calendar.MINUTE, minutes);
  }

  public static Date addHours(Date d, int hours) {
    return addTime(d, Calendar.HOUR_OF_DAY, hours);
  }

  public static Date addTime(Date d, int field, int amount) {
    if (d == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    cal.add(field, amount);
    return cal.getTime();
  }

  /**
   * Adds a number of days to a date.
   *
   * @param count
   *          days is truncated to second and can be negative
   * @param d
   *          may be <code>null</code>
   */
  public static Date addDays(Date d, double count) {
    if (d == null) {
      return null;
    }
    int sign = 1;
    if (count < 0) {
      count = -count;
      sign = -1;
    }
    double roundingFactor = (sign > 0) ? 0.000004 : 0.0000017;
    int sec = (int) ((count + roundingFactor) * 3600 * 24);
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    cal.add(Calendar.DATE, sign * (sec / 3600 / 24));
    cal.add(Calendar.HOUR_OF_DAY, sign * ((sec / 3600) % 24));
    cal.add(Calendar.MINUTE, sign * ((sec / 60) % 60));
    cal.add(Calendar.SECOND, (int) (sign * ((sec) % 60)));
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
    return c.getTime();
  }

  /**
   * truncate the date to hour
   *
   * @since 4.2
   */
  public static Date truncDateToHour(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    truncCalendarToHour(c);
    return c.getTime();
  }

  public static Date truncDateToMinute(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c.getTime();
  }

  public static Date truncDateToSecond(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    c.set(Calendar.MILLISECOND, 0);
    return c.getTime();
  }

  /**
   * truncate the date to week (depends on locale)
   */
  public static Date truncDateToWeek(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    truncCalendarToWeek(c, -1);
    return c.getTime();
  }

  /**
   * truncate the date to week (does not depend on locale, monday is always the first day in a week)
   *
   * @see ISO 8601
   */
  public static Date truncDateToIsoWeek(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    truncCalendarToIsoWeek(c, -1);
    return c.getTime();
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
    return c.getTime();
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
    return c.getTime();
  }

  /**
   * truncate the date to half year (i.e. jan 1 or jul 1 of the given year)
   *
   * @since 4.2
   */
  public static Date truncDateToHalfYear(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    truncCalendarToHalfYear(c);
    return c.getTime();
  }

  /**
   * truncate the date to quarter year (i.e. jan 1, apr 1, jul 1 or oct 1 of the given year)
   *
   * @since 4.2
   */
  public static Date truncDateToQuarter(Date d) {
    if (d == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    truncCalendarToQuarter(c);
    return c.getTime();
  }

  /**
   * truncate the calendar to a day with time 00:00:00.000
   */
  public static void truncCalendar(Calendar c) {
    if (c == null) {
      return;
    }
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
  }

  public static void truncCalendarToWeek(Calendar c, int adjustIncrement) {
    truncCalendarToWeek(c, adjustIncrement, Calendar.getInstance().getFirstDayOfWeek());
  }

  /**
   * Truncates to monday, see ISO 8601
   *
   * @param c
   *          Calendar to truncate
   * @param adjustIncrement
   *          -1 to back in time, +1 to go forward
   */
  public static void truncCalendarToIsoWeek(Calendar c, int adjustIncrement) {
    truncCalendarToWeek(c, adjustIncrement, Calendar.MONDAY);
  }

  /**
   * truncate the calendar to week
   *
   * @param adjustIncrement
   *          +1 or -1
   */
  public static void truncCalendarToWeek(Calendar c, int adjustIncrement, int firstDayOfWeek) {
    if (c == null) {
      return;
    }
    if (adjustIncrement < -1) {
      adjustIncrement = -1;
    }
    if (adjustIncrement > 1) {
      adjustIncrement = 1;
    }
    if (adjustIncrement == 0) {
      adjustIncrement = -1;
    }
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    while (c.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
      c.add(Calendar.DATE, adjustIncrement);
    }
  }

  /**
   * truncate the calendar to month
   */
  public static void truncCalendarToMonth(Calendar c) {
    if (c == null) {
      return;
    }
    c.set(Calendar.DATE, 1);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
  }

  /**
   * truncate the calendar to year
   */
  public static void truncCalendarToYear(Calendar c) {
    if (c == null) {
      return;
    }
    c.set(Calendar.MONTH, Calendar.JANUARY);
    c.set(Calendar.DATE, 1);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
  }

  /**
   * truncate the calendar to half year (i.e. jan 1 or jul 1 of the given year)
   *
   * @since 4.2
   */
  public static void truncCalendarToHalfYear(Calendar c) {
    if (c == null) {
      return;
    }
    int month = c.get(Calendar.MONTH);
    truncCalendarToYear(c);
    if (month >= Calendar.JULY) {
      c.set(Calendar.MONTH, Calendar.JULY);
    }
  }

  /**
   * truncate the calendar to half year (i.e. jan 1, apr 1, jul 1 or oct 1 of the given year)
   *
   * @since 4.2
   */
  public static void truncCalendarToQuarter(Calendar c) {
    if (c == null) {
      return;
    }
    final int month = c.get(Calendar.MONTH);
    truncCalendarToYear(c);
    int quarterMonth = Calendar.JANUARY;
    switch (month) {
      case Calendar.APRIL:
      case Calendar.MAY:
      case Calendar.JUNE:
        quarterMonth = Calendar.APRIL;
        break;
      case Calendar.JULY:
      case Calendar.AUGUST:
      case Calendar.SEPTEMBER:
        quarterMonth = Calendar.JULY;
        break;
      case Calendar.OCTOBER:
      case Calendar.NOVEMBER:
      case Calendar.DECEMBER:
        quarterMonth = Calendar.OCTOBER;
        break;
    }
    if (quarterMonth != Calendar.JANUARY) {
      c.set(Calendar.MONTH, quarterMonth);
    }
  }

  /**
   * truncate the calendar to hour
   *
   * @since 4.2
   */
  public static void truncCalendarToHour(Calendar c) {
    if (c == null) {
      return;
    }
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
   * @return true if the ranges intersect minDate and maxDate must not be null fromDate or toDate may be null
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
    Date dNew = c.getTime();
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
    return isWeekend(d, NlsLocale.get());
  }

  public static boolean isWeekend(Date d, Locale locale) {
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

    int[] weekendDays = getWeekendDays(locale);
    for (int weekendDay : weekendDays) {
      if (dayOfWeek == weekendDay) {
        return true;
      }
    }
    return false;
  }

  private static int[] getWeekendDays(Locale locale) {
    if (THU_FRY_WEEKEND_DAYS_COUNTRIES.contains(locale.getCountry())) {
      return new int[]{Calendar.THURSDAY, Calendar.FRIDAY};
    }
    else if (FRY_SUN_WEEKEND_DAYS_COUNTRIES.contains(locale.getCountry())) {
      return new int[]{Calendar.FRIDAY, Calendar.SUNDAY};
    }
    else if (FRY_WEEKEND_DAYS_COUNTRIES.contains(locale.getCountry())) {
      return new int[]{Calendar.FRIDAY};
    }
    else if (SUN_WEEKEND_DAYS_COUNTRIES.contains(locale.getCountry())) {
      return new int[]{Calendar.SUNDAY};
    }
    else if (FRY_SAT_WEEKEND_DAYS_COUNTRIES.contains(locale.getCountry())) {
      return new int[]{Calendar.FRIDAY, Calendar.SATURDAY};
    }
    else {
      return new int[]{Calendar.SATURDAY, Calendar.SUNDAY};
    }
  }

  /**
   * Correctly calculate covered days of a day range. 13.3.2008 00:00 - 14.3.2008 00:00 only covers 1 day (13.3.)
   * 13.3.2008 12:00 - 14.3.2008 12:00 covers 2 days (13.3., 14.3.)
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

  /**
   * Calculates the number of days between <code>start</code> and <code>end</code>. If the end date is before the start
   * date, the result will be positive as well. Example:
   * <ul>
   * <li>start = 1.1.2000, end = 2.1.2000 --> getDaysBetween(start, end) = 1
   * <li>start = 2.1.2000, end = 1.1.2000 --> getDaysBetween(start, end) = 1
   * </ul>
   * returns <code>-1</code> in case of an error (e.g. parameter is null)
   */
  public static int getDaysBetween(Date start, Date end) {
    if (start == null || end == null) {
      return -1;
    }

    Calendar startDate = convertDate(start);
    truncCalendar(startDate);
    Calendar endDate = convertDate(end);
    truncCalendar(endDate);

    long endL = endDate.getTimeInMillis() + endDate.getTimeZone().getOffset(endDate.getTimeInMillis());
    long startL = startDate.getTimeInMillis() + startDate.getTimeZone().getOffset(startDate.getTimeInMillis());
    int numDays = (int) ((endL - startL) / DAY_MILLIS);
    return Math.abs(numDays);
  }
}
