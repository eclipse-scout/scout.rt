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

import org.eclipse.scout.commons.LocaleThreadLocal;

public class DateTimeFormatFactory {

  public DateTimeFormatFactory() {
  }

  public DateFormat getDayMonthYear(int style) {
    return DateFormat.getDateInstance(style, LocaleThreadLocal.get());
  }

  public DateFormat getHourMinute() {
    return DateFormat.getTimeInstance(DateFormat.SHORT, LocaleThreadLocal.get());
  }

  public DateFormat getHourMinuteSecond() {
    return DateFormat.getTimeInstance(DateFormat.MEDIUM, LocaleThreadLocal.get());
  }

  public DateFormat getDayMonthYearHourMinuteSecond(int dateStyle, int timeStyle) {
    return DateFormat.getDateTimeInstance(dateStyle, timeStyle, LocaleThreadLocal.get());
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
