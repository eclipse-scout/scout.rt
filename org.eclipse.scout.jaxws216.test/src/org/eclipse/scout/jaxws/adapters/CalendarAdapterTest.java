/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.adapters;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link CalendarAdapter}
 */
public class CalendarAdapterTest {

  @Test
  public void testMarshall() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    try {
      // set default timeZone to UTC+1:00
      TimeZone.setDefault(TimeZone.getTimeZone("GMT+1:00")); // Berlin, Stockholm, Bern

      CalendarAdapter adapter = new CalendarAdapter();
      // test default time (UTC+01:00)
      Calendar calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getDefault());
      Assert.assertEquals("2011-11-07T12:50:00.000+01:00", adapter.marshal(calendar));

      // test local time (UTC+01:00)
      calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+1:00"));
      Assert.assertEquals("2011-11-07T12:50:00.000+01:00", adapter.marshal(calendar));

      // test time from other timezone than local (UTC-05:00)
      calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT-5:00"));
      Assert.assertEquals("2011-11-07T12:50:00.000-05:00", adapter.marshal(calendar));

      // test time from other timezone than local (UTC+05:00)
      calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+5:00"));
      Assert.assertEquals("2011-11-07T12:50:00.000+05:00", adapter.marshal(calendar));

      // test UTC time
      calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+0:00"));
      Assert.assertEquals("2011-11-07T12:50:00.000Z", adapter.marshal(calendar));

      // test UTC time
      calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("UTC"));
      Assert.assertEquals("2011-11-07T12:50:00.000Z", adapter.marshal(calendar));
    }
    finally {
      TimeZone.setDefault(defaultTimeZone);
    }
  }

  @Test
  public void testUnmarshall() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    try {
      // set default timeZone to UTC+1:00
      TimeZone.setDefault(TimeZone.getTimeZone("GMT+1:00")); // Berlin, Stockholm, Bern

      CalendarAdapter adapter = new CalendarAdapter();

      // test UTC time
      Calendar calendar = adapter.unmarshal("2011-11-07T12:50:00.000Z");
      assertDateTime(calendar, 2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+00:00"));

      calendar = adapter.unmarshal("2011-11-07T12:50:00.000Z");
      assertDateTime(calendar, 2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT-00:00"));

      calendar = adapter.unmarshal("2011-11-07T12:50:00.000Z");
      assertDateTime(calendar, 2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("UTC"));

      // test local time (UTC+01:00)
      calendar = adapter.unmarshal("2011-11-07T12:50:00.000+01:00");
      assertDateTime(calendar, 2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+1:00"));

      // test local time (UTC+01:00)
      calendar = adapter.unmarshal("2011-11-07T12:50:00.000");
      assertDateTime(calendar, 2011, 11, 7, 12, 50, 0, TimeZone.getDefault());

      // test time from other timezone than local (UTC-05:00)
      calendar = adapter.unmarshal("2011-11-07T12:50:00.000-05:00");
      assertDateTime(calendar, 2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT-5:00"));

      // test time from other timezone than local (UTC+05:00)
      calendar = adapter.unmarshal("2011-11-07T12:50:00.000+05:00");
      assertDateTime(calendar, 2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+5:00"));
    }
    finally {
      TimeZone.setDefault(defaultTimeZone);
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
    Assert.assertEquals(millisUtc, calendar.getTimeInMillis());

    // assert fields
    Assert.assertEquals(calendar.get(Calendar.YEAR), expectedYear);
    Assert.assertEquals(calendar.get(Calendar.MONTH) + 1, expectedMonth); // 0-based
    Assert.assertEquals(calendar.get(Calendar.DAY_OF_MONTH), expectedDay);
    Assert.assertEquals(calendar.get(Calendar.HOUR_OF_DAY), expectedHour);
    Assert.assertEquals(calendar.get(Calendar.MINUTE), expectedMinute);
    Assert.assertEquals(calendar.get(Calendar.SECOND), expectedSecond);
    Assert.assertEquals(calendar.getTimeZone().getRawOffset(), expectedTimeZone.getRawOffset());
  }
}
