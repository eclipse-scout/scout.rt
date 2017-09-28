/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.adapter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Use this adapter to work with UTC <code>xsd:time</code>s. A UTC time is also known as 'zulu' time, and has
 * 'GMT+-00:00'. Unlike {@link UtcDateTimeAdapter}, this adapter sets year, month and day to the epoch, which is defined
 * as 1970-01-01 in UTC.
 * <p>
 * the name of this adapter is misleading: the given date is marshalled / unmarshaled with timezone-shifting, but the
 * day-part is always 1970-01-01 and time always between 00:00 and 23:59
 * <p>
 * Fore more information, see {@link UtcDateTimeAdapter}.
 */
public class UtcTimeAdapter extends UtcDateTimeAdapter {

  @Override
  public String marshal(final Date date) throws Exception {
    if (date == null) {
      return null;
    }

    final GregorianCalendar zuluTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    zuluTime.setTimeInMillis(date.getTime());

    final XMLGregorianCalendar zuluXmlTime = FACTORY.newXMLGregorianCalendar(zuluTime);
    // Unset date information (year, month, day)
    zuluXmlTime.setYear(DatatypeConstants.FIELD_UNDEFINED);
    zuluXmlTime.setMonth(DatatypeConstants.FIELD_UNDEFINED);
    zuluXmlTime.setDay(DatatypeConstants.FIELD_UNDEFINED);

    return zuluXmlTime.toXMLFormat();
  }

  @Override
  public Date unmarshal(final String rawValue) throws Exception {
    if (!StringUtility.hasText(rawValue)) {
      return null;
    }

    // local time of given timezone (or UTC timezone if missing)
    final XMLGregorianCalendar localTime = FACTORY.newXMLGregorianCalendar(rawValue);
    if (localTime.getHour() == DatatypeConstants.FIELD_UNDEFINED
        || localTime.getMinute() == DatatypeConstants.FIELD_UNDEFINED) {
      // time components are required
      return null;
    }

    final long utcMillis = localTime.toGregorianCalendar(null, null, ZULU_DEFAULTS).getTimeInMillis();

    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(utcMillis);
    calendar.clear(Calendar.YEAR);
    calendar.clear(Calendar.MONTH);
    calendar.set(Calendar.DATE, 1);

    return calendar.getTime();
  }
}
