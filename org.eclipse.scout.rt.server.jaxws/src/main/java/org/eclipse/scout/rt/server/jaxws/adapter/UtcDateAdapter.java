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
 * Use this adapter to work with UTC <code>xsd:date</code>s. A UTC date is also known as 'zulu' date, and has
 * 'GMT+-00:00'. Unlike {@link UtcDateTimeAdapter}, this adapter truncates hours, minutes, seconds and milliseconds.
 * <p>
 * Fore more information, see {@link UtcDateTimeAdapter}.
 */
public class UtcDateAdapter extends UtcDateTimeAdapter {

  @Override
  protected void beforeMarshall(final XMLGregorianCalendar zuluTime) {
    // Unset temporal information (hour, minute, second, millisecond)
    zuluTime.setTime(
        DatatypeConstants.FIELD_UNDEFINED, // hour
        DatatypeConstants.FIELD_UNDEFINED, // minute
        DatatypeConstants.FIELD_UNDEFINED, // second
        DatatypeConstants.FIELD_UNDEFINED); // millisecond
  }

  @Override
  protected void beforeUnmarshall(final Calendar zuluTime) {
    // Unset temporal information (hour, minute, second, millisecond)
    final int year = zuluTime.get(Calendar.YEAR);
    final int dayOfYear = zuluTime.get(Calendar.DAY_OF_YEAR);

    zuluTime.clear();
    zuluTime.set(Calendar.YEAR, year);
    zuluTime.set(Calendar.DAY_OF_YEAR, dayOfYear);
  }
}
