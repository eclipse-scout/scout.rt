/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.adapter;

import java.util.Calendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Use this adapter to work with {@link Calendar} <code>xsd:time</code>s without loosing timezone information. Unlike
 * {@link CalendarDateTimeAdapter}, this adapter sets year, month and day to the epoch, which is defined as 1970-01-01
 * in UTC.
 * <p>
 * Fore more information, see {@link CalendarDateTimeAdapter}.
 */
public final class CalendarTimeAdapter extends CalendarDateTimeAdapter {

  @Override
  protected void beforeMarshall(final XMLGregorianCalendar calendar) {
    // Unset date information (year, month, day)
    calendar.setYear(DatatypeConstants.FIELD_UNDEFINED);
    calendar.setMonth(DatatypeConstants.FIELD_UNDEFINED);
    calendar.setDay(DatatypeConstants.FIELD_UNDEFINED);
  }

  @Override
  protected void beforeUnmarshall(final Calendar calendar) {
    // Unset date information (year, month, day)
    final int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
    final int minute = calendar.get(Calendar.MINUTE);
    final int second = calendar.get(Calendar.SECOND);
    final int millisecond = calendar.get(Calendar.MILLISECOND);

    calendar.clear();
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, second);
    calendar.set(Calendar.MILLISECOND, millisecond);
  }
}
