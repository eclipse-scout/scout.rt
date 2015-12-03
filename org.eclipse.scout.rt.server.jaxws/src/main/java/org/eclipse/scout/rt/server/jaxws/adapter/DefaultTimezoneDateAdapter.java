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
 * Use this adapter to work with <code>xsd:date</code>s in the default timezone of the Java Virtual Machine. Depending
 * on the JVM installation, the timezone may differ: 'GMT+-XX:XX'. Unlike {@link DefaultTimezoneDateTimeAdapter}, this
 * adapter truncates hours, minutes, seconds and milliseconds.
 * <p>
 * Whenever possible, use {@link UtcDateAdapter} or {@link CalendarDateAdapter} instead.
 * <p>
 * Fore more information, see {@link DefaultTimezoneDateTimeAdapter}.
 */
public class DefaultTimezoneDateAdapter extends DefaultTimezoneDateTimeAdapter {

  @Override
  protected void beforeMarshall(final XMLGregorianCalendar jvmLocalTime) {
    // Unset temporal information (hour, minute, second, millisecond)
    jvmLocalTime.setTime(
        DatatypeConstants.FIELD_UNDEFINED, // hour
        DatatypeConstants.FIELD_UNDEFINED, // minute
        DatatypeConstants.FIELD_UNDEFINED, // second
        DatatypeConstants.FIELD_UNDEFINED); // millisecond
  }

  @Override
  protected void beforeUnmarshall(final Calendar jvmLocalTime) {
    // Unset temporal information (hour, minute, second, millisecond)
    final int year = jvmLocalTime.get(Calendar.YEAR);
    final int dayOfYear = jvmLocalTime.get(Calendar.DAY_OF_YEAR);

    jvmLocalTime.clear();
    jvmLocalTime.set(Calendar.YEAR, year);
    jvmLocalTime.set(Calendar.DAY_OF_YEAR, dayOfYear);
  }
}
