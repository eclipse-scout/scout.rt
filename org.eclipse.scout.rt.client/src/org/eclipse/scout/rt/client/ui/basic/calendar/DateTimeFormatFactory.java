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
package org.eclipse.scout.rt.client.ui.basic.calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.eclipse.scout.commons.nls.NlsLocale;

public class DateTimeFormatFactory {
  private Locale m_nlsLocale;
  private boolean m_different;

  public DateTimeFormatFactory() {
    Locale loc = Locale.getDefault();
    m_nlsLocale = NlsLocale.getDefault().getLocale();
    m_different = (!loc.equals(m_nlsLocale));
  }

  public DateFormat getDayMonthYear(int style) {
    DateFormat fmt = DateFormat.getDateInstance(style);
    if (isDifferentLocale()) {
      fmt = transformDateFormat(fmt);
    }
    return fmt;
  }

  public DateFormat getHourMinute() {
    DateFormat fmt = DateFormat.getTimeInstance(DateFormat.SHORT);
    if (isDifferentLocale()) {
      fmt = transformDateFormat(fmt);
    }
    return fmt;
  }

  public DateFormat getHourMinuteSecond() {
    DateFormat fmt = DateFormat.getTimeInstance(DateFormat.MEDIUM);
    if (isDifferentLocale()) {
      fmt = transformDateFormat(fmt);
    }
    return fmt;
  }

  public DateFormat getDayMonthYearHourMinuteSecond(int dateStyle, int timeStyle) {
    DateFormat fmt = DateFormat.getDateTimeInstance(dateStyle, timeStyle);
    if (isDifferentLocale()) {
      fmt = transformDateFormat(fmt);
    }
    return fmt;
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

  private boolean isDifferentLocale() {
    return m_different;
  }

  private DateFormat transformDateFormat(DateFormat fmt) {
    if (fmt instanceof SimpleDateFormat) {
      SimpleDateFormat sfmt = (SimpleDateFormat) fmt;
      String pattern = sfmt.toPattern();
      fmt = new SimpleDateFormat(pattern, m_nlsLocale);
    }
    return fmt;
  }

  private String removeYear(String s) {
    return s.replace("[/-,. ]*[y]+[/-,.]*", "").trim();
  }

}
