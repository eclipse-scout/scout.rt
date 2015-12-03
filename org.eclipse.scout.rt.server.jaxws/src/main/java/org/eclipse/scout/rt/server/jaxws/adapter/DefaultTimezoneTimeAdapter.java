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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Use this adapter to work with <code>xsd:time</code>s in the default timezone of the Java Virtual Machine. Depending
 * on the JVM installation, the timezone may differ: 'GMT+-XX:XX'. Unlike {@link DefaultTimezoneDateTimeAdapter}, this
 * adapter sets year, month and day to the epoch, which is defined as 1970-01-01 in UTC.
 * <p>
 * Whenever possible, use {@link UtcTimeAdapter} or {@link CalendarTimeAdapter} instead.
 * <p>
 * Fore more information, see {@link DefaultTimezoneDateTimeAdapter}.
 */
public class DefaultTimezoneTimeAdapter extends DefaultTimezoneDateTimeAdapter {

  @Override
  protected void beforeMarshall(final XMLGregorianCalendar jvmLocalTime) {
    // Unset date information (year, month, day)
    jvmLocalTime.setYear(DatatypeConstants.FIELD_UNDEFINED);
    jvmLocalTime.setMonth(DatatypeConstants.FIELD_UNDEFINED);
    jvmLocalTime.setDay(DatatypeConstants.FIELD_UNDEFINED);
  }

  @Override
  protected void beforeUnmarshall(final Calendar jvmLocalTime) {
    // Unset date information (year, month, day)
    final int hourOfDay = jvmLocalTime.get(Calendar.HOUR_OF_DAY);
    final int minute = jvmLocalTime.get(Calendar.MINUTE);
    final int second = jvmLocalTime.get(Calendar.SECOND);
    final int millisecond = jvmLocalTime.get(Calendar.MILLISECOND);

    jvmLocalTime.clear();
    jvmLocalTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
    jvmLocalTime.set(Calendar.MINUTE, minute);
    jvmLocalTime.set(Calendar.SECOND, second);
    jvmLocalTime.set(Calendar.MILLISECOND, millisecond);
  }
}
