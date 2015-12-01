/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
    UtcDateTimeAdapter dateTimeAdapter = new UtcDateTimeAdapter();
    UtcDateAdapter dateAdapter = new UtcDateAdapter();
    UtcTimeAdapter timeAdapter = new UtcTimeAdapter();

    // test UTC time
    Calendar calendar = toCalendar(2011, 11, 7, 12, 50, 0, UTC_TIME_ZONE);
    assertEquals("2011-11-07T12:50:00.000Z", dateTimeAdapter.marshal(calendar.getTime()));
    assertEquals("2011-11-07Z", dateAdapter.marshal(calendar.getTime()));
    assertEquals("12:50:00.000Z", timeAdapter.marshal(calendar.getTime()));

    // test UTC time
    calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+0:00"));
    assertEquals("2011-11-07T12:50:00.000Z", dateTimeAdapter.marshal(calendar.getTime()));
    assertEquals("2011-11-07Z", dateAdapter.marshal(calendar.getTime()));
    assertEquals("12:50:00.000Z", timeAdapter.marshal(calendar.getTime()));

    // test UTC+01:00 (to the east of Greenwich; local time at UTC+01:00 is 2011-11-7 12:50)
    calendar = toCalendar(2011, 11, 7, 12, 50, 0, DEFAULT_TEST_TIME_ZONE_UTC_PLUS_1);
    assertEquals("2011-11-07T11:50:00.000Z", dateTimeAdapter.marshal(calendar.getTime()));
    assertEquals("2011-11-07Z", dateAdapter.marshal(calendar.getTime()));
    assertEquals("11:50:00.000Z", timeAdapter.marshal(calendar.getTime()));

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-7 12:50)
    calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertEquals("2011-11-07T17:50:00.000Z", dateTimeAdapter.marshal(calendar.getTime()));
    assertEquals("2011-11-07Z", dateAdapter.marshal(calendar.getTime()));
    assertEquals("17:50:00.000Z", timeAdapter.marshal(calendar.getTime()));

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 12:50)
    calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertEquals("2011-11-07T07:50:00.000Z", dateTimeAdapter.marshal(calendar.getTime()));
    assertEquals("2011-11-07Z", dateAdapter.marshal(calendar.getTime()));
    assertEquals("07:50:00.000Z", timeAdapter.marshal(calendar.getTime()));

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-7 00:00)
    calendar = toCalendar(2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertEquals("2011-11-07T05:00:00.000Z", dateTimeAdapter.marshal(calendar.getTime()));
    assertEquals("2011-11-07Z", dateAdapter.marshal(calendar.getTime()));
    assertEquals("05:00:00.000Z", timeAdapter.marshal(calendar.getTime()));

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 00:00)
    calendar = toCalendar(2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertEquals("2011-11-06T19:00:00.000Z", dateTimeAdapter.marshal(calendar.getTime()));
    assertEquals("2011-11-06Z", dateAdapter.marshal(calendar.getTime()));
    assertEquals("19:00:00.000Z", timeAdapter.marshal(calendar.getTime()));

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-6 23:00)
    calendar = toCalendar(2011, 11, 6, 23, 0, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertEquals("2011-11-07T04:00:00.000Z", dateTimeAdapter.marshal(calendar.getTime()));
    assertEquals("2011-11-07Z", dateAdapter.marshal(calendar.getTime()));
    assertEquals("04:00:00.000Z", timeAdapter.marshal(calendar.getTime()));

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-6 23:00)
    calendar = toCalendar(2011, 11, 6, 23, 0, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertEquals("2011-11-06T18:00:00.000Z", dateTimeAdapter.marshal(calendar.getTime()));
    assertEquals("2011-11-06Z", dateAdapter.marshal(calendar.getTime()));
    assertEquals("18:00:00.000Z", timeAdapter.marshal(calendar.getTime()));
  }

  @Test
  public void testUnmarshall() throws Exception {
    UtcDateTimeAdapter dateTimeAdapter = new UtcDateTimeAdapter();
    UtcDateAdapter dateAdapter = new UtcDateAdapter();
    UtcTimeAdapter timeAdapter = new UtcTimeAdapter();

    // test UTC time
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-07T12:50:00.000Z"),
        2011, 11, 7, 12, 50, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("2011-11-07T12:50:00.000Z"),
        2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("2011-11-07T12:50:00.000Z"),
        1970, 1, 1, 12, 50, 0, UTC_TIME_ZONE);

    // test UTC time
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-07Z"),
        2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("2011-11-07Z"),
        2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("2011-11-07Z"),
        1970, 1, 1, 0, 0, 0, UTC_TIME_ZONE);

    // test UTC time
    assertDateTime(dateTimeAdapter.unmarshal("12:50:00.000Z"),
        1970, 1, 1, 12, 50, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("12:50:00.000Z"),
        1970, 1, 1, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("12:50:00.000Z"),
        1970, 1, 1, 12, 50, 0, UTC_TIME_ZONE);

    // test implicit UTC time
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-07T12:50:00.000"),
        2011, 11, 7, 12, 50, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("2011-11-07T12:50:00.000"),
        2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("2011-11-07T12:50:00.000"),
        1970, 1, 1, 12, 50, 0, UTC_TIME_ZONE);

    // test implicit UTC time
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-07"),
        2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("2011-11-07"),
        2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("2011-11-07"),
        1970, 1, 1, 0, 0, 0, UTC_TIME_ZONE);

    // test implicit UTC time
    assertDateTime(dateTimeAdapter.unmarshal("12:50:00.000"),
        1970, 1, 1, 12, 50, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("12:50:00.000"),
        1970, 1, 1, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("12:50:00.000"),
        1970, 1, 1, 12, 50, 0, UTC_TIME_ZONE);

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-7 12:50)
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-07T12:50:00.000-05:00"),
        2011, 11, 7, 17, 50, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("2011-11-07T12:50:00.000-05:00"),
        2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("2011-11-07T12:50:00.000-05:00"),
        1970, 1, 1, 17, 50, 0, UTC_TIME_ZONE);

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 12:50)
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-07T12:50:00.000+05:00"),
        2011, 11, 7, 7, 50, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("2011-11-07T12:50:00.000+05:00"),
        2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("2011-11-07T12:50:00.000+05:00"),
        1970, 1, 1, 7, 50, 0, UTC_TIME_ZONE);

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-7 00:00)
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-07T00:00:00.000-05:00"),
        2011, 11, 7, 5, 0, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("2011-11-07T00:00:00.000-05:00"),
        2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("2011-11-07T00:00:00.000-05:00"),
        1970, 1, 1, 5, 0, 0, UTC_TIME_ZONE);

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 00:00)
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-07T00:00:00.000+05:00"),
        2011, 11, 6, 19, 00, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("2011-11-07T00:00:00.000+05:00"),
        2011, 11, 6, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("2011-11-07T00:00:00.000+05:00"),
        1970, 1, 1, 19, 00, 0, UTC_TIME_ZONE);

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-6 23:00)
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-06T23:00:00.000-05:00"),
        2011, 11, 7, 4, 0, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("2011-11-06T23:00:00.000-05:00"),
        2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("2011-11-06T23:00:00.000-05:00"),
        1970, 1, 1, 4, 0, 0, UTC_TIME_ZONE);

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 23:00)
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-06T23:00:00.000+05:00"),
        2011, 11, 6, 18, 00, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("2011-11-06T23:00:00.000+05:00"),
        2011, 11, 6, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("2011-11-06T23:00:00.000+05:00"),
        1970, 1, 1, 18, 00, 0, UTC_TIME_ZONE);

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 23:00)
    assertDateTime(dateTimeAdapter.unmarshal("23:00:00.000-05:00"),
        1970, 1, 2, 4, 0, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("23:00:00.000-05:00"),
        1970, 1, 2, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("23:00:00.000-05:00"),
        1970, 1, 1, 4, 0, 0, UTC_TIME_ZONE);

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 23:00)
    assertDateTime(dateTimeAdapter.unmarshal("23:00:00.000+05:00"),
        1970, 1, 1, 18, 00, 0, UTC_TIME_ZONE);
    assertDateTime(dateAdapter.unmarshal("23:00:00.000+05:00"),
        1970, 1, 1, 0, 0, 0, UTC_TIME_ZONE);
    assertDateTime(timeAdapter.unmarshal("23:00:00.000+05:00"),
        1970, 1, 1, 18, 00, 0, UTC_TIME_ZONE);

    // Missing timezone information, default timeZone set to UTC+5:00
    // However: Time will be interpreted as UTC time, and not according to the default timezone (UTC+5:00)
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+5:00"));
    try {
      assertDateTime(dateTimeAdapter.unmarshal("2011-11-07"),
          2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
      assertDateTime(dateAdapter.unmarshal("2011-11-07"),
          2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
      assertDateTime(timeAdapter.unmarshal("2011-11-07"),
          1970, 1, 1, 0, 0, 0, UTC_TIME_ZONE);

      assertDateTime(dateTimeAdapter.unmarshal("2011-11-07T00:00:00.000"),
          2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
      assertDateTime(dateAdapter.unmarshal("2011-11-07T00:00:000"),
          2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
      assertDateTime(timeAdapter.unmarshal("2011-11-07T00:00:00.000"),
          1970, 1, 1, 0, 0, 0, UTC_TIME_ZONE);
    }
    finally {
      TimeZone.setDefault(DEFAULT_TIME_ZONE);
    }

    // Missing timezone information, default timeZone set to UTC-5:00
    // However: Time will be interpreted as UTC time, and not according to the default timezone (UTC+5:00)
    TimeZone.setDefault(TimeZone.getTimeZone("GMT-5:00"));
    try {
      assertDateTime(dateTimeAdapter.unmarshal("2011-11-07"),
          2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
      assertDateTime(dateAdapter.unmarshal("2011-11-07"),
          2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
      assertDateTime(timeAdapter.unmarshal("2011-11-07"),
          1970, 1, 1, 0, 0, 0, UTC_TIME_ZONE);

      assertDateTime(dateTimeAdapter.unmarshal("2011-11-07T00:00:00.000"),
          2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
      assertDateTime(dateAdapter.unmarshal("2011-11-07T00:00:000"),
          2011, 11, 7, 0, 0, 0, UTC_TIME_ZONE);
      assertDateTime(timeAdapter.unmarshal("2011-11-07T00:00:00.000"),
          1970, 1, 1, 0, 0, 0, UTC_TIME_ZONE);
    }
    finally {
      TimeZone.setDefault(DEFAULT_TIME_ZONE);
    }
  }

  private Calendar toCalendar(int year, int month, int day, int hour, int min, int sec, TimeZone timeZone) {
    Calendar cal = Calendar.getInstance(timeZone);
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month - 1); // 0 based
    cal.set(Calendar.DAY_OF_MONTH, day); // 1 based
    cal.set(Calendar.HOUR_OF_DAY, hour); // 1 based
    cal.set(Calendar.MINUTE, min);
    cal.set(Calendar.SECOND, sec);
    cal.set(Calendar.MILLISECOND, 0);
    return cal;
  }

  private void assertDateTime(Date date, int expectedYear, int expectedMonth, int expectedDay, int expectedHour, int expectedMinute, int expectedSecond, TimeZone expectedTimeZone) {
    // assert millis
    long millisUtc = toCalendar(expectedYear, expectedMonth, expectedDay, expectedHour, expectedMinute, expectedSecond, expectedTimeZone).getTimeInMillis();
    assertEquals(millisUtc, date.getTime());

    // assert format
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    format.setTimeZone(expectedTimeZone);
    format.format(date);
    assertEquals(StringUtility.lpad(expectedYear + "", "0", 4) + "-"
        + StringUtility.lpad(expectedMonth + "", "0", 2) + "-"
        + StringUtility.lpad(expectedDay + "", "0", 2) + " "
        + StringUtility.lpad(expectedHour + "", "0", 2) + ":"
        + StringUtility.lpad(expectedMinute + "", "0", 2) + ":"
        + StringUtility.lpad(expectedSecond + "", "0", 2),
        format.format(date));
  }
}
