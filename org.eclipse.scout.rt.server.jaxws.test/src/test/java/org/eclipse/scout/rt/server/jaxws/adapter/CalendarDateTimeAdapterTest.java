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

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for {@link CalendarDateTimeAdapter}, {@link CalendarDateAdapter} and {@link CalendarTimeAdapter}.
 */
public class CalendarDateTimeAdapterTest {

  private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();
  private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

  @Before
  public void before() {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+1:00"));
  }

  @After
  public void afer() {
    TimeZone.setDefault(DEFAULT_TIME_ZONE);
  }

  @Test
  public void testMarshall() throws Exception {
    CalendarDateTimeAdapter dateTimeAdapter = new CalendarDateTimeAdapter();
    CalendarDateTimeAdapter dateAdapter = new CalendarDateAdapter();
    CalendarTimeAdapter timeAdapter = new CalendarTimeAdapter();

    // test UTC time
    Calendar calendar = toCalendar(2011, 11, 7, 12, 50, 0, UTC_TIME_ZONE);
    assertEquals("2011-11-07T12:50:00.000Z", dateTimeAdapter.marshal(calendar));
    assertEquals("2011-11-07Z", dateAdapter.marshal(calendar));
    assertEquals("12:50:00.000Z", timeAdapter.marshal(calendar));

    // test UTC time
    calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+0:00"));
    assertEquals("2011-11-07T12:50:00.000Z", dateTimeAdapter.marshal(calendar));
    assertEquals("2011-11-07Z", dateAdapter.marshal(calendar));
    assertEquals("12:50:00.000Z", timeAdapter.marshal(calendar));

    // test UTC+01:00 (to the east of Greenwich; local time at UTC+01:00 is 2011-11-7 12:50)
    calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+1:00"));
    assertEquals("2011-11-07T12:50:00.000+01:00", dateTimeAdapter.marshal(calendar));
    assertEquals("2011-11-07+01:00", dateAdapter.marshal(calendar));
    assertEquals("12:50:00.000+01:00", timeAdapter.marshal(calendar));

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-7 12:50)
    calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertEquals("2011-11-07T12:50:00.000-05:00", dateTimeAdapter.marshal(calendar));
    assertEquals("2011-11-07-05:00", dateAdapter.marshal(calendar));
    assertEquals("12:50:00.000-05:00", timeAdapter.marshal(calendar));

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 12:50)
    calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertEquals("2011-11-07T12:50:00.000+05:00", dateTimeAdapter.marshal(calendar));
    assertEquals("2011-11-07+05:00", dateAdapter.marshal(calendar));
    assertEquals("12:50:00.000+05:00", timeAdapter.marshal(calendar));

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-7 00:00)
    calendar = toCalendar(2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertEquals("2011-11-07T00:00:00.000-05:00", dateTimeAdapter.marshal(calendar));
    assertEquals("2011-11-07-05:00", dateAdapter.marshal(calendar));
    assertEquals("00:00:00.000-05:00", timeAdapter.marshal(calendar));

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 00:00)
    calendar = toCalendar(2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertEquals("2011-11-07T00:00:00.000+05:00", dateTimeAdapter.marshal(calendar));
    assertEquals("2011-11-07+05:00", dateAdapter.marshal(calendar));
    assertEquals("00:00:00.000+05:00", timeAdapter.marshal(calendar));

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-6 23:00)
    calendar = toCalendar(2011, 11, 6, 23, 0, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertEquals("2011-11-06T23:00:00.000-05:00", dateTimeAdapter.marshal(calendar));
    assertEquals("2011-11-06-05:00", dateAdapter.marshal(calendar));
    assertEquals("23:00:00.000-05:00", timeAdapter.marshal(calendar));

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-6 23:00)
    calendar = toCalendar(2011, 11, 6, 23, 0, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertEquals("2011-11-06T23:00:00.000+05:00", dateTimeAdapter.marshal(calendar));
    assertEquals("2011-11-06+05:00", dateAdapter.marshal(calendar));
    assertEquals("23:00:00.000+05:00", timeAdapter.marshal(calendar));
  }

  @Test
  public void testUnmarshall() throws Exception {
    CalendarDateTimeAdapter dateTimeAdapter = new CalendarDateTimeAdapter();
    CalendarDateTimeAdapter dateAdapter = new CalendarDateAdapter();
    CalendarTimeAdapter timeAdapter = new CalendarTimeAdapter();

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
        2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertDateTime(dateAdapter.unmarshal("2011-11-07T12:50:00.000-05:00"),
        2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertDateTime(timeAdapter.unmarshal("2011-11-07T12:50:00.000-05:00"),
        1970, 1, 1, 12, 50, 0, TimeZone.getTimeZone("GMT-5:00"));

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 12:50)
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-07T12:50:00.000+05:00"),
        2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertDateTime(dateAdapter.unmarshal("2011-11-07T12:50:00.000+05:00"),
        2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertDateTime(timeAdapter.unmarshal("2011-11-07T12:50:00.000+05:00"),
        1970, 1, 1, 12, 50, 0, TimeZone.getTimeZone("GMT+5:00"));

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-7 00:00)
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-07T00:00:00.000-05:00"),
        2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertDateTime(dateAdapter.unmarshal("2011-11-07T00:00:00.000-05:00"),
        2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertDateTime(timeAdapter.unmarshal("2011-11-07T00:00:00.000-05:00"),
        1970, 1, 1, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"));

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 00:00)
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-07T00:00:00.000+05:00"),
        2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertDateTime(dateAdapter.unmarshal("2011-11-07T00:00:00.000+05:00"),
        2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertDateTime(timeAdapter.unmarshal("2011-11-07T00:00:00.000+05:00"),
        1970, 1, 1, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"));

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-6 23:00)
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-06T23:00:00.000-05:00"),
        2011, 11, 6, 23, 0, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertDateTime(dateAdapter.unmarshal("2011-11-06T23:00:00.000-05:00"),
        2011, 11, 6, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertDateTime(timeAdapter.unmarshal("2011-11-06T23:00:00.000-05:00"),
        1970, 1, 1, 23, 0, 0, TimeZone.getTimeZone("GMT-5:00"));

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 23:00)
    assertDateTime(dateTimeAdapter.unmarshal("2011-11-06T23:00:00.000+05:00"),
        2011, 11, 6, 23, 00, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertDateTime(dateAdapter.unmarshal("2011-11-06T23:00:00.000+05:00"),
        2011, 11, 6, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertDateTime(timeAdapter.unmarshal("2011-11-06T23:00:00.000+05:00"),
        1970, 1, 1, 23, 00, 0, TimeZone.getTimeZone("GMT+5:00"));

    // test UTC-05:00 (to the WEST of Greenwich; local time at UTC-05:00 is 2011-11-6 23:00)
    assertDateTime(dateTimeAdapter.unmarshal("23:00:00.000-05:00"),
        1970, 1, 1, 23, 0, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertDateTime(dateAdapter.unmarshal("23:00:00.000-05:00"),
        1970, 1, 1, 0, 0, 0, TimeZone.getTimeZone("GMT-5:00"));
    assertDateTime(timeAdapter.unmarshal("23:00:00.000-05:00"),
        1970, 1, 1, 23, 0, 0, TimeZone.getTimeZone("GMT-5:00"));

    // test UTC+05:00 (to the EAST of Greenwich; local time at UTC+05:00 is 2011-11-7 23:00)
    assertDateTime(dateTimeAdapter.unmarshal("23:00:00.000+05:00"),
        1970, 1, 1, 23, 00, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertDateTime(dateAdapter.unmarshal("23:00:00.000+05:00"),
        1970, 1, 1, 0, 0, 0, TimeZone.getTimeZone("GMT+5:00"));
    assertDateTime(timeAdapter.unmarshal("23:00:00.000+05:00"),
        1970, 1, 1, 23, 00, 0, TimeZone.getTimeZone("GMT+5:00"));

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

  private void assertDateTime(Calendar calendar, int expectedYear, int expectedMonth, int expectedDay, int expectedHour, int expectedMinute, int expectedSecond, TimeZone expectedTimeZone) {
    Calendar expectedCalendar = toCalendar(expectedYear, expectedMonth, expectedDay, expectedHour, expectedMinute, expectedSecond, expectedTimeZone);

    // assert millis
    long millisUtc = expectedCalendar.getTimeInMillis();
    assertEquals(millisUtc, calendar.getTimeInMillis());

    // assert fields
    assertEquals(calendar.get(Calendar.YEAR), expectedYear);
    assertEquals(calendar.get(Calendar.MONTH) + 1, expectedMonth); // 0-based
    assertEquals(calendar.get(Calendar.DAY_OF_MONTH), expectedDay);
    assertEquals(calendar.get(Calendar.HOUR_OF_DAY), expectedHour);
    assertEquals(calendar.get(Calendar.MINUTE), expectedMinute);
    assertEquals(calendar.get(Calendar.SECOND), expectedSecond);
    assertEquals(calendar.getTimeZone().getRawOffset(), expectedTimeZone.getRawOffset());
  }
}
