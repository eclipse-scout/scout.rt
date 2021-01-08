/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataformat.ical.builder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.scout.rt.dataformat.ical.ICalBean;
import org.eclipse.scout.rt.dataformat.ical.model.ICalVCardHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ICalBuilderTest {
  private static List<IBean<?>> mockedBeans;
  private static Date s_mockDate;
  private TimeZone m_testingTimeZone = TimeZone.getTimeZone("GMT+3");

  @BeforeClass
  public static void beforeClass() {
    s_mockDate = BEANS.get(IDateProvider.class).currentSeconds();
    IDateProvider mock = mock(IDateProvider.class);
    when(mock.currentMillis()).thenReturn(s_mockDate);

    mockedBeans = BeanTestingHelper.get().registerBeans(new BeanMetaData(IDateProvider.class, mock));
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBeans(mockedBeans);
  }

  @Test
  public void testMeetingWithAlarm() {
    final String uid = "5edf8af0-5347-4331-ae0f-3b4980180b37";
    ICalVCardHelper helper = BEANS.get(ICalVCardHelper.class);
    Date startDate = createTestDate(14, 30, 0);
    Date endDate = DateUtility.addHours(startDate, 1);

    ICalBean cal = BEANS.get(ICalBuilder.class)
        .withProductIdentifier("IDENTIFIER")
        .withComponent(BEANS.get(ICalVEventBuilder.class)
            .withDescription("description")
            .withLocation("location")
            .withAlarm(Duration.ofDays(1), "ALARMDESC")
            .withSummary("summary")
            .withScheduling(startDate, endDate, false, m_testingTimeZone)
            .withUid(uid))
        .build();

    String expectedContent = "BEGIN:VCALENDAR\r\n" +
        "VERSION:2.0\r\n" +
        "PRODID;CHARSET=utf-8:IDENTIFIER\r\n" +
        "BEGIN:VEVENT\r\n" +
        "DTSTAMP;CHARSET=utf-8:" + helper.createDateTime(s_mockDate) + "\r\n" +
        "DESCRIPTION;CHARSET=utf-8:description\r\n" +
        "LOCATION;CHARSET=utf-8:location\r\n" +
        "BEGIN:VALARM\r\n" +
        "ACTION;CHARSET=utf-8:DISPLAY\r\n" +
        "TRIGGER;CHARSET=utf-8:-PT24H\r\n" +
        "DESCRIPTION;CHARSET=utf-8:ALARMDESC\r\n" +
        "END:VALARM\r\n" +
        "SUMMARY;CHARSET=utf-8:summary\r\n" +
        "DTSTART;CHARSET=utf-8:20200820T113000Z\r\n" + // -3 from the input date because it is converted to UTC
        "DTEND;CHARSET=utf-8:20200820T123000Z\r\n" + // -3 from the input date because it is converted to UTC
        "UID;CHARSET=utf-8:5edf8af0-5347-4331-ae0f-3b4980180b37\r\n" +
        "END:VEVENT\r\n" +
        "END:VCALENDAR\r\n";
    String createdContent = new String(cal.toBytes(StandardCharsets.UTF_8.name()), StandardCharsets.UTF_8);
    assertEquals(expectedContent, createdContent);
  }

  @Test
  public void testMeetingAllDayLate() {
    Date startDate = createTestDate(23, 45, 50);
    Date endDate = DateUtility.addDays(startDate, 2);
    assertMeetingAllDay(startDate, endDate, "20200820", "20200823"); // end date is exclusive. therefore expect one more
  }

  @Test
  public void testMeetingAllDayEarly() {
    Date startDate = createTestDate(1, 1, 0);
    Date endDate = DateUtility.addDays(startDate, 2);
    assertMeetingAllDay(startDate, endDate, "20200820", "20200823"); // end date is exclusive. therefore expect one more
  }

  @Test
  public void testMeetingAllDayTrunc() {
    Date startDate = createTestDate(0, 0, 0);
    Date endDate = DateUtility.addDays(startDate, 2);
    assertMeetingAllDay(startDate, endDate, "20200820", "20200823"); // end date is exclusive. therefore expect one more
  }

  @Test
  public void testMeetingAllDaySingle() {
    Date startDate = createTestDate(1, 30, 0);
    Date endDate = createTestDate(11, 30, 0);
    assertMeetingAllDay(startDate, endDate, "20200820", "20200821"); // end date is exclusive. therefore expect one more
  }

  @Test
  public void testWithLocalDate() {
    LocalDate startDate = LocalDate.of(2020, 3, 11);
    LocalDate endDate = LocalDate.of(2020, 3, 13);
    final String uid = "5edf8af0-5347-4331-ae0f-3b4980180b37";
    ICalVCardHelper helper = BEANS.get(ICalVCardHelper.class);
    ICalBean cal = BEANS.get(ICalBuilder.class)
        .withProductIdentifier("IDENTIFIER")
        .withComponent(BEANS.get(ICalVEventBuilder.class)
            .withDescription("description")
            .withLocation("location")
            .withSummary("summary")
            .withScheduling(startDate, endDate)
            .withUid(uid))
        .build();
    String expectedContent = "BEGIN:VCALENDAR\r\n" +
        "VERSION:2.0\r\n" +
        "PRODID;CHARSET=utf-8:IDENTIFIER\r\n" +
        "BEGIN:VEVENT\r\n" +
        "DTSTAMP;CHARSET=utf-8:" + helper.createDateTime(s_mockDate) + "\r\n" +
        "DESCRIPTION;CHARSET=utf-8:description\r\n" +
        "LOCATION;CHARSET=utf-8:location\r\n" +
        "SUMMARY;CHARSET=utf-8:summary\r\n" +
        "DTSTART;CHARSET=utf-8:20200311\r\n" +
        "DTEND;CHARSET=utf-8:20200314\r\n" +
        "UID;CHARSET=utf-8:5edf8af0-5347-4331-ae0f-3b4980180b37\r\n" +
        "END:VEVENT\r\n" +
        "END:VCALENDAR\r\n";
    String createdContent = new String(cal.toBytes(StandardCharsets.UTF_8.name()), StandardCharsets.UTF_8);
    assertEquals(expectedContent, createdContent);
  }

  protected Date createTestDate(int hourOfDay, int minute, int second) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(m_testingTimeZone);
    cal.set(2020, Calendar.AUGUST, 20, hourOfDay, minute, second);
    return cal.getTime();
  }

  protected void assertMeetingAllDay(Date startDate, Date endDate, String expectedStart, String expectedEnd) {
    final String uid = "5edf8af0-5347-4331-ae0f-3b4980180b37";
    ICalVCardHelper helper = BEANS.get(ICalVCardHelper.class);
    ICalBean cal = BEANS.get(ICalBuilder.class)
        .withProductIdentifier("IDENTIFIER")
        .withComponent(BEANS.get(ICalVEventBuilder.class)
            .withDescription("description")
            .withLocation("location")
            .withSummary("summary")
            .withScheduling(startDate, endDate, true, m_testingTimeZone)
            .withUid(uid))
        .build();
    String expectedContent = "BEGIN:VCALENDAR\r\n" +
        "VERSION:2.0\r\n" +
        "PRODID;CHARSET=utf-8:IDENTIFIER\r\n" +
        "BEGIN:VEVENT\r\n" +
        "DTSTAMP;CHARSET=utf-8:" + helper.createDateTime(s_mockDate) + "\r\n" +
        "DESCRIPTION;CHARSET=utf-8:description\r\n" +
        "LOCATION;CHARSET=utf-8:location\r\n" +
        "SUMMARY;CHARSET=utf-8:summary\r\n" +
        "DTSTART;CHARSET=utf-8:" + expectedStart + "\r\n" +
        "DTEND;CHARSET=utf-8:" + expectedEnd + "\r\n" +
        "UID;CHARSET=utf-8:5edf8af0-5347-4331-ae0f-3b4980180b37\r\n" +
        "END:VEVENT\r\n" +
        "END:VCALENDAR\r\n";
    String createdContent = new String(cal.toBytes(StandardCharsets.UTF_8.name()), StandardCharsets.UTF_8);
    System.out.println(createdContent);
    assertEquals(expectedContent, createdContent);
  }
}
