/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataformat.ical.builder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Date;
import java.util.List;

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
    Date startDate = BEANS.get(IDateProvider.class).currentMillis();
    Date endDate = DateUtility.addHours(startDate, 1);

    ICalBean cal = BEANS.get(ICalBuilder.class)
        .withProductIdentifier("IDENTIFIER")
        .withComponent(BEANS.get(ICalVEventBuilder.class)
            .withDescription("description")
            .withLocation("location")
            .withAlarm(Duration.ofDays(1), "ALARMDESC")
            .withSummary("summary")
            .withScheduling(startDate, endDate, false)
            .withUid(uid))
        .build();

    assertEquals("BEGIN:VCALENDAR\r\n" +
        "VERSION:2.1\r\n" +
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
        "DTSTART;CHARSET=utf-8:" + helper.createDateTime(startDate) + "\r\n" +
        "DTEND;CHARSET=utf-8:" + helper.createDateTime(endDate) + "\r\n" +
        "UID;CHARSET=utf-8:5edf8af0-5347-4331-ae0f-3b4980180b37\r\n" +
        "END:VEVENT\r\n" +
        "END:VCALENDAR\r\n" +
        "", new String(cal.toBytes("utf-8")));
  }

  @Test
  public void testMeetingAllDay() {
    final String uid = "5edf8af0-5347-4331-ae0f-3b4980180b37";
    ICalVCardHelper helper = BEANS.get(ICalVCardHelper.class);
    Date startDate = BEANS.get(IDateProvider.class).currentMillis();
    Date endDate = DateUtility.addDays(startDate, 2);

    ICalBean cal = BEANS.get(ICalBuilder.class)
        .withProductIdentifier("IDENTIFIER")
        .withComponent(BEANS.get(ICalVEventBuilder.class)
            .withDescription("description")
            .withLocation("location")
            .withSummary("summary")
            .withScheduling(startDate, endDate, true)
            .withUid(uid))
        .build();

    assertEquals("BEGIN:VCALENDAR\r\n" +
        "VERSION:2.1\r\n" +
        "PRODID;CHARSET=utf-8:IDENTIFIER\r\n" +
        "BEGIN:VEVENT\r\n" +
        "DTSTAMP;CHARSET=utf-8:" + helper.createDateTime(s_mockDate) + "\r\n" +
        "DESCRIPTION;CHARSET=utf-8:description\r\n" +
        "LOCATION;CHARSET=utf-8:location\r\n" +
        "SUMMARY;CHARSET=utf-8:summary\r\n" +
        "DTSTART;CHARSET=utf-8:" + helper.createDate(startDate) + "\r\n" +
        "DTEND;CHARSET=utf-8:" + helper.createDate(endDate) + "\r\n" +
        "UID;CHARSET=utf-8:5edf8af0-5347-4331-ae0f-3b4980180b37\r\n" +
        "END:VEVENT\r\n" +
        "END:VCALENDAR\r\n" +
        "", new String(cal.toBytes("utf-8")));
  }
}
