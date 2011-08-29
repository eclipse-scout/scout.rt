/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class FormattingUtility {
  private FormattingUtility() {
  }

  public static String fixedRecord(String s, int length) {
    StringBuffer b = new StringBuffer(s != null ? s : "");
    while (b.length() < length) {
      b.append(" ");
    }
    return b.toString();
  }

  /**
   * Formats given Object and converts to String handled are: {@link String} {@link java.util.Date} is formatted using
   * {@link java.text.DateFormat#MEDIUM} optionally displaying time when time is
   * other than 00:00:00 {@link Number} is formatted using {@link java.text.NumberFormat} {@link Boolean} is formatted
   * as "X" for
   * true, "" for false
   * 
   * @param Object
   *          o: Object to format
   * @return String: formatted and converted Object
   */
  public static String formatObject(Object o) {
    Locale loc = LocaleThreadLocal.get();
    if (loc == null) {
      loc = Locale.getDefault();
    }

    String ret = null;

    if (o instanceof String) {
      ret = (String) o;
    }
    else if (o instanceof Date) {
      ret = DateFormat.getDateInstance(DateFormat.MEDIUM, loc).format(o);

      // get time hours, minutes, seconds
      Calendar cal = Calendar.getInstance();
      cal.setTime((Date) o);
      int hour = cal.get(Calendar.HOUR);
      int minute = cal.get(Calendar.MINUTE);
      int second = cal.get(Calendar.SECOND);
      // if time different to 00:00:00
      // format with time
      if (hour != 0 || minute != 0 || second != 0) {
        ret = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, loc).format(o);
      }
    }
    else if (o instanceof Float || o instanceof Double) {
      NumberFormat f = new DecimalFormat();
      f.setMinimumFractionDigits(2);
      ret = f.format(o);
    }
    else if (o instanceof Number) {
      ret = NumberFormat.getNumberInstance(loc).format(o);
    }
    else if (o instanceof Boolean) {
      ret = ((Boolean) o).booleanValue() == true ? "X" : "";
    }

    return ret == null ? "" : ret;
  }
}
