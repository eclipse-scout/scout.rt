/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.date;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DateUtility {

  private DateUtility() {
  }

  public static final long MINUTE_MILLIS = 60L * 1000L;
  public static final long HOUR_MILLIS = 60L * MINUTE_MILLIS;
  public static final long DAY_MILLIS = 24L * HOUR_MILLIS;

  private static final Logger LOG = LoggerFactory.getLogger(DateUtility.class);

  //2 letter code countries for different weekends worldwide
  private static final List<String> SUN_WEEKEND_DAYS_COUNTRIES = Arrays.asList("GQ", "IN", "TH", "UG");
  private static final List<String> FRY_WEEKEND_DAYS_COUNTRIES = Arrays.asList("DJ", "IR");
  private static final List<String> FRY_SUN_WEEKEND_DAYS_COUNTRIES = Arrays.asList("BN");
  private static final List<String> THU_FRY_WEEKEND_DAYS_COUNTRIES = Arrays.asList("AF");
  private static final List<String> FRY_SAT_WEEKEND_DAYS_COUNTRIES = Arrays.asList("AE", "DZ", "BH", "BD", "EG", "IQ", "IL", "JO", "KW", "LY", "MV", "MR", "OM", "PS", "QA", "SA", "SD", "SY", "YE");

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
   * format date with specific pattern as defined in {@link SimpleDateFormat}
   */
  public static String format(Date d, String pattern) {
    if (d == null || !StringUtility.hasText(pattern)) {
      return "";
    }
    Locale loc = NlsLocale.get();
    return new SimpleDateFormat(pattern, loc).format(d);
  }

  /**
   * format LocalDate with specific pattern as defined in {@link DateTimeFormatter}
   */
  public static String format(LocalDate d, String pattern) {
    if (d == null || !StringUtility.hasText(pattern)) {
      return "";
    }
    return format(d.atStartOfDay(), pattern);
  }

  /**
   * format LocalDateTime with specific pattern as defined in {@link DateTimeFormatter}
   */
  public static String format(LocalDateTime d, String pattern) {
    if (d == null || !StringUtility.hasText(pattern)) {
      return "";
    }
    Locale loc = NlsLocale.get();
    return DateTimeFormatter.ofPattern(pattern, loc).format(d);
  }

  /**
   * Creates a {@link SimpleDateFormat} to parse the first argument according to the provided pattern. Disables lenient
   * behavior of {@link SimpleDateFormat}.
   */
  public static Date parse(String s, String pattern) {
    if (s == null) {
      return null;
    }
    try {
      Locale loc = NlsLocale.get();
      SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
      // default for SimpleDateFormat is a lenient behavior, e.g. 13.13.13, 952.1238.2010, 12.01.191;ABC would also be valid dates
      // disable lenient behavior, could lead to unexpected results
      df.setLenient(false);
      return df.parse(s);
    }
    catch (ParseException e) {
      throw new IllegalArgumentException("parse(\"" + s + "\",\"" + pattern + "\") failed", e);
    }
  }

  /**
   * Creates a {@link DateTimeFormatter} to parse the first argument according to the provided pattern.
   */
  public static LocalDate parseLocalDate(String s, String pattern) {
    if (s == null) {
      return null;
    }
    try {
      Locale loc = NlsLocale.get();
      return LocalDate.parse(s, DateTimeFormatter.ofPattern(pattern, loc));
    }
    catch (DateTimeParseException e) {
      throw new IllegalArgumentException("parse(\"" + s + "\",\"" + pattern + "\") failed", e);
    }
  }

  /**
   * Creates a {@link DateTimeFormatter} to parse the first argument according to the provided pattern.
   */
  public static LocalDateTime parseLocalDateTime(String s, String pattern) {
    if (s == null) {
      return null;
    }
    try {
      Locale loc = NlsLocale.get();
      return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(pattern, loc));
    }
    catch (DateTimeParseException e) {
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
   * @return <code>true</code> if, and only if the given String is a valid date according to the given date format.
   */
  public static boolean isValidDate(String s, String pattern) {
    try {
      parse(s, pattern);
      return true;
    }
    catch (IllegalArgumentException e) {
      LOG.debug("could not parse date from given text [s='{}',pattern='{}']", s, pattern, e);
    }
    return false;
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
    cal.add(Calendar.SECOND, sign * ((sec) % 60));
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

  public static LocalDateTime truncDate(LocalDateTime d) {
    if (d == null) {
      return null;
    }
    return d.withNano(0).withSecond(0).withMinute(0).withHour(0);
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

  public static LocalDateTime truncDateToHour(LocalDateTime d) {
    if (d == null) {
      return null;
    }
    return d.withNano(0).withSecond(0).withMinute(0);
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

  public static LocalDateTime truncDateToMinute(LocalDateTime d) {
    if (d == null) {
      return null;
    }
    return d.withNano(0).withSecond(0);
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

  public static LocalDateTime truncDateToSecond(LocalDateTime d) {
    if (d == null) {
      return null;
    }
    return d.withNano(0);
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
   * @see "ISO 8601"
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
   * truncate the calendar to quarter year (i.e. jan 1, apr 1, jul 1 or oct 1 of the given year)
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
   * truncate the calendar to minute
   */
  public static void truncCalendarToMinute(Calendar c) {
    if (c == null) {
      return;
    }
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
    return compareTo(minDate, d) <= 0 && compareTo(d, maxDate) <= 0;
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
      return compareTo(toDate, minDate) >= 0;
    }
    else if (toDate == null) {
      return compareTo(fromDate, maxDate) <= 0;
    }
    else {
      return compareTo(fromDate, maxDate) <= 0 && compareTo(toDate, minDate) >= 0;
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
        else if (compareTo(d, max) > 0) {
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
        else if (compareTo(d, min) < 0) {
          min = d;
        }
      }
    }
    return min;
  }

  /**
   * <b>Timestamp and Date:</b> {@link Timestamp#equals(Object)} is not symmetric. E.g. when comparing a Date d and
   * Timestamp t, d.equals(t) may return true while t.equals(d) will always return false. This is not "expected" and
   * inconvenient when performing operations like sorting on collections containing both Dates and Timestamps.
   * Therefore, this method handles <code>java.sql.Timestamp</code> specifically to provide a symmetric implementation
   * of the equivalence comparison.
   * <p>
   * <code>java.sql.Timestamp</code> is a subclass of <code>java.util.Date</code>, which additionally allows to specify
   * fractional seconds to a precision of nanoseconds. This method returns <code>true</code>, if and only if both
   * arguments of Type <code>java.util.Date</code> or <code>java.sql.Timestamp</code> represent the same point in time
   * at full precision.
   */
  @SuppressWarnings("squid:S1201")
  public static boolean equals(Date a, Date b) {
    return a == b || (a != null && b != null && compareTo(a, b) == 0);
  }

  /**
   * Both parameters must be non-null. Implementation of {@code a.compareTo(b)} that works for Timestamp and Date.
   * <p>
   * <b>Timestamp and Date:</b> {@link Timestamp#compareTo(Date)} is not symmetric. E.g. when comparing a Date d and
   * Timestamp t, d.compareTo(t) may return true while t.compareTo(d) returns false. This is not "expected" and
   * inconvenient when performing operations like sorting on collections containing both Dates and Timestamps.
   * Therefore, this method handles <code>java.sql.Timestamp</code> specifically to provide a symmetric implementation
   * of the comparison operation.
   * <p>
   * On Java 8 and below, {@link Date#compareTo} returns erroneous results when the argument is a Timestamp, due to
   * ignoring the fractional seconds part of the Timestamp (see JDK-8135055). Due to this and in order to retain full
   * precision, {@link Timestamp#compareTo} must be called. This method returns <code>0</code> if and only if both
   * arguments of Type <code>java.util.Date</code> or <code>java.sql.Timestamp</code> represent the same point in time
   * at full precision.
   * <p>
   * Not null safe, see {@link ObjectUtility#compareTo} for a null-safe implementation of compareTo.
   */
  public static int compareTo(Date a, Date b) {
    if (b instanceof java.sql.Timestamp && !(a instanceof java.sql.Timestamp)) {
      // ensure Timestamp.compareTo is called, not Date.compareTo
      // note: could also call use -1 * b.compareTo(a) here, but that equivalence is not specified and a new Timestamp object would be created regardless (in Timestamp.compareTo)
      return (new Timestamp(a.getTime())).compareTo(b);
    }
    return a.compareTo(b);
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
    double d = t / DAY_MILLIS;
    // range check
    if (d < 0) {
      d = 0d;
    }
    if (d > 1) {
      d = 1d;
    }
    return d;
  }

  /**
   * Converts a {@link Date} to a {@link LocalDate} using the default time-zone of the system.
   */
  public static LocalDate toLocalDate(Date d) {
    if (d == null) {
      return null;
    }
    return d.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate();
  }

  /**
   * Converts a {@link Date} to a {@link LocalDateTime} using the default time-zone of the system.
   */
  public static LocalDateTime toLocalDateTime(Date d) {
    if (d == null) {
      return null;
    }
    return d.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
  }

  /**
   * Converts a {@link LocalDate} to a {@link Date} using the default time-zone of the system. The time will be set to
   * midnight.
   */
  public static Date toUtilDate(LocalDate localDate) {
    if (localDate == null) {
      return null;
    }
    return Date.from(localDate.atStartOfDay()
        .atZone(ZoneId.systemDefault())
        .toInstant());
  }

  /**
   * Converts a {@link LocalDateTime} to a {@link Date} using the default time-zone of the system.
   */
  public static Date toUtilDate(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return Date.from(localDateTime
        .atZone(ZoneId.systemDefault())
        .toInstant());
  }

  /**
   * Calculates the number of days between <code>start</code> and <code>end</code>. If the end date is before the start
   * date, the result will be positive as well. Example:
   * <ul>
   * <li>start = 1.1.2000, end = 2.1.2000 --> getDaysBetween(start, end) = 1
   * <li>start = 2.1.2000, end = 1.1.2000 --> getDaysBetween(start, end) = 1
   * </ul>
   *
   * @param start
   *          the start date, inclusive
   * @param end
   *          the end date, exclusive
   * @return <code>-1</code> in case of an error (e.g. parameter is null)
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

  /**
   * Returns the absolute value of hours between <code>start</code> and <code>end</code>. Both input values are first
   * truncated to hours, i.e. any hour fractions (minutes, seconds) will be ignored.
   * <p>
   * <b>Examples:</b>
   * <li>start = 2020-01-01 15:02:03, end = 2020-01-02 13:01:05, result = 22
   * <li>start = 2020-01-02 13:01:05, end = 2020-01-01 15:02:03, result = 22
   * <li>start = 2020-01-01 15:02:03, end = 2020-01-01 15:02:03, result = 0
   * <li>start = 2020-01-01 15:02:03, end = 2020-01-01 15:59:59, result = 0
   * <li>start = 2020-01-01 15:02:03, end = 2020-01-01 16:00:00, result = 1
   * </ul>
   *
   * @param start
   *          the start date, inclusive
   * @param end
   *          the end date, exclusive
   * @return <code>-1</code> in case of an error (e.g. parameter is null)
   */
  public static int getHoursBetween(Date start, Date end) {
    if (start == null || end == null) {
      return -1;
    }
    Calendar startDate = DateUtility.convertDate(start);
    DateUtility.truncCalendarToHour(startDate);
    Calendar endDate = DateUtility.convertDate(end);
    DateUtility.truncCalendarToHour(endDate);

    long endL = endDate.getTimeInMillis() + endDate.getTimeZone().getOffset(endDate.getTimeInMillis());
    long startL = startDate.getTimeInMillis() + startDate.getTimeZone().getOffset(startDate.getTimeInMillis());
    int numHours = (int) ((endL - startL) / HOUR_MILLIS);
    return Math.abs(numHours);
  }

  /**
   * Returns the absolute value of minutes between <code>start</code> and <code>end</code>. Both input values are first
   * truncated to minutes, i.e. any minutes fractions (seconds, millis) will be ignored.
   * <p>
   * <b>Examples:</b>
   * <li>start = 2020-01-01 15:02:03, end = 2020-01-01 15:01:05, result = 1
   * <li>start = 2020-01-01 15:01:05, end = 2020-01-01 15:02:03, result = 1
   * </ul>
   *
   * @param start
   *          the start date, inclusive
   * @param end
   *          the end date, exclusive
   * @return <code>-1</code> in case of an error (e.g. parameter is null)
   */
  public static int getMinutesBetween(Date start, Date end) {
    if (start == null || end == null) {
      return -1;
    }
    Calendar startDate = DateUtility.convertDate(start);
    DateUtility.truncCalendarToMinute(startDate);
    Calendar endDate = DateUtility.convertDate(end);
    DateUtility.truncCalendarToMinute(endDate);

    long endL = endDate.getTimeInMillis() + endDate.getTimeZone().getOffset(endDate.getTimeInMillis());
    long startL = startDate.getTimeInMillis() + startDate.getTimeZone().getOffset(startDate.getTimeInMillis());
    int numMinutes = (int) ((endL - startL) / (MINUTE_MILLIS));
    return Math.abs(numMinutes);
  }
}
