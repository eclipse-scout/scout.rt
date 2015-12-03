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
 * Use this adapter to work with {@link Calendar} <code>xsd:date</code>s without loosing timezone information. Unlike
 * {@link CalendarDateTimeAdapter}, this adapter truncates hours, minutes, seconds and milliseconds.
 * <p>
 * Fore more information, see {@link CalendarDateTimeAdapter}.
 */
public final class CalendarDateAdapter extends CalendarDateTimeAdapter {

  @Override
  protected void beforeMarshall(final XMLGregorianCalendar calendar) {
    // Unset temporal information (hour, minute, second, millisecond)
    calendar.setTime(
        DatatypeConstants.FIELD_UNDEFINED, // hour
        DatatypeConstants.FIELD_UNDEFINED, // minute
        DatatypeConstants.FIELD_UNDEFINED, // second
        DatatypeConstants.FIELD_UNDEFINED); // millisecond
  }

  @Override
  protected void beforeUnmarshall(final Calendar calendar) {
    // Unset temporal information (hour, minute, second, millisecond)
    final int year = calendar.get(Calendar.YEAR);
    final int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

    calendar.clear();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
  }
}
