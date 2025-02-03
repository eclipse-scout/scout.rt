/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class JsonDateTest {

  @Test
  public void testJavaDateToJsonString() {
    assertNull(new JsonDate((Date) null).asJsonString());

    Calendar cal = Calendar.getInstance();
    cal.set(2015, 8, 24, 17, 38, 9);
    cal.set(Calendar.MILLISECOND, 579);
    assertEquals("2015-09-24 17:38:09.579", new JsonDate(cal.getTime()).asJsonString());

    cal = Calendar.getInstance();
    cal.set(2015, 8, 24, 17, 38, 9);
    cal.set(Calendar.MILLISECOND, 0);
    assertEquals("2015-09-24 17:38:09.000", new JsonDate(cal.getTime()).asJsonString());
  }

  @Test
  public void testJavaDateToJsonStringWithUTC() {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0400"));

    cal.set(2015, 8, 24, 17, 38, 9);
    cal.set(Calendar.MILLISECOND, 579);
    assertEquals("2015-09-24 13:38:09.579Z", new JsonDate(cal.getTime()).asJsonString(true));
  }

  @Test
  public void testJsonStringToJavaDate() {
    assertNull(new JsonDate((String) null).asJavaDate());
    assertNull(new JsonDate("").asJavaDate());

    Date date = new JsonDate("2015-09-24 17:38:09.579").asJavaDate();
    assertNotNull(date);
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    assertEquals(2015, cal.get(Calendar.YEAR));
    assertEquals(8, cal.get(Calendar.MONTH));
    assertEquals(24, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(17, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(38, cal.get(Calendar.MINUTE));
    assertEquals(9, cal.get(Calendar.SECOND));
    assertEquals(579, cal.get(Calendar.MILLISECOND));

    // Date only
    date = new JsonDate("2015-09-24").asJavaDate();
    assertNotNull(date);
    cal = Calendar.getInstance();
    cal.setTime(date);
    assertEquals(2015, cal.get(Calendar.YEAR));
    assertEquals(8, cal.get(Calendar.MONTH));
    assertEquals(24, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, cal.get(Calendar.MINUTE));
    assertEquals(0, cal.get(Calendar.SECOND));
    assertEquals(0, cal.get(Calendar.MILLISECOND));

    // Time only
    date = new JsonDate("17:38:09.579").asJavaDate();
    assertNotNull(date);
    cal = Calendar.getInstance();
    cal.setTime(date);
    assertEquals(1970, cal.get(Calendar.YEAR));
    assertEquals(0, cal.get(Calendar.MONTH));
    assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(17, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(38, cal.get(Calendar.MINUTE));
    assertEquals(9, cal.get(Calendar.SECOND));
    assertEquals(579, cal.get(Calendar.MILLISECOND));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testJsonStringToJavaDate_ExceptionInvalidString() {
    new JsonDate("Invalid Date String").asJavaDate();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testJsonStringToJavaDate_ExceptionValueTooShort() {
    new JsonDate("17:38").asJavaDate();
  }

  @Test
  public void testJsonStringToJavaDateWithUTC() {
    Date date = new JsonDate("2015-09-24 13:38:09.579Z").asJavaDate();
    assertNotNull(date);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0400"));
    cal.setTime(date);
    assertEquals(2015, cal.get(Calendar.YEAR));
    assertEquals(8, cal.get(Calendar.MONTH));
    assertEquals(24, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(17, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(38, cal.get(Calendar.MINUTE));
    assertEquals(9, cal.get(Calendar.SECOND));
    assertEquals(579, cal.get(Calendar.MILLISECOND));

    // Date only
    date = new JsonDate("2015-09-24Z").asJavaDate();
    assertNotNull(date);
    cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-0500"));
    cal.setTime(date);
    assertEquals(2015, cal.get(Calendar.YEAR));
    assertEquals(8, cal.get(Calendar.MONTH));
    assertEquals(23, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(19, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, cal.get(Calendar.MINUTE));
    assertEquals(0, cal.get(Calendar.SECOND));
    assertEquals(0, cal.get(Calendar.MILLISECOND));

    // Time only
    date = new JsonDate("02:36:18.942Z").asJavaDate();
    assertNotNull(date);
    cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-0300"));
    cal.setTime(date);
    assertEquals(1969, cal.get(Calendar.YEAR));
    assertEquals(11, cal.get(Calendar.MONTH));
    assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(36, cal.get(Calendar.MINUTE));
    assertEquals(18, cal.get(Calendar.SECOND));
    assertEquals(942, cal.get(Calendar.MILLISECOND));
  }

  @Test
  public void testMultipleConversions() {
    Calendar cal = Calendar.getInstance();
    cal.set(1848, 7, 1, 5, 6, 7);
    cal.set(Calendar.MILLISECOND, 890);

    Date date1 = cal.getTime();
    String jsonDate1 = new JsonDate(date1).asJsonString();
    Date date2 = new JsonDate(jsonDate1).asJavaDate();
    String jsonDate2 = new JsonDate(date2).asJsonString();

    assertEquals(date1, date2);
    assertEquals(jsonDate1, jsonDate2);
    assertEquals("1848-08-01 05:06:07.890", jsonDate2);
  }
}
