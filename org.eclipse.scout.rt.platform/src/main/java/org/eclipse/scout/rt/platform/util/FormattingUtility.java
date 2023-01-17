/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.date.DateFormatProvider;

public final class FormattingUtility {
  private FormattingUtility() {
  }

  /**
   * Formats the given object respecting the current thread's {@link NlsLocale} and converts it to a String. Supported
   * types are:
   * <ul>
   * <li>{@link String}</li>
   * <li>{@link Date} with empty time part is formatted using {@link DateFormat#MEDIUM}
   * <li>{@link Date} with non-empty time part is formatted {@link DateFormat#SHORT} for date and time part,
   * respectively</li>
   * <li>{@link Float}, {@link Double} and {@link BigDecimal} are formatted using {@link NumberFormat} with exactly 2
   * fraction digits</li>
   * <li>{@link Number} is formatted using {@link NumberFormat}</li>
   * <li>{@link Boolean} is formatted as "X" for <code>true</code>, "" for <code>false</code></li>
   * </ul>
   *
   * @param o
   *          object to format
   * @return Returns formatted string representation, never <code>null</code>.
   */
  public static String formatObject(Object o) {
    Locale loc = NlsLocale.get();
    String ret = null;
    if (o instanceof String) {
      ret = (String) o;
    }
    else if (o instanceof Date) {
      ret = BEANS.get(DateFormatProvider.class).getDateInstance(DateFormat.MEDIUM, loc).format(o);

      // get time hours, minutes, seconds
      Calendar cal = Calendar.getInstance();
      cal.setTime((Date) o);
      int hour = cal.get(Calendar.HOUR);
      int minute = cal.get(Calendar.MINUTE);
      int second = cal.get(Calendar.SECOND);
      // if time different to 00:00:00
      // format with time
      if (hour != 0 || minute != 0 || second != 0) {
        ret = BEANS.get(DateFormatProvider.class).getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, loc).format(o);
      }
    }
    else if (o instanceof Float || o instanceof Double || o instanceof BigDecimal) {
      NumberFormat f = BEANS.get(NumberFormatProvider.class).getNumberInstance(loc);
      f.setMinimumFractionDigits(2);
      f.setMaximumFractionDigits(2);
      ret = f.format(o);
    }
    else if (o instanceof Number) {
      ret = BEANS.get(NumberFormatProvider.class).getNumberInstance(loc).format(o);
    }
    else if (o instanceof Boolean) {
      ret = ((Boolean) o) ? "X" : "";
    }

    return ret == null ? "" : ret;
  }
}
