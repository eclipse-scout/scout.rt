/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.date.DateFormatProvider;

public class DateTimeFormatFactory {

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
