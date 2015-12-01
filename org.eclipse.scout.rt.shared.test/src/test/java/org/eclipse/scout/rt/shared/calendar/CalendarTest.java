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
package org.eclipse.scout.rt.shared.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.util.date.UTCDate;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelObjectReplacer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CalendarTest {

  private Locale m_oldLocale;
  private TimeZone m_oldTimeZone;
  private IObjectSerializer m_objectSerializer;

  @Before
  public void setUp() throws Throwable {
    m_oldLocale = Locale.getDefault();
    m_oldTimeZone = TimeZone.getDefault();
    m_objectSerializer = SerializationUtility.createObjectSerializer(new ServiceTunnelObjectReplacer());
  }

  @After
  public void tearDown() throws Throwable {
    Locale.setDefault(m_oldLocale);
    TimeZone.setDefault(m_oldTimeZone);
  }

  /**
   * A date entered on a client in the Buddhist Calendar should on the server contain the calendar of the server
   */
  @Test
  public void testBuddhistDateTransfer() throws Exception {
    //Buddhist Calendar
    Locale.setDefault(new Locale("th", "TH"));

    Date buddhistDate = new Date();
    Integer buddhistYear = Integer.valueOf(new SimpleDateFormat("yyyy").format(buddhistDate));

    byte[] outArray = write(buddhistDate);

    //Gregorian Calendar
    Locale.setDefault(new Locale("de", "CH"));

    Date transferredDate = (Date) read(outArray);
    Integer transferredYear = Integer.valueOf(new SimpleDateFormat("yyyy").format(transferredDate));

    assertEquals(buddhistDate, transferredDate);
    assertEquals(Integer.valueOf(buddhistYear - 543), transferredYear);
  }

  /**
   * A utcdate entered on a client in the Buddhist Calendar should on the server contain the calendar of the server
   */
  @Test
  public void testBuddhistUTCDateTransfer() throws Exception {
    //Buddhist Calendar
    Locale.setDefault(new Locale("th", "TH"));

    UTCDate buddhistDate = new UTCDate();
    Integer buddhistYear = Integer.valueOf(new SimpleDateFormat("yyyy").format(buddhistDate));

    byte[] outArray = write(buddhistDate);

    //Gregorian Calendar
    Locale.setDefault(new Locale("de", "CH"));

    UTCDate transferredDate = (UTCDate) read(outArray);
    Integer transferredYear = Integer.valueOf(new SimpleDateFormat("yyyy").format(transferredDate));

    assertEquals(buddhistDate, transferredDate);
    assertEquals(Integer.valueOf(buddhistYear - 543), transferredYear);
  }

  /**
   * The displayed hours of a date are the same when client and server are in different time zones
   */
  @Test
  public void testTimezoneDateTransfer() throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

    Date inDate = new Date();
    Integer hours = Integer.valueOf(new SimpleDateFormat("hh").format(inDate));

    byte[] outArray = write(inDate);

    TimeZone.setDefault(TimeZone.getTimeZone("GMT+0"));

    Date transferredDate = (Date) read(outArray);
    Integer transferredhours = Integer.valueOf(new SimpleDateFormat("hh").format(transferredDate));

    assertNotSame(inDate, transferredhours);
    assertEquals(hours, transferredhours);
  }

  /**
   * The time of a UTC date does not change when transferred through the service tunnel
   */
  @Test
  public void testTimezoneUTCDateTransfer() throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

    UTCDate inDate = new UTCDate();
    Integer hours = Integer.valueOf(new SimpleDateFormat("HH").format(inDate));

    byte[] outArray = write(inDate);

    TimeZone.setDefault(TimeZone.getTimeZone("GMT+0"));

    UTCDate transferredDate = (UTCDate) read(outArray);
    Integer transferredhours = Integer.valueOf(new SimpleDateFormat("HH").format(transferredDate));

    assertEquals(inDate, transferredDate);
    assertEquals(Integer.valueOf((hours - 1 + 24) % 24), transferredhours);
  }

  /**
   * write to the ServiceTunnel
   */
  private byte[] write(Object o) throws IOException {
    return m_objectSerializer.serialize(o);
  }

  /**
   * read from ServiceTunnel
   */
  private Object read(byte[] data) throws IOException, ClassNotFoundException {
    return m_objectSerializer.deserialize(data, Object.class);
  }
}
