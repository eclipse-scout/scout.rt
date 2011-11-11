/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws216.adapters;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * <p>
 * Adapter to convert a <code>xsd:dateTime</code> to a {@link Date} and vice versa. For both directions, the
 * transformation is in respect to the default timezone accessible by {@link TimeZone#getDefault()}. If no timezone is
 * provided at all, the date is interpreted to be local to the default timezone.
 * </p>
 * <p>
 * The {@link String} provided must correspond to the <code>xsd:dateTime</code> format defined on <a
 * href="http://www.w3.org/TR/xmlschema-2/#dateTime">http://www.w3.org/TR/xmlschema-2/#dateTime</a>. The format was
 * inspired by [ISO 8601] but with timezone information included, because in [ISO 8601], a time is only represented as
 * local time or in relation to UTC (Zulu time).
 * </p>
 * <p>
 * <h2>Definition of xsd:dateTime format</h2> <b> Format:
 * <code>'-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?</code></b>
 * <ul>
 * <li>'-'? <em>yyyy</em> is a four-or-more digit optionally negative-signed numeral that represents the year; if more
 * than four digits, leading zeros are prohibited, and '0000' is prohibited; also note that a plus sign is <b>not</b>
 * permitted);</li>
 * <li>the remaining '-'s are separators between parts of the date portion;</li>
 * <li>the first <em>mm</em> is a two-digit numeral that represents the month;</li>
 * <li><em>dd</em> is a two-digit numeral that represents the day;</li>
 * <li>'T' is a separator indicating that time-of-day follows;</li>
 * <li><em>hh</em> is a two-digit numeral that represents the hour; '24' is permitted if the minutes and seconds
 * represented are zero, and the <code>dateTime</code> value so represented is the first instant of the following day
 * (the hour property of a <code>dateTime</code> object cannot have a value greater than 23);</li>
 * <li>':' is a separator between parts of the time-of-day portion;</li>
 * <li>the second <em>mm</em> is a two-digit numeral that represents the minute;</li>
 * <li><em>ss</em> is a two-integer-digit numeral that represents the whole seconds;</li>
 * <li>'.' <em>s+</em> (if present) represents the fractional seconds;</li>
 * <li><em>zzzzzz</em> (if present) represents the timezone.</li>
 * </ul>
 * </p>
 */
public class DefaultTimezoneDateAdapter extends XmlAdapter<String, Date> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultTimezoneDateAdapter.class);

  public DefaultTimezoneDateAdapter() {
  }

  @Override
  public String marshal(Date date) throws Exception {
    if (date == null) {
      return null;
    }

    long utcMillis = date.getTime();
    GregorianCalendar calendar = new GregorianCalendar(TimeZone.getDefault());
    calendar.setTimeInMillis(utcMillis);

    DatatypeFactory factory = DatatypeFactory.newInstance();
    XMLGregorianCalendar xmlCalendar = factory.newXMLGregorianCalendar(calendar);
    return xmlCalendar.toXMLFormat();
  }

  @Override
  public Date unmarshal(String rawValue) throws Exception {
    if (!StringUtility.hasText(rawValue)) {
      return null;
    }

    // local time of given timezone (or default timezone if not applicable)
    DatatypeFactory factory = DatatypeFactory.newInstance();
    XMLGregorianCalendar xmlCalendar = factory.newXMLGregorianCalendar(rawValue);
    GregorianCalendar calendar = xmlCalendar.toGregorianCalendar();
    long utcMillis = calendar.getTimeInMillis();

    // default time
    Calendar defaultTimezoneCalendar = Calendar.getInstance();
    defaultTimezoneCalendar.setTimeInMillis(utcMillis);
    return defaultTimezoneCalendar.getTime();
  }
}
