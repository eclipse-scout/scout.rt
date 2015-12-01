/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import java.util.GregorianCalendar;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Adapter to convert a <code>xsd:dateTime</code> to a {@link Calendar} and vice versa. For both directions, the
 * timezone information is not lost. Use this adapter if you expect to work with dates from various timezones without
 * loosing the local time. If the UTC (Zulu-time) is sufficient, use {@link UtcDateTimeAdapter} instead.
 * <p>
 * The {@link String} provided must correspond to the <code>xsd:dateTime</code> format defined on
 * <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">http://www.w3.org/TR/xmlschema-2/#dateTime</a>. The format was
 * inspired by [ISO 8601] but with timezone information included, because in [ISO 8601], a time is only represented as
 * local time or in relation to UTC (Zulu time).
 * <p>
 * <h2>Specification of xsd:dateTime format</h2> <b> Format:
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
 */
public class CalendarDateTimeAdapter extends XmlAdapter<String, Calendar> {

  protected static final DatatypeFactory FACTORY;
  protected static final XMLGregorianCalendar ZULU_DEFAULTS;

  static {
    try {
      FACTORY = DatatypeFactory.newInstance();
    }
    catch (final DatatypeConfigurationException e) {
      throw new PlatformException("Failed to create 'DatatypeFactory' instance", e);
    }

    ZULU_DEFAULTS = FACTORY.newXMLGregorianCalendar();
    ZULU_DEFAULTS.setTimezone(0); // 'zulu' has no timeshift
  }

  @Override
  public String marshal(final Calendar calendar) throws Exception {
    if (calendar == null) {
      return null;
    }

    final GregorianCalendar gregCalendar = new GregorianCalendar(calendar.getTimeZone());
    gregCalendar.setTimeInMillis(calendar.getTimeInMillis());

    final XMLGregorianCalendar xmlCalendar = FACTORY.newXMLGregorianCalendar(gregCalendar);
    beforeMarshall(xmlCalendar);

    return xmlCalendar.toXMLFormat();
  }

  @Override
  public Calendar unmarshal(final String rawValue) throws Exception {
    if (!StringUtility.hasText(rawValue)) {
      return null;
    }

    final XMLGregorianCalendar xmlCalendar = FACTORY.newXMLGregorianCalendar(rawValue);
    beforeMarshall(xmlCalendar);

    return xmlCalendar.toGregorianCalendar(null, null, ZULU_DEFAULTS);
  }

  /**
   * Method invoked to intercept a 'date-time' before being marshalled.
   * <p>
   * The default implementation does nothing.
   */
  protected void beforeMarshall(final XMLGregorianCalendar calendar) {
    // NOOP
  }

  /**
   * Method invoked to intercept a 'date-time' before being unmarshalled.
   * <p>
   * The default implementation does nothing.
   */
  protected void beforeUnmarshall(final Calendar calendar) {
    // NOOP
  }
}
