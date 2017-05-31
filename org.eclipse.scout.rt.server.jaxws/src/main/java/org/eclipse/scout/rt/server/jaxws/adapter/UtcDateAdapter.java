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

import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Use this adapter to work with UTC <code>xsd:date</code>s. A UTC date is also known as 'zulu' date, and has
 * 'GMT+-00:00'. Unlike {@link UtcDateTimeAdapter}, this adapter truncates hours, minutes, seconds and milliseconds.
 * <p>
 * the name of this adapter is misleading: the given date is marshalled / unmarshaled without timezone-shifting
 * <p>
 * Fore more information, see {@link UtcDateTimeAdapter}.
 */
public class UtcDateAdapter extends UtcDateTimeAdapter {

  @Override
  public String marshal(final Date date) throws Exception {
    if (date == null) {
      return null;
    }

    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(date.getTime());

    final XMLGregorianCalendar zuluXmlTime = FACTORY.newXMLGregorianCalendar();
    zuluXmlTime.clear();
    zuluXmlTime.setTimezone(ZULU_DEFAULTS.getTimezone());
    zuluXmlTime.setYear(calendar.get(Calendar.YEAR));
    zuluXmlTime.setMonth(calendar.get(Calendar.MONTH) + 1);
    zuluXmlTime.setDay(calendar.get(Calendar.DAY_OF_MONTH));
    zuluXmlTime.setTime(
        DatatypeConstants.FIELD_UNDEFINED, // hour
        DatatypeConstants.FIELD_UNDEFINED, // minute
        DatatypeConstants.FIELD_UNDEFINED, // second
        DatatypeConstants.FIELD_UNDEFINED); // millisecond

    return zuluXmlTime.toXMLFormat();
  }

  @Override
  public Date unmarshal(final String rawValue) throws Exception {
    if (!StringUtility.hasText(rawValue)) {
      return null;
    }

    // local time of given timezone (or UTC timezone if missing)
    final XMLGregorianCalendar localTime = FACTORY.newXMLGregorianCalendar(rawValue);
    if (localTime.getYear() == DatatypeConstants.FIELD_UNDEFINED
        || localTime.getMonth() == DatatypeConstants.FIELD_UNDEFINED
        || localTime.getDay() == DatatypeConstants.FIELD_UNDEFINED) {
      // date components are required
      return null;
    }

    final Calendar calendar = Calendar.getInstance();
    calendar.clear();

    calendar.set(Calendar.YEAR, localTime.getYear());
    calendar.set(Calendar.MONTH, localTime.getMonth() - 1);
    calendar.set(Calendar.DAY_OF_MONTH, localTime.getDay());

    return calendar.getTime();
  }
}
