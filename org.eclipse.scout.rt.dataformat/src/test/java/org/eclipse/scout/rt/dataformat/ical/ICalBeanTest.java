/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataformat.ical;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.dataformat.ical.model.ICalVCardHelper;
import org.eclipse.scout.rt.dataformat.ical.model.Property;
import org.eclipse.scout.rt.dataformat.ical.model.PropertyParameter;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.junit.Test;

public class ICalBeanTest {

  @Test
  public void testWrite() {
    ICalBean cal = BEANS.get(ICalBean.class);
    ICalVCardHelper helper = BEANS.get(ICalVCardHelper.class);

    Date prodDate = BEANS.get(IDateProvider.class).currentMillis();
    Date startDate = DateUtility.addDays(BEANS.get(IDateProvider.class).currentMillis(), 1);
    Date endDate = DateUtility.addHours(startDate, 2);
    String uid = "e11d9aac-890f-409b-a016-3acef4685dc6";

    cal.addProperty(ICalProperties.PROP_BEGIN_ICALENDAR);
    cal.addProperty(ICalProperties.PROP_VERSION_2_0);
    cal.addProperty(new Property(ICalProperties.PROP_NAME_PRODID, "prodid doe corporation"));
    cal.addProperty(ICalProperties.PROP_BEGIN_VEVENT);
    cal.addProperty(new Property(ICalProperties.PROP_NAME_DTSTAMP, helper.createDateTime(prodDate)));
    cal.addProperty(new Property(ICalProperties.PROP_NAME_UID, uid));
    cal.addProperty(new Property(ICalProperties.PROP_NAME_DTSTART, helper.createDateTime(startDate)));
    cal.addProperty(new Property(ICalProperties.PROP_NAME_DTEND, helper.createDateTime(endDate)));
    cal.addProperty(new Property(ICalProperties.PROP_NAME_ORGANIZER, Arrays.asList(new PropertyParameter(ICalProperties.PARAM_NAME_CN, "\"John Doe\"")),
        "mailto:john@doe.mail.com"));

    cal.addProperty(ICalProperties.PROP_END_VEVENT);
    cal.addProperty(ICalProperties.PROP_END_ICALENDAR);

    StringWriter bw = new StringWriter();
    cal.write(bw, "utf-8");

    String writtenIcal = bw.toString();

    assertEquals("BEGIN:VCALENDAR\r\n" +
        "VERSION:2.0\r\n" +
        "PRODID;CHARSET=utf-8:prodid doe corporation\r\n" +
        "BEGIN:VEVENT\r\n" +
        "DTSTAMP;CHARSET=utf-8:" + helper.createDateTime(prodDate) + "\r\n" +
        "UID;CHARSET=utf-8:" + uid + "\r\n" +
        "DTSTART;CHARSET=utf-8:" + helper.createDateTime(startDate) + "\r\n" +
        "DTEND;CHARSET=utf-8:" + helper.createDateTime(endDate) + "\r\n" +
        "ORGANIZER;CHARSET=utf-8;CN=\"John Doe\":mailto:john@doe.mail.com\r\n" +
        "END:VEVENT\r\n" +
        "END:VCALENDAR\r\n", writtenIcal);
  }

  @Test
  public void testParse() {
    ICalVCardHelper helper = BEANS.get(ICalVCardHelper.class);
    Date prodDate = BEANS.get(IDateProvider.class).currentMillis();
    Date startDate = DateUtility.addDays(BEANS.get(IDateProvider.class).currentMillis(), 1);
    Date endDate = DateUtility.addHours(startDate, 2);
    String uid = "478bb47d-c9a7-43d2-a920-c5a8b952d478";

    final String ical = "BEGIN:VCALENDAR\r\n" +
        "VERSION:2.0\r\n" +
        "PRODID;CHARSET=utf-8:prodid doe corporation\r\n" +
        "BEGIN:VEVENT\r\n" +
        "DTSTAMP;CHARSET=utf-8:" + helper.createDateTime(prodDate) + "\r\n" +
        "UID;CHARSET=utf-8:" + uid + "\r\n" +
        "DTSTART;CHARSET=utf-8:" + helper.createDateTime(startDate) + "\r\n" +
        "DTEND;CHARSET=utf-8:" + helper.createDateTime(endDate) + "\r\n" +
        "ORGANIZER;CHARSET=utf-8;CN=\"John Doe\":mailto:john@doe.mail.com\r\n" +
        "END:VEVENT\r\n" +
        "END:VCALENDAR\r\n";

    ICalBean bean = ICalBean.parse(new StringReader(ical), "utf-8");

    assertNotNull(bean.getProperty(ICalProperties.PROP_NAME_BEGIN));
    assertNotNull(bean.getProperty(ICalProperties.PROP_NAME_VERSION));
    assertNotNull(bean.getProperty(ICalProperties.PROP_NAME_PRODID));
    assertNotNull(bean.getProperty(ICalProperties.PROP_NAME_DTSTAMP));
    assertNotNull(bean.getProperty(ICalProperties.PROP_NAME_UID));
    assertNotNull(bean.getProperty(ICalProperties.PROP_NAME_DTSTART));
    assertNotNull(bean.getProperty(ICalProperties.PROP_NAME_DTEND));
    assertNotNull(bean.getProperty(ICalProperties.PROP_NAME_ORGANIZER));

    assertEquals(ICalProperties.PROP_VALUE_VERSION_2_0, bean.getProperty(ICalProperties.PROP_NAME_VERSION).getValue());
    assertEquals("prodid doe corporation", bean.getProperty(ICalProperties.PROP_NAME_PRODID).getValue());
    assertEquals(helper.createDateTime(prodDate), bean.getProperty(ICalProperties.PROP_NAME_DTSTAMP).getValue());
    assertEquals(uid, bean.getProperty(ICalProperties.PROP_NAME_UID).getValue());
    assertEquals(helper.createDateTime(startDate), bean.getProperty(ICalProperties.PROP_NAME_DTSTART).getValue());
    assertEquals(helper.createDateTime(endDate), bean.getProperty(ICalProperties.PROP_NAME_DTEND).getValue());
    assertEquals("mailto:john@doe.mail.com", bean.getProperty(ICalProperties.PROP_NAME_ORGANIZER).getValue());
    assertEquals("John Doe", bean.getProperty(ICalProperties.PROP_NAME_ORGANIZER).getParameter(ICalProperties.PARAM_NAME_CN).getValue());

    List<Property> beginProps = bean.getProperties(ICalProperties.PROP_NAME_BEGIN);
    assertNotNull(beginProps);
    assertEquals(ICalProperties.PROP_VALUE_ICALENDAR, beginProps.get(0).getValue());
    assertEquals(ICalProperties.PROP_VALUE_VEVENT, beginProps.get(1).getValue());

    List<Property> endProps = bean.getProperties(ICalProperties.PROP_NAME_END);
    assertNotNull(endProps);
    assertEquals(ICalProperties.PROP_VALUE_VEVENT, endProps.get(0).getValue());
    assertEquals(ICalProperties.PROP_VALUE_ICALENDAR, endProps.get(1).getValue());
  }
}
