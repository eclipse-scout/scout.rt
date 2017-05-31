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
package org.eclipse.scout.rt.server.jaxws.adapter;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for {@link UtcDateTimeAdapter}, {@link UtcDateAdapter} and {@link UtcTimeAdapter}.
 */
public class UtcDateTimeAdapterTest {

  private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();
  private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
  private static final TimeZone DEFAULT_TEST_TIME_ZONE_UTC_PLUS_1 = TimeZone.getTimeZone("GMT+1:00");
  private static final TimeZone DEFAULT_TEST_TIME_ZONE_UTC_MINUS_1 = TimeZone.getTimeZone("GMT-1:00");

  private static final UtcDateTimeAdapter DATE_TIME_ADAPTER = new UtcDateTimeAdapter();
  private static final UtcDateAdapter DATE_ADAPTER = new UtcDateAdapter();
  private static final UtcTimeAdapter TIME_ADAPTER = new UtcTimeAdapter();

  @Before
  public void before() {
    TimeZone.setDefault(DEFAULT_TEST_TIME_ZONE_UTC_PLUS_1);
  }

  @After
  public void afer() {
    TimeZone.setDefault(DEFAULT_TIME_ZONE);
  }

  @Test
  public void testMarshall() throws Exception {
    // test UTC time
    testMarshallImpl(2011, 11, 7, 12, 50, 0, UTC_TIME_ZONE, "2011-11-07T12:50:00.000Z", "2011-11-07Z", "12:50:00.000Z");

    // test UTC time
    testMarshallImpl(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+0:00"), "2011-11-07T12:50:00.000Z", "2011-11-07Z", "12:50:00.000Z");

    // test UTC+01:00 (to the east of Greenwich; local time at UTC+01:00 is 2011-11-7 12:50)
    testMarshallImpl(2011, 11, 7, 12, 50, 0, DEFAULT_TEST_TIME_ZONE_UTC_PLUS_1, "2011-11-07T11:50:00.000Z", "2011-11-07Z", "11:50:00.000Z");

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-7 12:50)
    testMarshallImpl(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT-5:00"), "2011-11-07T17:50:00.000Z", "2011-11-07Z", "17:50:00.000Z");

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 12:50)
    testMarshallImpl(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+5:00"), "2011-11-07T07:50:00.000Z", "2011-11-07Z", "07:50:00.000Z");

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-7 00:00)
    testMarshallImpl(2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"), "2011-11-07T05:00:00.000Z", "2011-11-07Z", "05:00:00.000Z");

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 00:00)
    testMarshallImpl(2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"), "2011-11-06T19:00:00.000Z", "2011-11-07Z", "19:00:00.000Z");

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-6 23:00)
    testMarshallImpl(2011, 11, 6, 23, 0, 0, TimeZone.getTimeZone("GMT-5:00"), "2011-11-07T04:00:00.000Z", "2011-11-06Z", "04:00:00.000Z");

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-6 23:00)
    testMarshallImpl(2011, 11, 6, 23, 0, 0, TimeZone.getTimeZone("GMT+5:00"), "2011-11-06T18:00:00.000Z", "2011-11-06Z", "18:00:00.000Z");

    // test UTC+01:00 (to the east of Greenwich; local time at UTC+01:00 is 2011-11-7 00:50)
    testMarshallImpl(2011, 11, 7, 00, 50, 0, DEFAULT_TEST_TIME_ZONE_UTC_PLUS_1, "2011-11-06T23:50:00.000Z", "2011-11-07Z", "23:50:00.000Z");

    // test UTC-01:00 (to the west of Greenwich; local time at UTC-01:00 is 2011-11-7 23:50)
    testMarshallImpl(2011, 11, 6, 23, 50, 0, DEFAULT_TEST_TIME_ZONE_UTC_MINUS_1, "2011-11-07T00:50:00.000Z", "2011-11-06Z", "00:50:00.000Z");
  }

  @Test
  public void testUnmarshall() throws Exception {
    // test UTC time
    testUnmarshallImpl("2011-11-07T12:50:00.000Z", 2011, 11, 7, 12, 50, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07T12:50:00.000Z", 2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07T12:50:00.000Z", 1970, 1, 1, 12, 50, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test UTC time
    testUnmarshallImpl("2011-11-07Z", -1, 0, 0, 0, 0, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07Z", 2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07Z", -1, 0, 0, 0, 0, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test UTC time
    testUnmarshallImpl("12:50:00.000Z", -1, 0, 0, 0, 0, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("12:50:00.000Z", -1, 0, 0, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("12:50:00.000Z", 1970, 1, 1, 12, 50, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test implicit UTC time
    testUnmarshallImpl("2011-11-07T12:50:00.000", 2011, 11, 7, 12, 50, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07T12:50:00.000", 2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07T12:50:00.000", 1970, 1, 1, 12, 50, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test implicit UTC time
    testUnmarshallImpl("2011-11-07", -1, 0, 0, 0, 0, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07", 2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07", -1, 0, 0, 0, 0, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test implicit UTC time
    testUnmarshallImpl("12:50:00.000", -1, 0, 0, 0, 0, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("12:50:00.000", -1, 0, 0, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("12:50:00.000", 1970, 1, 1, 12, 50, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-7 12:50)
    testUnmarshallImpl("2011-11-07T12:50:00.000-05:00", 2011, 11, 7, 17, 50, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07T12:50:00.000-05:00", 2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07T12:50:00.000-05:00", 1970, 1, 1, 17, 50, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 12:50)
    testUnmarshallImpl("2011-11-07T12:50:00.000+05:00", 2011, 11, 7, 7, 50, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07T12:50:00.000+05:00", 2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07T12:50:00.000+05:00", 1970, 1, 1, 7, 50, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-7 00:00)
    testUnmarshallImpl("2011-11-07T00:00:00.000-05:00", 2011, 11, 7, 5, 0, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07T00:00:00.000-05:00", 2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07T00:00:00.000-05:00", 1970, 1, 1, 5, 0, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 00:00)
    testUnmarshallImpl("2011-11-07T00:00:00.000+05:00", 2011, 11, 6, 19, 00, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07T00:00:00.000+05:00", 2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07T00:00:00.000+05:00", 1970, 1, 1, 19, 00, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-6 23:00)
    testUnmarshallImpl("2011-11-06T23:00:00.000-05:00", 2011, 11, 7, 4, 0, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-06T23:00:00.000-05:00", 2011, 11, 6, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("2011-11-06T23:00:00.000-05:00", 1970, 1, 1, 4, 0, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 23:00)
    testUnmarshallImpl("2011-11-06T23:00:00.000+05:00", 2011, 11, 6, 18, 00, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-06T23:00:00.000+05:00", 2011, 11, 6, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("2011-11-06T23:00:00.000+05:00", 1970, 1, 1, 18, 00, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 23:00)
    testUnmarshallImpl("23:00:00.000-05:00", -1, 0, 0, 0, 0, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("23:00:00.000-05:00", -1, 0, 0, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("23:00:00.000-05:00", 1970, 1, 1, 4, 0, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 23:00)
    testUnmarshallImpl("23:00:00.000+05:00", -1, 0, 0, 0, 00, 0, UTC_TIME_ZONE, DATE_TIME_ADAPTER);
    testUnmarshallImpl("23:00:00.000+05:00", -1, 0, 0, 0, 0, 0, UTC_TIME_ZONE, DATE_ADAPTER);
    testUnmarshallImpl("23:00:00.000+05:00", 1970, 1, 1, 18, 00, 0, UTC_TIME_ZONE, TIME_ADAPTER);

    // Missing timezone information, default timeZone set to UTC+5:00
    // However: Time will be interpreted as UTC time, and not according to the default timezone (UTC+5:00)
    testUnmarshallImpl("2011-11-07", -1, 0, 0, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"), DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07", 2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"), DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07", -1, 0, 0, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"), TIME_ADAPTER);

    testUnmarshallImpl("2011-11-07T00:00:00.000", 2011, 11, 7, 5, 0, 0, TimeZone.getTimeZone("GMT+5:00"), DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07T00:00:00.000", 2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"), DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07T00:00:00.000", 1970, 1, 1, 5, 0, 0, TimeZone.getTimeZone("GMT+5:00"), TIME_ADAPTER);

    // Missing timezone information, default timeZone set to UTC-5:00
    // However: Time will be interpreted as UTC time, and not according to the default timezone (UTC-5:00)
    testUnmarshallImpl("2011-11-07", -1, 0, 0, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"), DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07", 2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"), DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07", -1, 0, 0, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"), TIME_ADAPTER);

    testUnmarshallImpl("2011-11-07T00:00:00.000", 2011, 11, 6, 19, 0, 0, TimeZone.getTimeZone("GMT-5:00"), DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-07T00:00:00.000", 2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"), DATE_ADAPTER);
    testUnmarshallImpl("2011-11-07T00:00:00.000", 1970, 1, 1, 19, 0, 0, TimeZone.getTimeZone("GMT-5:00"), TIME_ADAPTER);

    testUnmarshallImpl("2011-11-06T23:50:00.000Z", 2011, 11, 7, 0, 50, 0, DEFAULT_TEST_TIME_ZONE_UTC_PLUS_1, DATE_TIME_ADAPTER);
    testUnmarshallImpl("2011-11-06T23:50:00.000Z", 2011, 11, 6, 0, 0, 0, DEFAULT_TEST_TIME_ZONE_UTC_PLUS_1, DATE_ADAPTER);
    testUnmarshallImpl("2011-11-06T23:50:00.000Z", 1970, 1, 1, 0, 50, 0, DEFAULT_TEST_TIME_ZONE_UTC_PLUS_1, TIME_ADAPTER);
  }

  protected void testMarshallImpl(int year, int month, int day, int hour, int min, int sec, TimeZone timeZone, String expectedDateTimeString, String expectedDateString, String expectedTimeString) throws Exception {
    try {
      TimeZone.setDefault(timeZone);
      Date d = toDate(year, month, day, hour, min, sec);
      assertEquals(expectedDateTimeString, DATE_TIME_ADAPTER.marshal(d));
      assertEquals(expectedDateString, DATE_ADAPTER.marshal(d));
      assertEquals(expectedTimeString, TIME_ADAPTER.marshal(d));

    }
    finally {
      TimeZone.setDefault(DEFAULT_TIME_ZONE);
    }
  }

  protected Date toDate(int year, int month, int day, int hour, int min, int sec) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month - 1); // 0 based
    cal.set(Calendar.DAY_OF_MONTH, day); // 1 based
    cal.set(Calendar.HOUR_OF_DAY, hour); // 1 based
    cal.set(Calendar.MINUTE, min);
    cal.set(Calendar.SECOND, sec);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  protected void testUnmarshallImpl(String dateTimeString, int expectedYear, int expectedMonth, int expectedDay, int expectedHour, int expectedMinute, int expectedSecond, TimeZone expectedTimeZone, UtcDateTimeAdapter adapter)
      throws Exception {
    final TimeZone defaultTimeZone = TimeZone.getDefault();
    try {
      TimeZone.setDefault(expectedTimeZone);

      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(Calendar.YEAR, expectedYear);
      cal.set(Calendar.MONTH, expectedMonth - 1); // 0 based
      cal.set(Calendar.DAY_OF_MONTH, expectedDay); // 1 based
      cal.set(Calendar.HOUR_OF_DAY, expectedHour); // 1 based
      cal.set(Calendar.MINUTE, expectedMinute);
      cal.set(Calendar.SECOND, expectedSecond);
      cal.set(Calendar.MILLISECOND, 0);
      long millisUtc = cal.getTimeInMillis();

      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      format.setTimeZone(expectedTimeZone);

      Date date = adapter.unmarshal(dateTimeString);
      if (date == null) {
        assertEquals("no date/time expected with given dateTimeString", -1, expectedYear);
      }
      else {
        // assert millis
        assertEquals(millisUtc, date.getTime());

        // assert format
        assertEquals(StringUtility.lpad(expectedYear + "", "0", 4) + "-"
            + StringUtility.lpad(expectedMonth + "", "0", 2) + "-"
            + StringUtility.lpad(expectedDay + "", "0", 2) + " "
            + StringUtility.lpad(expectedHour + "", "0", 2) + ":"
            + StringUtility.lpad(expectedMinute + "", "0", 2) + ":"
            + StringUtility.lpad(expectedSecond + "", "0", 2),
            format.format(date));
      }
    }
    finally {
      TimeZone.setDefault(defaultTimeZone);
    }
  }
}
