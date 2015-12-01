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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Use this adapter to work with UTC <code>xsd:time</code>s. A UTC time is also known as 'zulu' time, and has
 * 'GMT+-00:00'. Unlike {@link UtcDateTimeAdapter}, this adapter sets year, month and day to the epoch, which is defined
 * as 1970-01-01 in UTC.
 * <p>
 * Fore more information, see {@link UtcDateTimeAdapter}.
 */
public class UtcTimeAdapter extends UtcDateTimeAdapter {

  @Override
  protected void beforeMarshall(final XMLGregorianCalendar zuluTime) {
    // Unset date information (year, month, day)
    zuluTime.setYear(DatatypeConstants.FIELD_UNDEFINED);
    zuluTime.setMonth(DatatypeConstants.FIELD_UNDEFINED);
    zuluTime.setDay(DatatypeConstants.FIELD_UNDEFINED);
  }

  @Override
  protected void beforeUnmarshall(final Calendar zuluTime) {
    // Unset date information (year, month, day)
    final int hourOfDay = zuluTime.get(Calendar.HOUR_OF_DAY);
    final int minute = zuluTime.get(Calendar.MINUTE);
    final int second = zuluTime.get(Calendar.SECOND);
    final int millisecond = zuluTime.get(Calendar.MILLISECOND);

    zuluTime.clear();
    zuluTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
    zuluTime.set(Calendar.MINUTE, minute);
    zuluTime.set(Calendar.SECOND, second);
    zuluTime.set(Calendar.MILLISECOND, millisecond);
  }
}
