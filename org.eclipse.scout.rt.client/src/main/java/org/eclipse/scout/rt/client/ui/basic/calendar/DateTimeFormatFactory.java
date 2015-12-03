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
package org.eclipse.scout.rt.client.ui.basic.calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.date.DateFormatProvider;

public class DateTimeFormatFactory {

  public DateTimeFormatFactory() {
  }

  public DateFormat getDayMonthYear(int style) {
    return BEANS.get(DateFormatProvider.class).getDateInstance(style, NlsLocale.get());
  }

  public DateFormat getHourMinute() {
    return BEANS.get(DateFormatProvider.class).getTimeInstance(DateFormat.SHORT, NlsLocale.get());
  }

  public DateFormat getHourMinuteSecond() {
    return BEANS.get(DateFormatProvider.class).getTimeInstance(DateFormat.MEDIUM, NlsLocale.get());
  }

  public DateFormat getDayMonthYearHourMinuteSecond(int dateStyle, int timeStyle) {
    return BEANS.get(DateFormatProvider.class).getDateTimeInstance(dateStyle, timeStyle, NlsLocale.get());
  }

  /**
   * Derived formats
   */
  public DateFormat getDayMonth(int style) {
    DateFormat fmt = getDayMonthYear(style);
    if (fmt instanceof SimpleDateFormat) {
      String pattern = ((SimpleDateFormat) fmt).toPattern();
      pattern = removeYear(pattern);
      ((SimpleDateFormat) fmt).applyPattern(pattern);
    }
    return fmt;
  }

  private String removeYear(String s) {
    return s.replaceAll("[/\\-,. ]*[y]+[/\\-,.]*", "").trim();
  }

}
