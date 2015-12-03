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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for {@link DateUtility}
 * <p>
 * Tests in this class change global state (timezone). Not designed to run in parallel.
 * </p>
 */
public class DateUtilityTest {
  private static final String EXPEC_DATE_1970_01_01_000000 = "1970-01-01 00:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_003000 = "1970-01-01 00:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_010000 = "1970-01-01 01:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_013000 = "1970-01-01 01:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_020000 = "1970-01-01 02:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_023000 = "1970-01-01 02:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_030000 = "1970-01-01 03:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_033000 = "1970-01-01 03:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_040000 = "1970-01-01 04:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_043000 = "1970-01-01 04:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_050000 = "1970-01-01 05:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_053000 = "1970-01-01 05:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_060000 = "1970-01-01 06:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_063000 = "1970-01-01 06:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_070000 = "1970-01-01 07:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_073000 = "1970-01-01 07:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_080000 = "1970-01-01 08:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_083000 = "1970-01-01 08:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_090000 = "1970-01-01 09:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_093000 = "1970-01-01 09:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_100000 = "1970-01-01 10:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_103000 = "1970-01-01 10:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_110000 = "1970-01-01 11:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_113000 = "1970-01-01 11:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_120000 = "1970-01-01 12:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_123000 = "1970-01-01 12:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_130000 = "1970-01-01 13:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_133000 = "1970-01-01 13:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_140000 = "1970-01-01 14:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_143000 = "1970-01-01 14:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_150000 = "1970-01-01 15:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_153000 = "1970-01-01 15:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_160000 = "1970-01-01 16:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_163000 = "1970-01-01 16:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_170000 = "1970-01-01 17:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_173000 = "1970-01-01 17:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_180000 = "1970-01-01 18:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_183000 = "1970-01-01 18:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_190000 = "1970-01-01 19:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_193000 = "1970-01-01 19:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_200000 = "1970-01-01 20:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_203000 = "1970-01-01 20:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_210000 = "1970-01-01 21:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_213000 = "1970-01-01 21:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_220000 = "1970-01-01 22:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_223000 = "1970-01-01 22:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_230000 = "1970-01-01 23:00:00.000";
  private static final String EXPEC_DATE_1970_01_01_233000 = "1970-01-01 23:30:00.000";
  private static final String EXPEC_DATE_1970_01_01_235959 = "1970-01-01 23:59:59.000";

  private static final String DATE_TIME_PATTERN = "dd-MM-yyyy HH:mm";
  private static final String YEAR_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
  private static final int HOURS_IN_DAY = 24;

  private static final double MINUTES_IN_HOURS = 60;
  private static final double SECONDS_IN_MINUTES = 60;
  private static final double SECONDS_IN_DAY = HOURS_IN_DAY * MINUTES_IN_HOURS * SECONDS_IN_MINUTES;

  private TimeZone m_timezoneBackup;

  /**
   * Store original timezone, such that it can be changed in tests.
   */
  @Before
  public void backupOriginalTimeZone() {
    m_timezoneBackup = TimeZone.getDefault();
  }

  /**
   * Reset the timezone to the original timezone.
   */
  @After
  public void restoreOriginalTimeZone() {
    TimeZone.setDefault(m_timezoneBackup);
  }

  /**
   * Test for {@link DateUtility#addDays(Date, double)}
   *
   * @throws ParseException
   */
  @Test
  public void testAddDaysFull() throws ParseException {
    String date1 = "2013-09-30 10:10:10.111";
    String date2 = "2013-10-01 10:10:10.111";
    assertDateEquals(date2, DateUtility.addDays(dateOf(date1), 1));
    assertDateEquals(date1, DateUtility.addDays(dateOf(date2), -1));
  }

  /**
   * Test for {@link DateUtility#addDays(Date, double)} for a larger day period
   *
   * @throws ParseException
   */
  @Test
  public void testAddDaysLarge() throws ParseException {
    assertDateEquals("2030-12-31 00:00:00.000", DateUtility.addDays(dateOf("1990-01-01 00:00:00.000"), 14974));
  }

  /**
   * Test for {@link DateUtility#addDays(Date, double)}
   *
   * @throws ParseException
   */
  @Test
  public void testAddDatetoNullFull() throws ParseException {
    assertNull(DateUtility.addDays(null, -1));
  }

  /**
   * Tests for all seconds in a day, if adding this fraction of the day is the same as setting the seconds directly.
   * {@link DateUtility#addDays(Date, double)}
   */
  @Test
  public void testAddDays() {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
    assertEquals("Test only works without day Daylight Saving Time", 0, TimeZone.getDefault().getDSTSavings());

    for (int sec = 0; sec < SECONDS_IN_DAY; sec++) {
      double d = sec / SECONDS_IN_DAY;
      Calendar cal = getCalendar(1, 0);

      String result = stringOf(DateUtility.addDays(cal.getTime(), d));

      cal.set(Calendar.SECOND, sec);

      String expected = stringOf(cal.getTime());
      assertEquals(expected, result);
    }
  }

  /**
   * Tests for all seconds in a day, if subtracting this fraction of the day is the same as setting the seconds
   * directly. {@link DateUtility#addDays(Date, double)}
   */
  @Test
  public void testSubtractDays() {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
    assertEquals("Test only works without day Daylight Saving Time", 0, TimeZone.getDefault().getDSTSavings());

    for (int sec = 0; sec < SECONDS_IN_DAY; sec++) {
      double d = -sec / SECONDS_IN_DAY;
      Calendar cal = getCalendar(1, 0);

      String result = stringOf(DateUtility.addDays(cal.getTime(), d));

      cal.set(Calendar.SECOND, -sec);

      String expected = stringOf(cal.getTime());
      assertEquals(expected, result);
    }
  }

  private Calendar getCalendar(int hour, int minute) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR, hour);
    cal.set(Calendar.MINUTE, minute);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal;
  }

  /**
   * Test for {@link DateUtility#addDays(Date, double)} using {@link DateUtility#convertDateToDoubleTime(Date)}
   */
  @Test
  public void testDuration() {
    final Calendar c1500 = getCalendar(15, 0);
    final Calendar c1515 = getCalendar(15, 15);
    final Calendar c1445 = getCalendar(14, 45);
    final Calendar c1530 = getCalendar(15, 30);
    final Calendar c1600 = getCalendar(16, 0);
    final Calendar c1700 = getCalendar(17, 0);
    assertCorrectDuration(c1500, c1500);
    assertCorrectDuration(c1500, c1515);
    assertCorrectDuration(c1500, c1445);
    assertCorrectDuration(c1500, c1530);
    assertCorrectDuration(c1500, c1600);
    assertCorrectDuration(c1500, c1700);
    assertCorrectDuration(c1700, c1500);
  }

  public void assertCorrectDuration(Calendar start, Calendar end) {
    Date startDate = start.getTime();
    Date endDate = end.getTime();
    Double duration = (DateUtility.convertDateToDoubleTime(endDate) - DateUtility.convertDateToDoubleTime(startDate));
    assertEquals(endDate, DateUtility.addDays(startDate, duration));
  }

  /**
   * Test for {@link DateUtility#addMilliseconds(Date, int)}
   * <p>
   * This test changes global state while running: Sets the default timezone to GMT+1 (no daylight safing time)
   * </p>
   */
  @Test
  public void testAddMilliseconds() {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
    assertEquals("Test only works without day Daylight Saving Time", 0, TimeZone.getDefault().getDSTSavings());

    for (int i = 0; i < SECONDS_IN_DAY; i++) {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DATE, 1);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      String result = stringOf(DateUtility.addMilliseconds(cal.getTime(), i));
      cal.set(Calendar.MILLISECOND, i);
      String expected = stringOf(cal.getTime());
      assertEquals(expected, result);
    }
  }

  /**
   * Test for {@link DateUtility#addSeconds(Date, int)}
   * <p>
   * This test changes global state while running: Sets the default timezone to GMT+1 (no daylight safing time)
   * </p>
   */
  @Test
  public void testAddSeconds() {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
    assertEquals("Test only works without day Daylight Saving Time", 0, TimeZone.getDefault().getDSTSavings());

    Calendar cal = Calendar.getInstance();
    for (int i = 0; i < SECONDS_IN_DAY; i++) {
      cal.set(Calendar.DATE, 1);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      String result = stringOf(DateUtility.addSeconds(cal.getTime(), i));
      cal.set(Calendar.SECOND, i);
      String expected = stringOf(cal.getTime());
      assertEquals(expected, result);
    }
  }

  /**
   * Test for {@link DateUtility#addMinutes(Date, int)}
   * <p>
   * This test changes global state while running: Sets the default timezone to GMT+1 (no daylight safing time)
   * </p>
   */
  @Test
  public void testAddMinutes() {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
    assertEquals("Test only works without day Daylight Saving Time", 0, TimeZone.getDefault().getDSTSavings());
    //test adding minutes for 10 days
    int testDays = 10;
    double minutes = testDays * MINUTES_IN_HOURS * HOURS_IN_DAY;
    Calendar cal = Calendar.getInstance();

    for (int i = 0; i < minutes; i++) {
      cal.set(Calendar.DATE, 1);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MINUTE, 0);

      String originalTime = stringOf(cal.getTime());
      String resultTime = stringOf(DateUtility.addMinutes(cal.getTime(), i));
      cal.set(Calendar.MINUTE, i);
      String expectedTime = stringOf(cal.getTime());
      assertEquals("Add " + i + "minutes to " + originalTime, expectedTime, resultTime);
    }
  }

  /**
   * Test for {@link DateUtility#addMonths(Date d, int count)}
   *
   * @throws ParseException
   */
  @Test
  public void testAddMonthstoNullFull() throws ParseException {
    assertNull(DateUtility.addMonths(null, -1));
  }

  /**
   * Test for {@link DateUtility#addMonths(Date d, int count)}
   *
   * @throws ParseException
   */
  @Test
  public void testAddMonthsFull() throws ParseException {
    String date1 = "2013-12-10 10:10:10.111";
    String date2 = "2014-01-10 10:10:10.111";
    assertDateEquals(date2, DateUtility.addMonths(dateOf(date1), 1));
    assertDateEquals(date1, DateUtility.addMonths(dateOf(date2), -1));
  }

  @Test
  public void testIsInDateRange() {
    Date from = DateUtility.parse("01-02-2011 00:00", DATE_TIME_PATTERN);
    Date d = DateUtility.parse("09-02-2011 00:00", DATE_TIME_PATTERN);
    assertFalse(DateUtility.isInDateRange(from, d, DateUtility.parse("08-02-2011 14:52", DATE_TIME_PATTERN)));
    assertFalse(DateUtility.isInDateRange(from, d, DateUtility.parse("08-02-2011 23:59", DATE_TIME_PATTERN)));
    assertTrue(DateUtility.isInDateRange(from, d, DateUtility.parse("09-02-2011 00:00", DATE_TIME_PATTERN)));
    assertTrue(DateUtility.isInDateRange(from, d, DateUtility.parse("09-02-2011 00:01", DATE_TIME_PATTERN)));
  }

  @Test
  public void testConvertDoubleTimeToDate() {
    //TimeField selector values:
    testConvertDoubleTimeToDate(0.0, EXPEC_DATE_1970_01_01_000000);
    testConvertDoubleTimeToDate(0.020833333, EXPEC_DATE_1970_01_01_003000);
    testConvertDoubleTimeToDate(0.041666667, EXPEC_DATE_1970_01_01_010000);
    testConvertDoubleTimeToDate(0.0625, EXPEC_DATE_1970_01_01_013000);
    testConvertDoubleTimeToDate(0.083333333, EXPEC_DATE_1970_01_01_020000);
    testConvertDoubleTimeToDate(0.104166667, EXPEC_DATE_1970_01_01_023000);
    testConvertDoubleTimeToDate(0.125, EXPEC_DATE_1970_01_01_030000);
    testConvertDoubleTimeToDate(0.145833333, EXPEC_DATE_1970_01_01_033000);
    testConvertDoubleTimeToDate(0.166666667, EXPEC_DATE_1970_01_01_040000);
    testConvertDoubleTimeToDate(0.1875, EXPEC_DATE_1970_01_01_043000);
    testConvertDoubleTimeToDate(0.208333333, EXPEC_DATE_1970_01_01_050000);
    testConvertDoubleTimeToDate(0.229166667, EXPEC_DATE_1970_01_01_053000);
    testConvertDoubleTimeToDate(0.25, EXPEC_DATE_1970_01_01_060000);
    testConvertDoubleTimeToDate(0.270833333, EXPEC_DATE_1970_01_01_063000);
    testConvertDoubleTimeToDate(0.291666667, EXPEC_DATE_1970_01_01_070000);
    testConvertDoubleTimeToDate(0.3125, EXPEC_DATE_1970_01_01_073000);
    testConvertDoubleTimeToDate(0.333333333, EXPEC_DATE_1970_01_01_080000);
    testConvertDoubleTimeToDate(0.354166667, EXPEC_DATE_1970_01_01_083000);
    testConvertDoubleTimeToDate(0.375, EXPEC_DATE_1970_01_01_090000);
    testConvertDoubleTimeToDate(0.395833333, EXPEC_DATE_1970_01_01_093000);
    testConvertDoubleTimeToDate(0.416666667, EXPEC_DATE_1970_01_01_100000);
    testConvertDoubleTimeToDate(0.4375, EXPEC_DATE_1970_01_01_103000);
    testConvertDoubleTimeToDate(0.458333333, EXPEC_DATE_1970_01_01_110000);
    testConvertDoubleTimeToDate(0.479166667, EXPEC_DATE_1970_01_01_113000);
    testConvertDoubleTimeToDate(0.5, EXPEC_DATE_1970_01_01_120000);
    testConvertDoubleTimeToDate(0.520833333, EXPEC_DATE_1970_01_01_123000);
    testConvertDoubleTimeToDate(0.541666667, EXPEC_DATE_1970_01_01_130000);
    testConvertDoubleTimeToDate(0.5625, EXPEC_DATE_1970_01_01_133000);
    testConvertDoubleTimeToDate(0.583333333, EXPEC_DATE_1970_01_01_140000);
    testConvertDoubleTimeToDate(0.604166667, EXPEC_DATE_1970_01_01_143000);
    testConvertDoubleTimeToDate(0.625, EXPEC_DATE_1970_01_01_150000);
    testConvertDoubleTimeToDate(0.645833333, EXPEC_DATE_1970_01_01_153000);
    testConvertDoubleTimeToDate(0.666666667, EXPEC_DATE_1970_01_01_160000);
    testConvertDoubleTimeToDate(0.6875, EXPEC_DATE_1970_01_01_163000);
    testConvertDoubleTimeToDate(0.708333333, EXPEC_DATE_1970_01_01_170000);
    testConvertDoubleTimeToDate(0.729166667, EXPEC_DATE_1970_01_01_173000);
    testConvertDoubleTimeToDate(0.75, EXPEC_DATE_1970_01_01_180000);
    testConvertDoubleTimeToDate(0.770833333, EXPEC_DATE_1970_01_01_183000);
    testConvertDoubleTimeToDate(0.791666667, EXPEC_DATE_1970_01_01_190000);
    testConvertDoubleTimeToDate(0.8125, EXPEC_DATE_1970_01_01_193000);
    testConvertDoubleTimeToDate(0.833333333, EXPEC_DATE_1970_01_01_200000);
    testConvertDoubleTimeToDate(0.854166667, EXPEC_DATE_1970_01_01_203000);
    testConvertDoubleTimeToDate(0.875, EXPEC_DATE_1970_01_01_210000);
    testConvertDoubleTimeToDate(0.895833333, EXPEC_DATE_1970_01_01_213000);
    testConvertDoubleTimeToDate(0.916666667, EXPEC_DATE_1970_01_01_220000);
    testConvertDoubleTimeToDate(0.9375, EXPEC_DATE_1970_01_01_223000);
    testConvertDoubleTimeToDate(0.958333333, EXPEC_DATE_1970_01_01_230000);
    testConvertDoubleTimeToDate(0.979166667, EXPEC_DATE_1970_01_01_233000);

    //Limit condition:
    testConvertDoubleTimeToDate(0.999988425925926, EXPEC_DATE_1970_01_01_235959);

    //Out of definition Range:
    testConvertDoubleTimeToDate(1.0, EXPEC_DATE_1970_01_01_000000);
    testConvertDoubleTimeToDate(1.25, EXPEC_DATE_1970_01_01_060000);
    testConvertDoubleTimeToDate(1.875, EXPEC_DATE_1970_01_01_210000);
    testConvertDoubleTimeToDate(9.0, EXPEC_DATE_1970_01_01_000000);
    testConvertDoubleTimeToDate(9.25, EXPEC_DATE_1970_01_01_060000);
    testConvertDoubleTimeToDate(9.875, EXPEC_DATE_1970_01_01_210000);

    testConvertDoubleTimeToDate(-0.125, EXPEC_DATE_1970_01_01_210000);
    testConvertDoubleTimeToDate(-0.25, EXPEC_DATE_1970_01_01_180000);
    testConvertDoubleTimeToDate(-0.5, EXPEC_DATE_1970_01_01_120000);
    testConvertDoubleTimeToDate(-0.75, EXPEC_DATE_1970_01_01_060000);
    testConvertDoubleTimeToDate(-1.0, EXPEC_DATE_1970_01_01_000000);
    testConvertDoubleTimeToDate(-7.125, EXPEC_DATE_1970_01_01_210000);
    testConvertDoubleTimeToDate(-7.25, EXPEC_DATE_1970_01_01_180000);
    testConvertDoubleTimeToDate(-7.75, EXPEC_DATE_1970_01_01_060000);
  }

  /**
   * @param input
   *          Double to be converted
   * @param expectedDate
   *          with the Pattern 'yyyy-MM-dd HH:mm:ss.SSS'
   */
  private void testConvertDoubleTimeToDate(Double input, String expectedDate) {
    Date value = DateUtility.convertDoubleTimeToDate(input);
    String message = "Conversion DoubleTime <" + input + "> to Date";
    assertDateEquals(message, expectedDate, value);
  }

  @Test
  public void testConvertDateToDoubleTime() {
    //TimeField selector values:
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_000000, 0.0);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_003000, 0.020833333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_010000, 0.041666666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_013000, 0.0625);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_020000, 0.083333333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_023000, 0.104166666666666667);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_030000, 0.125);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_033000, 0.145833333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_040000, 0.166666666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_043000, 0.1875);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_050000, 0.208333333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_053000, 0.229166666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_060000, 0.25);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_063000, 0.270833333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_070000, 0.291666666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_073000, 0.3125);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_080000, 0.333333333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_083000, 0.354166666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_090000, 0.375);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_093000, 0.395833333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_100000, 0.416666666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_103000, 0.4375);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_110000, 0.458333333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_113000, 0.479166666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_120000, 0.5);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_123000, 0.520833333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_130000, 0.541666666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_133000, 0.5625);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_140000, 0.583333333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_143000, 0.604166666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_150000, 0.625);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_153000, 0.645833333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_160000, 0.666666666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_163000, 0.6875);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_170000, 0.708333333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_173000, 0.729166666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_180000, 0.75);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_183000, 0.770833333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_190000, 0.791666666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_193000, 0.8125);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_200000, 0.833333333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_203000, 0.854166666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_210000, 0.875);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_213000, 0.895833333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_220000, 0.916666666666666664);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_223000, 0.9375);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_230000, 0.958333333333333332);
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_233000, 0.979166666666666664);

    //Limit condition:
    testConvertDateToDoubleTime(EXPEC_DATE_1970_01_01_235959, 0.999988425925926);

    //Out of definition Range:
    testConvertDateToDoubleTime("1970-01-02 00:00:00.000", 0.0);
    testConvertDateToDoubleTime("1970-01-02 06:00:00.000", 0.25);

    testConvertDateToDoubleTime("1970-01-15 00:00:00.000", 0.0);
    testConvertDateToDoubleTime("1970-01-15 06:00:00.000", 0.25);

    testConvertDateToDoubleTime("1969-12-31 00:00:00.000", 0.0);
    testConvertDateToDoubleTime("1969-12-31 18:00:00.000", 0.75);

    testConvertDateToDoubleTime("1969-12-05 00:00:00.000", 0.0);
    testConvertDateToDoubleTime("1969-12-05 18:00:00.000", 0.75);

  }

  /**
   * @param input
   *          Date defined with the Pattern 'yyyy-MM-dd_HH:mm:ss.SSS'
   * @param expected
   *          Double DoubleTime as defined in {@link DateUtility#convertDateToDoubleTime(java.util.Date)}
   */
  private void testConvertDateToDoubleTime(String input, Double expected) {
    assertEquals("Conversion Date <" + input + "> to DoubleTime", expected, DateUtility.convertDateToDoubleTime(DateUtility.parse(input, YEAR_DATE_TIME_PATTERN)));
  }

  @Test
  public void testConvertDateToDoubleTimeToDate() {
    //TimeField selector values:
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_000000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_003000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_010000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_013000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_020000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_023000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_030000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_033000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_040000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_043000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_050000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_053000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_060000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_063000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_070000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_073000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_080000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_083000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_090000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_093000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_100000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_103000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_110000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_113000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_120000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_123000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_130000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_133000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_140000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_143000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_150000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_153000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_160000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_163000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_170000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_173000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_180000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_183000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_190000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_193000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_200000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_203000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_210000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_213000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_220000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_223000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_230000);
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_233000);

    //Limit condition:
    testConvertDateToDoubleTimeToDate(EXPEC_DATE_1970_01_01_235959);
  }

  /**
   * @param input
   *          Date defined with the Pattern 'yyyy-MM-dd_HH:mm:ss.SSS'
   */
  private void testConvertDateToDoubleTimeToDate(String input) {
    assertEquals("Conversion Date <" + input + "> to DoubleTime and to Date", DateUtility.parse(input, YEAR_DATE_TIME_PATTERN),
        DateUtility.convertDoubleTimeToDate(DateUtility.convertDateToDoubleTime(DateUtility.parse(input, YEAR_DATE_TIME_PATTERN))));
  }

  @Test
  public void testConvertDoubleTimeToDateToDoubleTime() {
    //TimeField selector values:
    testConvertDoubleTimeToDateToDoubleTime(0.0);
    testConvertDoubleTimeToDateToDoubleTime(0.020833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.041666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.0625);
    testConvertDoubleTimeToDateToDoubleTime(0.083333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.104166666666666667);
    testConvertDoubleTimeToDateToDoubleTime(0.125);
    testConvertDoubleTimeToDateToDoubleTime(0.145833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.166666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.1875);
    testConvertDoubleTimeToDateToDoubleTime(0.208333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.229166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.25);
    testConvertDoubleTimeToDateToDoubleTime(0.270833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.291666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.3125);
    testConvertDoubleTimeToDateToDoubleTime(0.333333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.354166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.375);
    testConvertDoubleTimeToDateToDoubleTime(0.395833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.416666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.4375);
    testConvertDoubleTimeToDateToDoubleTime(0.458333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.479166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.5);
    testConvertDoubleTimeToDateToDoubleTime(0.520833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.541666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.5625);
    testConvertDoubleTimeToDateToDoubleTime(0.583333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.604166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.625);
    testConvertDoubleTimeToDateToDoubleTime(0.645833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.666666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.6875);
    testConvertDoubleTimeToDateToDoubleTime(0.708333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.729166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.75);
    testConvertDoubleTimeToDateToDoubleTime(0.770833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.791666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.8125);
    testConvertDoubleTimeToDateToDoubleTime(0.833333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.854166666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.875);
    testConvertDoubleTimeToDateToDoubleTime(0.895833333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.916666666666666664);
    testConvertDoubleTimeToDateToDoubleTime(0.9375);
    testConvertDoubleTimeToDateToDoubleTime(0.958333333333333332);
    testConvertDoubleTimeToDateToDoubleTime(0.979166666666666664);

    //Limit condition:
    testConvertDoubleTimeToDateToDoubleTime(0.999988425925926);
  }

  /**
   * @param input
   *          Double DoubleTime as defined in {@link DateUtility#convertDateToDoubleTime(java.util.Date)}
   */
  private void testConvertDoubleTimeToDateToDoubleTime(Double input) {
    assertEquals("Conversion Date <" + input + "> to DoubleTime and to Date", input, DateUtility.convertDateToDoubleTime(DateUtility.convertDoubleTimeToDate(input)));
  }

  @Test
  public void testDaysBetween() {
    Calendar cal = Calendar.getInstance();
    Date d1, d2;

    assertEquals(-1, DateUtility.getDaysBetween(null, null));
    assertEquals(-1, DateUtility.getDaysBetween(cal.getTime(), null));
    assertEquals(-1, DateUtility.getDaysBetween(null, cal.getTime()));

    cal.set(2000, Calendar.JANUARY, 1, 0, 0);
    d1 = cal.getTime();
    cal.set(2000, Calendar.JANUARY, 2, 0, 0);
    d2 = cal.getTime();
    assertEquals(1, DateUtility.getDaysBetween(d1, d2));
    assertEquals(1, DateUtility.getDaysBetween(d2, d1));

    cal.set(2014, Calendar.FEBRUARY, 23, 0, 0);
    d1 = cal.getTime();
    cal.set(2014, Calendar.MARCH, 31, 0, 0);
    d2 = cal.getTime();
    assertEquals(36, DateUtility.getDaysBetween(d1, d2));
    assertEquals(36, DateUtility.getDaysBetween(d2, d1));

    cal.set(2000, Calendar.JANUARY, 1, 23, 59);
    d1 = cal.getTime();
    cal.set(2000, Calendar.JANUARY, 2, 0, 0);
    d2 = cal.getTime();
    assertEquals(1, DateUtility.getDaysBetween(d1, d2));
    assertEquals(1, DateUtility.getDaysBetween(d2, d1));

    assertEquals(0, DateUtility.getDaysBetween(d1, d1));
  }

  @Test
  public void testIsWeekend() {
    Calendar todayCalendar = Calendar.getInstance();

    Calendar mondayCalendar = Calendar.getInstance();
    mondayCalendar.set(2014, Calendar.OCTOBER, 6, 0, 0);

    Calendar sundayCalendar = Calendar.getInstance();
    sundayCalendar.set(2014, Calendar.OCTOBER, 5, 0, 0);

    Calendar saturdayCalendar = Calendar.getInstance();
    saturdayCalendar.set(2014, Calendar.OCTOBER, 4, 0, 0);

    Calendar fridayCalendar = Calendar.getInstance();
    fridayCalendar.set(2014, Calendar.OCTOBER, 3, 0, 0);

    Calendar thursdayCalendar = Calendar.getInstance();
    thursdayCalendar.set(2014, Calendar.OCTOBER, 2, 0, 0);

    assertTrue(DateUtility.isWeekend(thursdayCalendar.getTime(), new Locale("ar", "AF")));
    assertFalse(DateUtility.isWeekend(thursdayCalendar.getTime(), new Locale("ar", "GQ")));

    assertFalse(DateUtility.isWeekend(fridayCalendar.getTime(), Locale.US));
    assertTrue(DateUtility.isWeekend(fridayCalendar.getTime(), new Locale("ar", "AE")));

    assertTrue(DateUtility.isWeekend(sundayCalendar.getTime(), Locale.UK));
    assertFalse(DateUtility.isWeekend(sundayCalendar.getTime(), new Locale("ar", "AE")));

    assertFalse(DateUtility.isWeekend(saturdayCalendar.getTime(), new Locale("ar", "BN")));
    assertTrue(DateUtility.isWeekend(saturdayCalendar.getTime(), new Locale("ar", "AE")));
    assertFalse(DateUtility.isWeekend(saturdayCalendar.getTime(), new Locale("ar", "AF")));
    assertFalse(DateUtility.isWeekend(saturdayCalendar.getTime(), new Locale("ar", "IN")));

    assertTrue(DateUtility.isWeekend(sundayCalendar.getTime(), Locale.UK));
    assertFalse(DateUtility.isWeekend(sundayCalendar.getTime(), new Locale("ar", "AE")));

    assertFalse(DateUtility.isWeekend(mondayCalendar.getTime(), new Locale("ar", "AE")));
    assertFalse(DateUtility.isWeekend(mondayCalendar.getTime(), new Locale("ar", "AF")));
    assertFalse(DateUtility.isWeekend(mondayCalendar.getTime(), new Locale("ar", "IN")));
    assertFalse(DateUtility.isWeekend(mondayCalendar.getTime(), new Locale("ar", "BN")));
    assertFalse(DateUtility.isWeekend(mondayCalendar.getTime(), Locale.US));
    assertFalse(DateUtility.isWeekend(mondayCalendar.getTime(), Locale.UK));

    assertEquals(DateUtility.isWeekend(todayCalendar.getTime()), DateUtility.isWeekend(todayCalendar.getTime(), Locale.getDefault()));
  }

  @Test
  public void testTruncDateToHalfYear() {
    assertNull(DateUtility.truncDateToHalfYear(null));

    // 1. half year
    assertDateEquals("2015-01-01 00:00:00.000", DateUtility.truncDateToHalfYear(dateOf("2015-01-01 00:00:00.000")));
    assertDateEquals("2015-01-01 00:00:00.000", DateUtility.truncDateToHalfYear(dateOf("2015-06-30 23:59:59.999")));

    // 2. half year
    assertDateEquals("2015-07-01 00:00:00.000", DateUtility.truncDateToHalfYear(dateOf("2015-07-01 00:00:00.000")));
    assertDateEquals("2015-07-01 00:00:00.000", DateUtility.truncDateToHalfYear(dateOf("2015-12-31 23:59:59.999")));

    // leap year
    assertDateEquals("2012-01-01 00:00:00.000", DateUtility.truncDateToHalfYear(dateOf("2012-02-29 15:30:53.458")));
  }

  @Test
  public void testTruncCalendarToHalfYear() {
    DateUtility.truncCalendarToHalfYear(null);

    Calendar cal = calendarOf("2015-01-01 00:00:00.000");
    DateUtility.truncCalendarToHalfYear(cal);
    assertCalendarEquals("2015-01-01 00:00:00.000", cal);

    cal = calendarOf("2015-06-30 23:59:59.999");
    DateUtility.truncCalendarToHalfYear(cal);
    assertCalendarEquals("2015-01-01 00:00:00.000", cal);

    cal = calendarOf("2015-07-01 00:00:00.000");
    DateUtility.truncCalendarToHalfYear(cal);
    assertCalendarEquals("2015-07-01 00:00:00.000", cal);

    cal = calendarOf("2015-12-31 23:59:59.999");
    DateUtility.truncCalendarToHalfYear(cal);
    assertCalendarEquals("2015-07-01 00:00:00.000", cal);
  }

  @Test
  public void testTruncDateToQuarter() {
    assertNull(DateUtility.truncDateToQuarter(null));

    // 1. quarter
    assertDateEquals("2015-01-01 00:00:00.000", DateUtility.truncDateToQuarter(dateOf("2015-01-01 00:00:00.000")));
    assertDateEquals("2015-01-01 00:00:00.000", DateUtility.truncDateToQuarter(dateOf("2015-03-31 23:59:59.999")));

    // 2. quarter
    assertDateEquals("2015-04-01 00:00:00.000", DateUtility.truncDateToQuarter(dateOf("2015-04-01 00:00:00.000")));
    assertDateEquals("2015-04-01 00:00:00.000", DateUtility.truncDateToQuarter(dateOf("2015-06-30 23:59:59.999")));

    // 3. quarter
    assertDateEquals("2015-07-01 00:00:00.000", DateUtility.truncDateToQuarter(dateOf("2015-07-01 00:00:00.000")));
    assertDateEquals("2015-07-01 00:00:00.000", DateUtility.truncDateToQuarter(dateOf("2015-09-30 23:59:59.999")));

    // 4. quarter
    assertDateEquals("2015-10-01 00:00:00.000", DateUtility.truncDateToQuarter(dateOf("2015-10-01 00:00:00.000")));
    assertDateEquals("2015-10-01 00:00:00.000", DateUtility.truncDateToQuarter(dateOf("2015-12-31 23:59:59.999")));

    // leap year
    assertDateEquals("2012-01-01 00:00:00.000", DateUtility.truncDateToQuarter(dateOf("2012-02-29 15:30:53.458")));
  }

  @Test
  public void testTruncDateToIsoWeek() {
    assertNull(DateUtility.truncDateToIsoWeek(null));

    assertDateEquals("2014-12-29 00:00:00.000", DateUtility.truncDateToIsoWeek(dateOf("2015-01-01 00:00:00.000")));
    assertDateEquals("2014-12-29 00:00:00.000", DateUtility.truncDateToIsoWeek(dateOf("2015-01-04 00:00:00.000")));
    assertDateEquals("2015-01-05 00:00:00.000", DateUtility.truncDateToIsoWeek(dateOf("2015-01-05 00:00:00.000")));
    assertDateEquals("2014-12-29 00:00:00.000", DateUtility.truncDateToIsoWeek(dateOf("2014-12-29 00:00:00.000")));
  }

  @Test
  public void testTruncCalendarToQuarter() {
    DateUtility.truncCalendarToQuarter(null);

    // 1. quarter
    Calendar cal = calendarOf("2015-01-01 00:00:00.000");
    DateUtility.truncCalendarToQuarter(cal);
    assertCalendarEquals("2015-01-01 00:00:00.000", cal);

    cal = calendarOf("2015-03-30 23:59:59.999");
    DateUtility.truncCalendarToQuarter(cal);
    assertCalendarEquals("2015-01-01 00:00:00.000", cal);

    // 2. quarter
    cal = calendarOf("2015-04-01 00:00:00.000");
    DateUtility.truncCalendarToQuarter(cal);
    assertCalendarEquals("2015-04-01 00:00:00.000", cal);

    cal = calendarOf("2015-06-30 23:59:59.999");
    DateUtility.truncCalendarToQuarter(cal);
    assertCalendarEquals("2015-04-01 00:00:00.000", cal);

    // 3. quarter
    cal = calendarOf("2015-07-01 00:00:00.000");
    DateUtility.truncCalendarToQuarter(cal);
    assertCalendarEquals("2015-07-01 00:00:00.000", cal);

    cal = calendarOf("2015-09-30 23:59:59.999");
    DateUtility.truncCalendarToQuarter(cal);
    assertCalendarEquals("2015-07-01 00:00:00.000", cal);

    // 4. quarter
    cal = calendarOf("2015-10-01 00:00:00.000");
    DateUtility.truncCalendarToQuarter(cal);
    assertCalendarEquals("2015-10-01 00:00:00.000", cal);

    cal = calendarOf("2015-12-31 23:59:59.999");
    DateUtility.truncCalendarToQuarter(cal);
    assertCalendarEquals("2015-10-01 00:00:00.000", cal);
  }

  @Test
  public void testTruncDateToHour() {
    assertNull("Trunc <null> to hour", DateUtility.truncDateToHour(null));
    assertDateEquals("1970-01-01 00:00:00.000", DateUtility.truncDateToHour(dateOf("1970-01-01 00:01:00.000")));
    assertDateEquals("1970-01-01 00:00:00.000", DateUtility.truncDateToHour(dateOf("1970-01-01 00:59:00.000")));
    assertDateEquals("1970-01-01 10:00:00.000", DateUtility.truncDateToHour(dateOf("1970-01-01 10:43:00.000")));
    assertDateEquals("1970-01-01 13:00:00.000", DateUtility.truncDateToHour(dateOf("1970-01-01 13:43:00.000")));
    assertDateEquals("1970-01-01 23:00:00.000", DateUtility.truncDateToHour(dateOf("1970-01-01 23:00:00.000")));
    assertDateEquals("1970-01-01 23:00:00.000", DateUtility.truncDateToHour(dateOf("1970-01-01 23:59:00.000")));
    assertDateEquals("2015-01-01 00:00:00.000", DateUtility.truncDateToHour(dateOf("2015-01-01 00:00:00.000")));
    assertDateEquals("2015-01-01 18:00:00.000", DateUtility.truncDateToHour(dateOf("2015-01-01 18:17:25.831")));
    assertDateEquals("2015-01-01 23:00:00.000", DateUtility.truncDateToHour(dateOf("2015-01-01 23:59:59.999")));
  }

  @Test
  public void testTruncCalendarToHour() {
    DateUtility.truncCalendarToHour(null);

    Calendar cal = calendarOf("2015-01-01 00:00:00.000");
    DateUtility.truncCalendarToHour(cal);
    assertCalendarEquals("2015-01-01 00:00:00.000", cal);

    cal = calendarOf("2015-01-01 18:17:25.831");
    DateUtility.truncCalendarToHour(cal);
    assertCalendarEquals("2015-01-01 18:00:00.000", cal);

    cal = calendarOf("2015-01-01 23:59:59.999");
    DateUtility.truncCalendarToHour(cal);
    assertCalendarEquals("2015-01-01 23:00:00.000", cal);
  }

  public static void assertDateEquals(String expectedDate, Date date) {
    assertDateEquals(null, expectedDate, date);
  }

  public static void assertDateEquals(String message, String expectedDate, Date date) {
    assertEquals(message, expectedDate, stringOf(date));
  }

  public static void assertCalendarEquals(String expectedDate, Calendar cal) {
    assertDateEquals(expectedDate, DateUtility.convertCalendar(cal));
  }

  /**
   * Parses the given string into a {@link Calendar}.
   *
   * @param dateString
   *          staring representation using format yyyy-MM-dd HH:mm:ss.SSS
   */
  public static Calendar calendarOf(String dateString) {
    return DateUtility.convertDate(dateOf(dateString));
  }

  /**
   * Parses the given string into a {@link Date}.
   *
   * @param dateString
   *          staring representation using format yyyy-MM-dd HH:mm:ss.SSS
   */
  public static Date dateOf(String dateString) {
    return DateUtility.parse(dateString, YEAR_DATE_TIME_PATTERN);
  }

  /**
   * Formats the given date using format yyyy-MM-dd_HH:mm:ss.SSS
   */
  public static String stringOf(Date date) {
    return DateUtility.format(date, YEAR_DATE_TIME_PATTERN);
  }
}
