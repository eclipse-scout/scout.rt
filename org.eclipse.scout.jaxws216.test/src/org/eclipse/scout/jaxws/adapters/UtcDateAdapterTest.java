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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.scout.commons.StringUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit test for {@link UtcDateAdapter}
 */
public class UtcDateAdapterTest {

  @Test
  public void testMarshall() throws Exception {
    UtcDateAdapter adapter = new UtcDateAdapter();
    // test UTC time
    Calendar calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("UTC"));
    Assert.assertEquals("2011-11-07T12:50:00.000Z", adapter.marshal(calendar.getTime()));

    // test UTC time
    calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+0:00"));
    Assert.assertEquals("2011-11-07T12:50:00.000Z", adapter.marshal(calendar.getTime()));

    // test time from other timezone than UTC (UTC-05:00)
    calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT-5:00"));
    Assert.assertEquals("2011-11-07T17:50:00.000Z", adapter.marshal(calendar.getTime()));

    // test time from other timezone than UTC (UTC+05:00)
    calendar = toCalendar(2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("GMT+5:00"));
    Assert.assertEquals("2011-11-07T07:50:00.000Z", adapter.marshal(calendar.getTime()));

  }

  @Test
  public void testUnmarshall() throws Exception {
    UtcDateAdapter adapter = new UtcDateAdapter();

    Date date = adapter.unmarshal("2011-11-07T12:50:00.000Z");
    assertDateTime(date, 2011, 11, 7, 12, 50, 0, TimeZone.getTimeZone("UTC"));

    date = adapter.unmarshal("2011-11-07T12:50:00.000-05:00");
    assertDateTime(date, 2011, 11, 7, 17, 50, 0, TimeZone.getTimeZone("UTC"));

    date = adapter.unmarshal("2011-11-07T12:50:00.000+05:00");
    assertDateTime(date, 2011, 11, 7, 7, 50, 0, TimeZone.getTimeZone("UTC"));

    date = adapter.unmarshal("2011-11-07Z");
    assertDateTime(date, 2011, 11, 7, 0, 0, 0, TimeZone.getTimeZone("UTC"));

    TimeZone defaultTimeZone = TimeZone.getDefault();
    try {
      // set default timeZone to UTC+1:00
      TimeZone.setDefault(TimeZone.getTimeZone("GMT+1:00")); // Berlin, Stockholm, Bern
      date = adapter.unmarshal("2011-11-07");
      assertDateTime(date, 2011, 11, 6, 23, 0, 0, TimeZone.getTimeZone("UTC"));
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

  private void assertDateTime(Date date, int expectedYear, int expectedMonth, int expectedDay, int expectedHour, int expectedMinute, int expectedSecond, TimeZone expectedTimeZone) {
    // assert millis
    long millisUtc = toCalendar(expectedYear, expectedMonth, expectedDay, expectedHour, expectedMinute, expectedSecond, expectedTimeZone).getTimeInMillis();
    Assert.assertEquals(millisUtc, date.getTime());

    // assert format
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    format.setTimeZone(expectedTimeZone);
    format.format(date);
    Assert.assertEquals(expectedYear + "-" +
        StringUtility.lpad(expectedMonth + "", "0", 2) + "-" +
        StringUtility.lpad(expectedDay + "", "0", 2) + " " +
        StringUtility.lpad(expectedHour + "", "0", 2) + ":" +
        StringUtility.lpad(expectedMinute + "", "0", 2) + ":" +
        StringUtility.lpad(expectedSecond + "", "0", 2),
        format.format(date));
  }
}
