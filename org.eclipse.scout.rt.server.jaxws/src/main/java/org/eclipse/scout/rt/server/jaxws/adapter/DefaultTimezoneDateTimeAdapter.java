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
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Use this adapter to work with <code>xsd:dateTime</code>s in the default timezone of the Java Virtual Machine.
 * Depending on the JVM installation, the timezone may differ: 'GMT+-XX:XX'.
 * <p>
 * Whenever possible, use {@link UtcDateTimeAdapter} or {@link CalendarDateTimeAdapter} instead.
 * <p>
 * This adapter converts <code>xsd:dateTime</code> into milliseconds relative to the JVM's default timezone, by
 * respecting the timezone as provided. If the timezone is missing, the date is interpreted to be local to the JVM
 * default timezone. To convert a {@link Date} into <code>xsd:dateTime</code>, the date's milliseconds are used as
 * milliseconds from the epoch relative to the JVM's default timezone, and are formatted respectively with the current
 * 'GMT+-XX:XX'.
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
public class DefaultTimezoneDateTimeAdapter extends XmlAdapter<String, Date> {

  protected static final DatatypeFactory FACTORY;

  static {
    try {
      FACTORY = DatatypeFactory.newInstance();
    }
    catch (final DatatypeConfigurationException e) {
      throw new PlatformException("Failed to create 'DatatypeFactory' instance", e);
    }
  }

  @Override
  public String marshal(final Date date) throws Exception {
    if (date == null) {
      return null;
    }

    final GregorianCalendar jvmLocalTime = new GregorianCalendar(TimeZone.getDefault());
    jvmLocalTime.setTimeInMillis(date.getTime());

    final XMLGregorianCalendar jvmLocalXmlTime = FACTORY.newXMLGregorianCalendar(jvmLocalTime);
    beforeMarshall(jvmLocalXmlTime);

    return jvmLocalXmlTime.toXMLFormat();
  }

  @Override
  public Date unmarshal(final String rawValue) throws Exception {
    if (!StringUtility.hasText(rawValue)) {
      return null;
    }

    // local time of given timezone (or default timezone if missing)
    final XMLGregorianCalendar localTime = FACTORY.newXMLGregorianCalendar(rawValue);
    final long millis = localTime.toGregorianCalendar().getTimeInMillis();

    // UTC time
    final GregorianCalendar jvmLocalTime = new GregorianCalendar(TimeZone.getDefault());
    jvmLocalTime.setTimeInMillis(millis);
    beforeUnmarshall(jvmLocalTime);

    return new Date(jvmLocalTime.getTimeInMillis());
  }

  /**
   * Method invoked to intercept a JVM local time before being marshalled.
   * <p>
   * The default implementation does nothing.
   */
  protected void beforeMarshall(final XMLGregorianCalendar jvmLocalTime) {
    // NOOP
  }

  /**
   * Method invoked to intercept a JVM local time before being unmarshalled.
   * <p>
   * The default implementation does nothing.
   */
  protected void beforeUnmarshall(final Calendar jvmLocalTime) {
    // NOOP
  }
}
